package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client for the user's own HTTPS bridge. The MetaApi secret token must remain on that server.
 * Do not ship a shared bridge API key inside a public APK.
 */
class MetaApiBridgeClient(
    private val baseUrl: String,
    private val bridgeKey: String,
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private suspend fun request(path: String, method: String = "GET", body: JSONObject? = null): JSONObject =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://")) { "Bridge URL must use HTTPS." }
            require(bridgeKey.isNotBlank()) { "Bridge authentication is not configured." }
            val builder = Request.Builder()
                .url(baseUrl.trimEnd('/') + path)
                .header("x-bridge-key", bridgeKey)
                .header("Accept", "application/json")
            when (method) {
                "POST" -> builder.post((body ?: JSONObject()).toString().toRequestBody(jsonType))
                else -> builder.get()
            }
            http.newCall(builder.build()).execute().use { response ->
                val payload = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw IOException("Bridge HTTP ${response.code}: $payload")
                if (payload.isBlank()) JSONObject() else JSONObject(payload)
            }
        }

    suspend fun createMt5Account(login: String, password: String, server: String): JSONObject =
        request("/api/accounts", "POST", JSONObject().apply {
            put("login", login)
            put("password", password)
            put("server", server)
            put("platform", "mt5")
        })

    suspend fun deployAccount(accountId: String): JSONObject =
        request("/api/accounts/${safeId(accountId)}/deploy", "POST")

    suspend fun getConnectionStatus(accountId: String): JSONObject =
        request("/api/accounts/${safeId(accountId)}/status")

    suspend fun getAccountInformation(accountId: String): JSONObject =
        request("/api/accounts/${safeId(accountId)}/account-information")

    suspend fun getPositions(accountId: String): JSONObject =
        request("/api/accounts/${safeId(accountId)}/positions")

    suspend fun openMarketOrder(
        accountId: String,
        side: String,
        symbol: String,
        lot: Double,
        stopLoss: Double? = null,
        takeProfit: Double? = null
    ): JSONObject {
        require(side == "BUY" || side == "SELL") { "Side must be BUY or SELL." }
        require(lot in listOf(0.01, 0.02, 0.03, 0.05)) { "Lot is outside configured grid ladder." }
        val body = JSONObject().apply {
            put("side", side)
            put("symbol", symbol)
            put("volume", lot)
            stopLoss?.takeIf { it > 0 }?.let { put("stopLoss", it) }
            takeProfit?.takeIf { it > 0 }?.let { put("takeProfit", it) }
        }
        return request("/api/accounts/${safeId(accountId)}/trades", "POST", body)
    }

    suspend fun closePosition(accountId: String, positionId: String): JSONObject =
        request("/api/accounts/${safeId(accountId)}/positions/${safeId(positionId)}/close", "POST")

    private fun safeId(value: String): String {
        require(value.matches(Regex("[A-Za-z0-9-]{1,80}"))) { "Invalid identifier." }
        return value
    }
}
