package com.binance.widget.balance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.binance.widget.balance.R
import com.binance.widget.balance.data.PreferenceStorage
import com.binance.widget.balance.worker.RefreshWorker
import java.util.concurrent.TimeUnit

class BinanceBalanceWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_MANUAL_REFRESH = "com.binance.widget.ACTION_MANUAL_REFRESH"
        private const val WORK_NAME_PERIODIC = "binance_periodic_refresh"
        private const val WORK_NAME_ONE_TIME = "binance_one_time_refresh"

        /**
         * 更新桌面上所有该插件的视图
         */
        fun updateAllWidgets(context: Context, balanceText: String? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BinanceBalanceWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            val display = balanceText ?: PreferenceStorage(context).getLastBalance()
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, display)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            balance: String
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_pure_number)
            views.setTextViewText(R.id.tv_balance_number, balance)

            // 设置点击小组件即可立即刷新
            val intent = Intent(context, BinanceBalanceWidgetProvider::class.java).apply {
                action = ACTION_MANUAL_REFRESH
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * 启动周期性后台刷新任务
         */
        fun startPeriodicRefresh(context: Context) {
            val prefs = PreferenceStorage(context)
            val interval = prefs.getRefreshIntervalMinutes().coerceAtLeast(15).toLong()
            val periodicRequest = PeriodicWorkRequestBuilder<RefreshWorker>(
                interval, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        /**
         * 停止周期性后台刷新
         */
        fun stopPeriodicRefresh(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
        }

        /**
         * 立即执行一次刷新
         */
        fun triggerInstantRefresh(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val balance = PreferenceStorage(context).getLastBalance()
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, balance)
        }
        triggerInstantRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MANUAL_REFRESH) {
            triggerInstantRefresh(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startPeriodicRefresh(context)
        triggerInstantRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopPeriodicRefresh(context)
    }
}
