package com.example.model

enum class BrokerType(val title: String, val defaultPort: Int, val description: String) {
    MT4("MetaTrader 4", 443, "Standard MetaTrader 4 broker gateway protocol"),
    MT5("MetaTrader 5", 443, "Next-gen MetaTrader 5 multi-asset protocol"),
    EXNESS("Exness Trading", 443, "Exness Real / Demo high-speed terminal server")
}

data class BrokerConfig(
    val brokerType: BrokerType = BrokerType.MT4,
    val login: String = "",
    val password: String = "", // Kept encrypted in storage
    val server: String = "",
    val isConnected: Boolean = false,
    val connectionMessage: String = "Disconnected",
    val pingMs: Long = 0,
    val balance: Double = 0.0,
    val equity: Double = 0.0,
    val currency: String = "USD",
    val leverage: String = "1:500"
)

data class VpsConfig(
    val ipAddress: String = "",
    val port: String = "22",
    val username: String = "Administrator",
    val password: String = "", // Kept encrypted in storage
    val isConnected: Boolean = false,
    val connectionMessage: String = "VPS Disconnected",
    val pingMs: Long = 0,
    val osInfo: String = "Unknown",
    val metaEditorReady: Boolean = false,
    val activeTerminalPath: String = ""
)

enum class SymbolCategory {
    COMMODITIES,
    CRYPTO,
    FOREX
}

data class TradingSymbol(
    val id: String,
    val displayName: String,
    val code: String,
    val category: SymbolCategory,
    val isEnabled: Boolean = true,
    val bid: Double = 0.0,
    val ask: Double = 0.0,
    val spread: Double = 0.0,
    val digits: Int = 2
)

enum class EaFileType {
    MQ4,
    EX4,
    MQ5,
    EX5
}

enum class FileExecutionStatus {
    IDLE,
    UPLOADED,
    COMPILING,
    COMPILED,
    COMPILATION_ERROR,
    RUNNING,
    STOPPED
}

data class EaFileItem(
    val id: String,
    val fileName: String,
    val fileType: EaFileType,
    val fileSizeFormatted: String,
    val isRequiresCompilation: Boolean, // MQ4 & MQ5 require MetaEditor compilation
    val status: FileExecutionStatus = FileExecutionStatus.IDLE,
    val statusDetails: String = "Ready to deploy",
    val isBundledTemplate: Boolean = false,
    val sourceCode: String = ""
)

data class TradeLog(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    TRADE
}

data class ActiveTrade(
    val ticket: Long,
    val symbol: String,
    val type: String, // BUY or SELL
    val lots: Double,
    val openPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val profit: Double,
    val openTime: Long = System.currentTimeMillis()
)
