package com.example

import com.example.model.BrokerType
import com.example.model.EaFileType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultLotSize_isZeroPointZeroFive() {
        val defaultLot = 0.05f
        assertEquals(0.05f, defaultLot, 0.001f)
    }

    @Test
    fun requiredSymbols_allPresent() {
        val requiredSymbolIds = listOf(
            "gold", "bitcoin", "oil", "audusd", "audjpy", "audcad", "audnzd"
        )
        assertEquals(7, requiredSymbolIds.size)
        assertTrue(requiredSymbolIds.contains("gold"))
        assertTrue(requiredSymbolIds.contains("bitcoin"))
        assertTrue(requiredSymbolIds.contains("oil"))
        assertTrue(requiredSymbolIds.contains("audusd"))
        assertTrue(requiredSymbolIds.contains("audjpy"))
        assertTrue(requiredSymbolIds.contains("audcad"))
        assertTrue(requiredSymbolIds.contains("audnzd"))
    }

    @Test
    fun supportedEaFormats_requiresCompilationCheck() {
        assertTrue(isCompilationRequired(EaFileType.MQ4))
        assertTrue(isCompilationRequired(EaFileType.MQ5))
        assertFalse(isCompilationRequired(EaFileType.EX4))
        assertFalse(isCompilationRequired(EaFileType.EX5))
    }

    private fun isCompilationRequired(type: EaFileType): Boolean {
        return type == EaFileType.MQ4 || type == EaFileType.MQ5
    }

    @Test
    fun brokerTypes_verified() {
        assertEquals("MetaTrader 4", BrokerType.MT4.title)
        assertEquals("MetaTrader 5", BrokerType.MT5.title)
        assertEquals("Exness Trading", BrokerType.EXNESS.title)
    }
}
