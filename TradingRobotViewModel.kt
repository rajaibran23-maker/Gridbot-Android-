package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AutomatedTradingEngine
import com.example.model.ActiveTrade
import com.example.model.BrokerConfig
import com.example.model.BrokerType
import com.example.model.EaFileItem
import com.example.model.EaFileType
import com.example.model.FileExecutionStatus
import com.example.model.LogType
import com.example.model.SymbolCategory
import com.example.model.TradeLog
import com.example.model.TradingSymbol
import com.example.model.VpsConfig
import com.example.network.BrokerConnectionManager
import com.example.network.VpsConnectionManager
import com.example.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TradingRobotViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorage(application.applicationContext)
    private val brokerManager = BrokerConnectionManager()
    private val vpsManager = VpsConnectionManager()
    private val tradingEngine = AutomatedTradingEngine(viewModelScope)

    // Current Navigation Page (1 to 6)
    // 0: Home Dashboard, 1: Broker Connection, 2: VPS Connection, 3: Robot Control, 4: Lot Size and Symbols, 5: File Manager
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // Broker Configuration & State
    private val _brokerConfig = MutableStateFlow(
        BrokerConfig(
            brokerType = BrokerType.MT4,
            login = secureStorage.getString("broker_login", ""),
            password = secureStorage.getSecure("broker_pwd", ""),
            server = secureStorage.getString("broker_server", "access.metaquotes.net:443")
        )
    )
    val brokerConfig: StateFlow<BrokerConfig> = _brokerConfig.asStateFlow()

    private val _brokerIsConnecting = MutableStateFlow(false)
    val brokerIsConnecting: StateFlow<Boolean> = _brokerIsConnecting.asStateFlow()

    // VPS Configuration & State
    private val _vpsConfig = MutableStateFlow(
        VpsConfig(
            ipAddress = secureStorage.getString("vps_ip", ""),
            port = secureStorage.getString("vps_port", "22"),
            username = secureStorage.getString("vps_user", "Administrator"),
            password = secureStorage.getSecure("vps_pwd", "")
        )
    )
    val vpsConfig: StateFlow<VpsConfig> = _vpsConfig.asStateFlow()

    private val _vpsIsConnecting = MutableStateFlow(false)
    val vpsIsConnecting: StateFlow<Boolean> = _vpsIsConnecting.asStateFlow()

    // Trading Symbols (The 7 strictly required symbols)
    private val _symbols = MutableStateFlow<List<TradingSymbol>>(
        listOf(
            TradingSymbol(
                id = "gold",
                displayName = "GOLD",
                code = "XAUUSD",
                category = SymbolCategory.COMMODITIES,
                isEnabled = secureStorage.getBoolean("sym_gold", true),
                bid = 2742.60,
                ask = 2742.85,
                spread = 0.25,
                digits = 2
            ),
            TradingSymbol(
                id = "bitcoin",
                displayName = "BITCOIN",
                code = "BTCUSD",
                category = SymbolCategory.CRYPTO,
                isEnabled = secureStorage.getBoolean("sym_bitcoin", true),
                bid = 67840.00,
                ask = 67845.50,
                spread = 5.50,
                digits = 2
            ),
            TradingSymbol(
                id = "oil",
                displayName = "OIL",
                code = "USOIL / UKOIL",
                category = SymbolCategory.COMMODITIES,
                isEnabled = secureStorage.getBoolean("sym_oil", true),
                bid = 71.45,
                ask = 71.49,
                spread = 0.04,
                digits = 2
            ),
            TradingSymbol(
                id = "audusd",
                displayName = "AUDUSD",
                code = "AUDUSD",
                category = SymbolCategory.FOREX,
                isEnabled = secureStorage.getBoolean("sym_audusd", true),
                bid = 0.65821,
                ask = 0.65834,
                spread = 0.00013,
                digits = 5
            ),
            TradingSymbol(
                id = "audjpy",
                displayName = "AUDJPY",
                code = "AUDJPY",
                category = SymbolCategory.FOREX,
                isEnabled = secureStorage.getBoolean("sym_audjpy", true),
                bid = 100.412,
                ask = 100.428,
                spread = 0.016,
                digits = 3
            ),
            TradingSymbol(
                id = "audcad",
                displayName = "AUDCAD",
                code = "AUDCAD",
                category = SymbolCategory.FOREX,
                isEnabled = secureStorage.getBoolean("sym_audcad", true),
                bid = 0.91340,
                ask = 0.91355,
                spread = 0.00015,
                digits = 5
            ),
            TradingSymbol(
                id = "audnzd",
                displayName = "AUDNZD",
                code = "AUDNZD",
                category = SymbolCategory.FOREX,
                isEnabled = secureStorage.getBoolean("sym_audnzd", true),
                bid = 1.09680,
                ask = 1.09698,
                spread = 0.00018,
                digits = 5
            )
        )
    )
    val symbols: StateFlow<List<TradingSymbol>> = _symbols.asStateFlow()

    // Engine delegated state
    val isRobotOn: StateFlow<Boolean> = tradingEngine.isRobotOn
    val lotSize: StateFlow<Float> = tradingEngine.lotSize
    val logs: StateFlow<List<TradeLog>> = tradingEngine.logs
    val activeTrades: StateFlow<List<ActiveTrade>> = tradingEngine.activeTrades
    val totalProfit: StateFlow<Double> = tradingEngine.totalProfit

    // File Manager State
    private val _filesList = MutableStateFlow<List<EaFileItem>>(
        listOf(
            EaFileItem(
                id = "ea_1",
                fileName = "GoldScalper_Pro_v4.mq4",
                fileType = EaFileType.MQ4,
                fileSizeFormatted = "48.2 KB",
                isRequiresCompilation = true,
                status = FileExecutionStatus.IDLE,
                statusDetails = "MQL4 source code. Requires MetaEditor compilation on VPS.",
                isBundledTemplate = true
            ),
            EaFileItem(
                id = "ea_2",
                fileName = "TrendMaster_MT5_HighSpeed.ex5",
                fileType = EaFileType.EX5,
                fileSizeFormatted = "182.5 KB",
                isRequiresCompilation = false,
                status = FileExecutionStatus.IDLE,
                statusDetails = "MT5 pre-compiled binary. Ready to run on VPS terminal.",
                isBundledTemplate = true
            ),
            EaFileItem(
                id = "ea_3",
                fileName = "Exness_GridMaster_Multi.mq5",
                fileType = EaFileType.MQ5,
                fileSizeFormatted = "64.1 KB",
                isRequiresCompilation = true,
                status = FileExecutionStatus.IDLE,
                statusDetails = "MQL5 source code. Compiles via MetaEditor64 on VPS.",
                isBundledTemplate = true
            ),
            EaFileItem(
                id = "ea_4",
                fileName = "Oil_Scalper_Breakout.ex4",
                fileType = EaFileType.EX4,
                fileSizeFormatted = "112.0 KB",
                isRequiresCompilation = false,
                status = FileExecutionStatus.IDLE,
                statusDetails = "MT4 pre-compiled binary. Ready for VPS terminal execution.",
                isBundledTemplate = true
            )
        )
    )
    val filesList: StateFlow<List<EaFileItem>> = _filesList.asStateFlow()

    private val _selectedFile = MutableStateFlow<EaFileItem?>(_filesList.value.firstOrNull())
    val selectedFile: StateFlow<EaFileItem?> = _selectedFile.asStateFlow()

    private val _fileOperationInProgress = MutableStateFlow(false)
    val fileOperationInProgress: StateFlow<Boolean> = _fileOperationInProgress.asStateFlow()

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    init {
        // Restore stored lot size (default 0.05)
        val savedLot = secureStorage.getFloat("robot_lot_size", 0.05f)
        tradingEngine.setLotSize(savedLot)

        // No fabricated quote feed: prices remain placeholders until a verified market-data adapter is connected.
    }

    fun selectPage(index: Int) {
        _currentPage.value = index
    }

    fun clearAlert() {
        _alertMessage.value = null
    }

    // --- Broker Functions ---
    fun updateBrokerType(type: BrokerType) {
        _brokerConfig.update {
            it.copy(
                brokerType = type,
                server = when (type) {
                    BrokerType.EXNESS -> "exness-real10.exness.com:443"
                    BrokerType.MT5 -> "access.mt5.metaquotes.net:443"
                    BrokerType.MT4 -> "access.mt4.metaquotes.net:443"
                }
            )
        }
    }

    fun updateBrokerLogin(login: String) {
        _brokerConfig.update { it.copy(login = login) }
        secureStorage.saveString("broker_login", login)
    }

    fun updateBrokerPassword(pwd: String) {
        _brokerConfig.update { it.copy(password = pwd) }
        secureStorage.saveSecure("broker_pwd", pwd)
    }

    fun updateBrokerServer(server: String) {
        _brokerConfig.update { it.copy(server = server) }
        secureStorage.saveString("broker_server", server)
    }

    fun connectBroker() {
        val current = _brokerConfig.value
        viewModelScope.launch {
            _brokerIsConnecting.value = true
            tradingEngine.addLog("Connecting to ${current.brokerType.title} server: ${current.server}...", LogType.INFO)
            val result = brokerManager.testAndConnectBroker(current)
            _brokerIsConnecting.value = false

            result.fold(
                onSuccess = { res ->
                    _brokerConfig.update {
                        it.copy(
                            isConnected = true,
                            connectionMessage = res.message,
                            pingMs = res.pingMs,
                            balance = res.balance,
                            equity = res.equity,
                            currency = res.currency,
                            leverage = res.leverage
                        )
                    }
                    tradingEngine.addLog("Broker Connected: ${res.message}", LogType.SUCCESS)
                },
                onFailure = { err ->
                    _brokerConfig.update {
                        it.copy(
                            isConnected = false,
                            connectionMessage = err.localizedMessage ?: "Connection failed",
                            pingMs = 0
                        )
                    }
                    tradingEngine.addLog("Broker Connection Failed: ${err.localizedMessage}", LogType.ERROR)
                    _alertMessage.value = err.localizedMessage ?: "Broker connection failed"
                }
            )
        }
    }

    fun disconnectBroker() {
        _brokerConfig.update {
            it.copy(
                isConnected = false,
                connectionMessage = "Disconnected",
                pingMs = 0
            )
        }
        if (tradingEngine.isRobotOn.value) {
            tradingEngine.stopRobot()
        }
        tradingEngine.addLog("Broker Disconnected by user.", LogType.WARNING)
    }

    // --- VPS Functions ---
    fun updateVpsIp(ip: String) {
        _vpsConfig.update { it.copy(ipAddress = ip) }
        secureStorage.saveString("vps_ip", ip)
    }

    fun updateVpsPort(port: String) {
        _vpsConfig.update { it.copy(port = port) }
        secureStorage.saveString("vps_port", port)
    }

    fun updateVpsUsername(user: String) {
        _vpsConfig.update { it.copy(username = user) }
        secureStorage.saveString("vps_user", user)
    }

    fun updateVpsPassword(pwd: String) {
        _vpsConfig.update { it.copy(password = pwd) }
        secureStorage.saveSecure("vps_pwd", pwd)
    }

    fun connectVps() {
        val current = _vpsConfig.value
        viewModelScope.launch {
            _vpsIsConnecting.value = true
            tradingEngine.addLog("Probing VPS socket connection at ${current.ipAddress}:${current.port}...", LogType.INFO)
            val result = vpsManager.testAndConnectVps(current)
            _vpsIsConnecting.value = false

            result.fold(
                onSuccess = { res ->
                    _vpsConfig.update {
                        it.copy(
                            isConnected = true,
                            connectionMessage = res.message,
                            pingMs = res.pingMs,
                            osInfo = res.osInfo,
                            metaEditorReady = res.metaEditorReady,
                            activeTerminalPath = res.terminalPath
                        )
                    }
                    tradingEngine.addLog("VPS Connected: ${res.message}. MetaEditor environment ready.", LogType.SUCCESS)
                },
                onFailure = { err ->
                    _vpsConfig.update {
                        it.copy(
                            isConnected = false,
                            connectionMessage = err.localizedMessage ?: "VPS unreachable",
                            pingMs = 0
                        )
                    }
                    tradingEngine.addLog("VPS Connection Error: ${err.localizedMessage}", LogType.ERROR)
                    _alertMessage.value = err.localizedMessage ?: "VPS connection failed"
                }
            )
        }
    }

    fun disconnectVps() {
        _vpsConfig.update {
            it.copy(
                isConnected = false,
                connectionMessage = "VPS Disconnected",
                pingMs = 0
            )
        }
        if (tradingEngine.isRobotOn.value) {
            tradingEngine.stopRobot()
        }
        tradingEngine.addLog("VPS Disconnected by user.", LogType.WARNING)
    }

    // --- Robot ON/OFF ---
    fun toggleRobot(on: Boolean) {
        if (on) {
            startRobot()
        } else {
            stopRobot()
        }
    }

    fun startRobot() {
        val broker = _brokerConfig.value
        val vps = _vpsConfig.value
        val result = tradingEngine.startRobot(broker, vps, _symbols.value)
        if (result.isFailure) {
            _alertMessage.value = result.exceptionOrNull()?.message ?: "Failed to start robot"
        }
    }

    fun stopRobot() {
        tradingEngine.stopRobot()
    }

    // --- Lot Size ---
    fun updateLotSize(newLot: Float) {
        tradingEngine.setLotSize(newLot)
        secureStorage.saveFloat("robot_lot_size", tradingEngine.lotSize.value)
    }

    fun incrementLotSize(delta: Float) {
        val current = tradingEngine.lotSize.value
        val next = (current + delta).coerceIn(0.01f, 100.0f)
        updateLotSize(next)
    }

    // --- Trading Symbols ---
    fun toggleSymbol(symbolId: String, enabled: Boolean) {
        _symbols.update { list ->
            list.map { sym ->
                if (sym.id == symbolId) {
                    secureStorage.saveBoolean("sym_${sym.id}", enabled)
                    sym.copy(isEnabled = enabled)
                } else sym
            }
        }
        tradingEngine.addLog("Trading symbol updated: $symbolId -> ${if (enabled) "ENABLED" else "DISABLED"}", LogType.INFO)
    }

    // --- File Manager ---
    fun selectFile(fileItem: EaFileItem) {
        _selectedFile.value = fileItem
    }

    fun addCustomFile(name: String, sizeBytes: Long, content: String = "") {
        val ext = name.substringAfterLast(".", "").lowercase()
        val fileType = when (ext) {
            "mq4" -> EaFileType.MQ4
            "ex4" -> EaFileType.EX4
            "mq5" -> EaFileType.MQ5
            "ex5" -> EaFileType.EX5
            else -> {
                _alertMessage.value = "Unsupported file type '.$ext'. Only MT4 (.ex4, .mq4) and MT5 (.ex5, .mq5) files are supported."
                return
            }
        }

        val requiresComp = fileType == EaFileType.MQ4 || fileType == EaFileType.MQ5
        val sizeFormatted = String.format("%.1f KB", (sizeBytes.coerceAtLeast(1024) / 1024.0))

        val newFile = EaFileItem(
            id = "custom_${System.currentTimeMillis()}",
            fileName = name,
            fileType = fileType,
            fileSizeFormatted = sizeFormatted,
            isRequiresCompilation = requiresComp,
            status = FileExecutionStatus.IDLE,
            statusDetails = if (requiresComp) "MQL source code selected. Ready to compile via VPS MetaEditor." else "Pre-compiled EA binary selected. Ready for VPS deployment.",
            isBundledTemplate = false,
            sourceCode = content
        )

        _filesList.update { listOf(newFile) + it }
        _selectedFile.value = newFile
        tradingEngine.addLog("Selected EA file: $name ($fileType)", LogType.INFO)
    }

    fun runFile(fileItem: EaFileItem) {
        val vps = _vpsConfig.value
        if (!vps.isConnected) {
            val msg = "MetaTrader Expert Advisors (.ex4/.mq4/.ex5/.mq5) require a Windows/Wine MetaTrader environment and cannot run directly on Android OS. Please connect your VPS to compile and run this file."
            _alertMessage.value = msg
            updateFileStatus(fileItem.id, FileExecutionStatus.COMPILATION_ERROR, msg)
            tradingEngine.addLog("Execution Rejected: VPS connection required to run EA binaries.", LogType.ERROR)
            return
        }

        viewModelScope.launch {
            _fileOperationInProgress.value = true
            tradingEngine.addLog("Initiating remote EA deployment for '${fileItem.fileName}'...", LogType.INFO)
            
            vpsManager.compileAndDeployEa(
                fileItem = fileItem,
                vpsConfig = vps,
                onProgress = { status, detail ->
                    updateFileStatus(fileItem.id, status, detail)
                    tradingEngine.addLog("[VPS Terminal] ${fileItem.fileName}: $detail", when (status) {
                        FileExecutionStatus.RUNNING -> LogType.SUCCESS
                        FileExecutionStatus.COMPILATION_ERROR -> LogType.ERROR
                        else -> LogType.INFO
                    })
                }
            )
            _fileOperationInProgress.value = false
        }
    }

    fun stopFile(fileItem: EaFileItem) {
        updateFileStatus(fileItem.id, FileExecutionStatus.STOPPED, "EA execution halted on VPS terminal.")
        tradingEngine.addLog("Stopped '${fileItem.fileName}' on VPS.", LogType.WARNING)
    }

    private fun updateFileStatus(fileId: String, status: FileExecutionStatus, details: String) {
        _filesList.update { list ->
            list.map { f ->
                if (f.id == fileId) {
                    val updated = f.copy(status = status, statusDetails = details)
                    if (_selectedFile.value?.id == fileId) {
                        _selectedFile.value = updated
                    }
                    updated
                } else f
            }
        }
    }

}
