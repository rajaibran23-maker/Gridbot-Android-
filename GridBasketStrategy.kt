package com.example.engine

/**
 * Deterministic grid/basket decision core. It does NOT send broker orders.
 * A verified MT5 execution adapter must consume StrategyAction values.
 * P/L passed to onQuote must be net of commission, swap and fees.
 */
class GridBasketStrategy(
    private val emergencyLossUsd: Double = 50.0,
    private val firstTargetsUsd: Double = 1.0,
    private val finalBasketTargetUsd: Double = 3.0
) {
    enum class Side { BUY, SELL }
    data class Position(val ticket: String, val side: Side, val lot: Double, val entryPrice: Double)
    sealed class StrategyAction {
        data class Open(val side: Side, val lot: Double, val level: Int) : StrategyAction()
        data class CloseAll(val reason: String) : StrategyAction()
        data class Halt(val reason: String) : StrategyAction()
    }

    private val lots = listOf(0.01, 0.02, 0.03, 0.05)
    private val positions = mutableListOf<Position>()
    private var halted = false
    private var lastEntryPrice: Double? = null
    private var symbol: String? = null
    private var pendingOpenCount: Int? = null
    private var pendingClose = false

    @Synchronized
    fun onQuote(
        symbol: String,
        side: Side,
        bid: Double,
        ask: Double,
        netBasketProfitUsd: Double,
        adverseStepPrice: Double,
        openPositions: List<Position>
    ): List<StrategyAction> {
        if (halted || pendingClose) return emptyList()
        if (openPositions.isEmpty()) {
            positions.clear()
            lastEntryPrice = null
            this.symbol = symbol
        } else {
            positions.clear(); positions.addAll(openPositions)
            if (this.symbol != symbol) return listOf(StrategyAction.Halt("One strategy instance may manage only one symbol."))
            val expected = pendingOpenCount
            if (expected != null && positions.size >= expected) pendingOpenCount = null
            if (positions.map { it.side }.distinct().size > 1 || positions.any { it.side != side }) {
                halted = true
                return listOf(StrategyAction.Halt("Mixed BUY/SELL positions are not allowed in one basket."))
            }
        }
        if (pendingOpenCount != null) return emptyList() // wait for broker confirmation; prevent duplicate grid orders

        // Emergency stop is based on the net P/L of every position managed by this strategy.
        if (netBasketProfitUsd <= -emergencyLossUsd) {
            halted = true
            pendingClose = true
            return listOf(StrategyAction.CloseAll("Emergency stop: basket P/L reached -$${emergencyLossUsd}"), StrategyAction.Halt("Manual restart required after emergency stop."))
        }

        if (positions.isNotEmpty()) {
            val target = if (positions.any { it.lot >= 0.05 - 1e-8 }) finalBasketTargetUsd else firstTargetsUsd
            if (netBasketProfitUsd >= target) {
                pendingClose = true
                return listOf(StrategyAction.CloseAll("Basket target reached: +$${target}"))
            }
        }

        val nextIndex = positions.size
        if (nextIndex >= lots.size) return emptyList()
        val executablePrice = if (side == Side.BUY) ask else bid
        if (positions.isEmpty()) {
            lastEntryPrice = executablePrice
            pendingOpenCount = 1
            return listOf(StrategyAction.Open(side, lots[0], 1))
        }
        val anchor = lastEntryPrice ?: positions.last().entryPrice
        // Grid spacing: 1 step, 1 step, then 2 steps before the 0.05 level.
        val distance = adverseStepPrice * if (nextIndex == 3) 2.0 else 1.0
        val adverseMove = if (side == Side.BUY) anchor - bid else ask - anchor
        if (adverseMove + 1e-10 >= distance) {
            lastEntryPrice = executablePrice
            pendingOpenCount = positions.size + 1
            return listOf(StrategyAction.Open(side, lots[nextIndex], nextIndex + 1))
        }
        return emptyList()
    }

    @Synchronized
    fun onBasketClosed() {
        positions.clear()
        lastEntryPrice = null
        symbol = null
        pendingOpenCount = null
        pendingClose = false
    }

    @Synchronized
    fun resetAfterEmergencyStop() { onBasketClosed(); halted = false }


    companion object {
        fun forexPipSize(symbol: String, digits: Int): Double =
            if (symbol.uppercase().endsWith("JPY")) 0.01 else if (digits == 5 || digits == 3) 0.0001 else 0.0001

        fun forexStepPrice(symbol: String, digits: Int, pips: Double = 10.0): Double =
            forexPipSize(symbol, digits) * pips
    }
}

