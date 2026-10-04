package com.binance.widget.balance.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.binance.widget.balance.data.PreferenceStorage
import com.binance.widget.balance.network.BinanceApiClient
import com.binance.widget.balance.widget.BinanceBalanceWidgetProvider

class RefreshWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PreferenceStorage(context)
        val apiKey = prefs.getApiKey()
        val secretKey = prefs.getSecretKey()

        if (apiKey.isBlank() || secretKey.isBlank()) {
            return Result.success()
        }

        val apiClient = BinanceApiClient()
        val result = apiClient.fetchTotalAssetUsdt(apiKey, secretKey)

        if (result.isSuccess) {
            prefs.saveLastBalance(result.displayString)
            BinanceBalanceWidgetProvider.updateAllWidgets(context, result.displayString)
            return Result.success()
        } else {
            return Result.retry()
        }
    }
}
