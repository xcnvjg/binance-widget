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

            // 根据小组件实际宽高与数字长度，动态自适应计算最佳字号 (sp)，防止出现省略号
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            val dynamicSp = calculateOptimalTextSize(minWidth, minHeight, balance)
            views.setTextViewTextSize(R.id.tv_balance_number, android.util.TypedValue.COMPLEX_UNIT_SP, dynamicSp)

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
         * 动态自适应字号算法：根据组件实时分配的 dp 尺寸与字符串长度自适应缩放
         */
        private fun calculateOptimalTextSize(widthDp: Int, heightDp: Int, text: String): Float {
            val len = text.length.coerceAtLeast(1)
            // 若系统尚未上报真实尺寸，采用保守优雅的默认区间
            if (widthDp <= 0 || heightDp <= 0) {
                return when {
                    len >= 12 -> 13f
                    len >= 9 -> 16f
                    len >= 6 -> 20f
                    else -> 26f
                }
            }

            // 每个字符的平均宽度系数约为字号的 0.56 倍
            val maxSpForWidth = (widthDp * 0.94f) / (len * 0.56f)
            // 考虑高度约束，通常高度可用 60%
            val maxSpForHeight = heightDp * 0.60f

            val optimalSp = minOf(maxSpForWidth, maxSpForHeight)
            // 限制字号在 [11sp, 44sp] 之间，确保 1x1、2x1 也能完整显示纯数字
            return optimalSp.coerceIn(11f, 44f)
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val balance = PreferenceStorage(context).getLastBalance()
        updateAppWidget(context, appWidgetManager, appWidgetId, balance)
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
