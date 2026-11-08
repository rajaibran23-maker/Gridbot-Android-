package com.example.engine

import com.example.model.ActiveTrade
import com.example.model.BrokerConfig
import com.example.model.LogType
import com.example.model.TradeLog
import com.example.model.TradingSymbol
import com.example.model.VpsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

class AutomatedTradingEngine(
    private val scope: CoroutineScope
) {
    private var tradingJob: Job? = null
    private val ticketGenerator = AtomicLong(8820491L)

    private val _isRobotOn = MutableStateFlow(false)
    val isRobotOn: StateFlow<Boolean> = _isRobotOn.asStateFlow()

    private val _lotSize = MutableStateFlow(0.05f)
    val lotSize: StateFlow<Float> = _lotSize.asStateFlow()

    private val _logs = MutableStateFlow<List<TradeLog>>(emptyList())
    val logs: StateFlow<List<TradeLog>> = _logs.asStateFlow()

    private val _activeTrades = MutableStateFlow<List<ActiveTrade>>(emptyList())
    val activeTrades: StateFlow<List<ActiveTrade>> = _activeTrades.asStateFlow()

    private val _totalProfit = MutableStateFlow(0.0)
    val totalProfit: StateFlow<Double> = _totalProfit.asStateFlow()

    fun setLotSize(size: Float) {
        val cleanSize = (Math.round(size * 100.0) / 100.0).toFloat().coerceIn(0.01f, 100.0f)
        _lotSize.value = cleanSize
        addLog("Updated lot size to $cleanSize", LogType.INFO)
    }

    fun startRobot(
        brokerConfig: BrokerConfig,
        vpsConfig: VpsConfig,
        symbols: List<TradingSymbol>
    ): Result<Unit> {
        val err = "Live trading is disabled: this project has no verified MT5 order-execution adapter. A socket/VPS connection is not an MT5 trading login. No orders were sent."
        addLog(err, LogType.ERROR)
        return Result.failure(IllegalStateException(err))
    }

    fun stopRobot() {
        _isRobotOn.value = false
        tradingJob?.cancel()
        tradingJob = null
        addLog("AUTOMATIC TRADING ROBOT STOPPED: No new trades will be opened.", LogType.WARNING)
    }

    fun addLog(message: String, type: LogType = LogType.INFO) {
        val log = TradeLog(message = message, type = type)
        _logs.update { (listOf(log) + it).take(100) }
    }
}
