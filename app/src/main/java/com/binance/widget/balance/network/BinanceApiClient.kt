package com.binance.widget.balance.network

import com.binance.widget.balance.model.AssetOverview
import com.binance.widget.balance.model.IBinanceRepository
import com.binance.widget.balance.model.WalletBalanceItem
import com.binance.widget.balance.util.CryptoUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class BinanceApiClient : IBinanceRepository {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val gson = Gson()
    private val numberFormatter = DecimalFormat("#,##0.00")

    // 币安多可用域名列表，增强国内及多网络环境下的连通性
    private val apiHosts = listOf(
        "https://api.binance.com",
        "https://api1.binance.com",
        "https://api2.binance.com",
        "https://api3.binance.com",
        "https://api4.binance.com"
    )

    override suspend fun fetchTotalAssetUsdt(apiKey: String, secretKey: String): AssetOverview =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank() || secretKey.isBlank()) {
                return@withContext AssetOverview(
                    totalUsdt = 0.0,
                    displayString = "---",
                    timestamp = System.currentTimeMillis(),
                    isSuccess = false,
                    errorMessage = "请先在应用内配置 API Key 和 Secret"
                )
            }

            var lastError = "网络请求失败"
            for (host in apiHosts) {
                try {
                    // 1. 获取 BTC/USDT 现货价格用于将 BTC 钱包估值转换为 USDT
                    val btcPrice = fetchBtcPrice(host)

                    // 2. 调用 /sapi/v1/asset/wallet/balance 获取各钱包 BTC 估值
                    val timestamp = System.currentTimeMillis()
                    val queryString = "timestamp=$timestamp&recvWindow=60000"
                    val signature = CryptoUtils.hmacSha256(queryString, secretKey)
                    val fullUrl = "$host/sapi/v1/asset/wallet/balance?$queryString&signature=$signature"

                    val request = Request.Builder()
                        .url(fullUrl)
                        .addHeader("X-MBX-APIKEY", apiKey)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val type = object : TypeToken<List<WalletBalanceItem>>() {}.type
                        val walletList: List<WalletBalanceItem> = gson.fromJson(responseBody, type)

                        var totalBtc = 0.0
                        for (item in walletList) {
                            val b = item.balance.toDoubleOrNull() ?: 0.0
                            totalBtc += b
                        }

                        val totalUsdt = totalBtc * btcPrice
                        val displayStr = numberFormatter.format(totalUsdt)

                        return@withContext AssetOverview(
                            totalUsdt = totalUsdt,
                            displayString = displayStr,
                            timestamp = System.currentTimeMillis(),
                            isSuccess = true
                        )
                    } else {
                        val json = try { JSONObject(responseBody) } catch (e: Exception) { null }
                        val msg = json?.optString("msg") ?: "HTTP ${response.code}"
                        lastError = "币安接口错误: $msg"
                    }
                } catch (e: Exception) {
                    lastError = "网络连接异常: ${e.localizedMessage ?: "timeout"}"
                }
            }

            AssetOverview(
                totalUsdt = 0.0,
                displayString = "---",
                timestamp = System.currentTimeMillis(),
                isSuccess = false,
                errorMessage = lastError
            )
        }

    private fun fetchBtcPrice(host: String): Double {
        val priceUrl = "$host/api/v3/ticker/price?symbol=BTCUSDT"
        val request = Request.Builder().url(priceUrl).get().build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            return json.optString("price", "60000.0").toDoubleOrNull() ?: 60000.0
        }
        return 60000.0
    }
}
