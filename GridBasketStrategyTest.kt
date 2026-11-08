package com.example.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GridBasketStrategyTest {
    @Test fun opensConfiguredLotsInSequenceAndUsesBasketTargets() {
        val engine = GridBasketStrategy()
        val first = engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 2000.0, 2000.2, 0.0, 1.0, emptyList())
        assertEquals(0.01, (first.single() as GridBasketStrategy.StrategyAction.Open).lot, 0.000001)

        val positions = mutableListOf(GridBasketStrategy.Position("1", GridBasketStrategy.Side.BUY, 0.01, 2000.2))
        assertTrue(engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 1999.1, 1999.3, -2.0, 1.0, positions).isEmpty())
        val second = engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 1999.0, 1999.2, -2.0, 1.0, positions).single() as GridBasketStrategy.StrategyAction.Open
        assertEquals(0.02, second.lot, 0.000001)

        val stageTwo = positions + GridBasketStrategy.Position("2", GridBasketStrategy.Side.BUY, 0.02, 1999.2)
        val third = engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 1998.0, 1998.2, -2.0, 1.0, stageTwo).single() as GridBasketStrategy.StrategyAction.Open
        assertEquals(0.03, third.lot, 0.000001)

        val stageThree = stageTwo + GridBasketStrategy.Position("3", GridBasketStrategy.Side.BUY, 0.03, 1998.2)
        val fourth = engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 1996.0, 1996.2, -5.0, 1.0, stageThree).single() as GridBasketStrategy.StrategyAction.Open
        assertEquals(0.05, fourth.lot, 0.000001)

        val stageFour = stageThree + GridBasketStrategy.Position("4", GridBasketStrategy.Side.BUY, 0.05, 1996.2)
        val closeAtThree = engine.onQuote("XAUUSD", GridBasketStrategy.Side.BUY, 1996.0, 1996.2, 3.0, 1.0, stageFour).single()
        assertTrue(closeAtThree is GridBasketStrategy.StrategyAction.CloseAll)
    }

    @Test fun emergencyStopHaltsUntilManualReset() {
        val engine = GridBasketStrategy()
        val actions = engine.onQuote("XAUUSD", GridBasketStrategy.Side.SELL, 2000.0, 2000.2, -50.0, 1.0, emptyList())
        assertTrue(actions.first() is GridBasketStrategy.StrategyAction.CloseAll)
        assertTrue(actions.last() is GridBasketStrategy.StrategyAction.Halt)
        assertTrue(engine.onQuote("XAUUSD", GridBasketStrategy.Side.SELL, 2000.0, 2000.2, 0.0, 1.0, emptyList()).isEmpty())
    }
}
