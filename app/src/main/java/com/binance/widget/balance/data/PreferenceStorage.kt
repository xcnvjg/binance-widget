package com.binance.widget.balance.data

import android.content.Context
import android.content.SharedPreferences
import com.binance.widget.balance.model.IWidgetPreferences

class PreferenceStorage(context: Context) : IWidgetPreferences {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("binance_widget_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SECRET_KEY = "secret_key"
        private const val KEY_LAST_BALANCE = "last_balance"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval_minutes"
        private const val DEFAULT_INTERVAL = 15 // 默认15分钟
    }

    override fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    override fun getSecretKey(): String {
        return prefs.getString(KEY_SECRET_KEY, "") ?: ""
    }

    override fun saveCredentials(apiKey: String, secretKey: String) {
        prefs.edit()
            .putString(KEY_API_KEY, apiKey.trim())
            .putString(KEY_SECRET_KEY, secretKey.trim())
            .apply()
    }

    override fun getLastBalance(): String {
        return prefs.getString(KEY_LAST_BALANCE, "---") ?: "---"
    }

    override fun saveLastBalance(balance: String) {
        prefs.edit().putString(KEY_LAST_BALANCE, balance).apply()
    }

    override fun getRefreshIntervalMinutes(): Int {
        return prefs.getInt(KEY_REFRESH_INTERVAL, DEFAULT_INTERVAL)
    }

    override fun saveRefreshIntervalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_REFRESH_INTERVAL, minutes).apply()
    }
}
