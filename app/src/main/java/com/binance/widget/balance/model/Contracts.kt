package com.binance.widget.balance.model

import com.google.gson.annotations.SerializedName

/**
 * FROZEN: 钱包各模块估值数据模型
 */
data class WalletBalanceItem(
    @SerializedName("activate")
    val activate: Boolean = true,
    @SerializedName("balance")
    val balance: String = "0", // 折算资产
    @SerializedName("walletName")
    val walletName: String = "" // spot, funding, futures, etc.
)

/**
 * FROZEN: 资产总览聚合结果
 */
data class AssetOverview(
    val totalUsdt: Double,
    val displayString: String, // 纯数字，如 "12345.67"
    val timestamp: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

/**
 * 币安 API 契约接口
 */
interface IBinanceRepository {
    suspend fun fetchTotalAssetUsdt(apiKey: String, secretKey: String): AssetOverview
}

/**
 * 本地配置存储契约接口
 */
interface IWidgetPreferences {
    fun getApiKey(): String
    fun getSecretKey(): String
    fun saveCredentials(apiKey: String, secretKey: String)
    fun getLastBalance(): String
    fun saveLastBalance(balance: String)
    fun getRefreshIntervalMinutes(): Int
    fun saveRefreshIntervalMinutes(minutes: Int)
}
