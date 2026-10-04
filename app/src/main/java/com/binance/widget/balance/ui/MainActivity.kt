package com.binance.widget.balance.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.binance.widget.balance.R
import com.binance.widget.balance.data.PreferenceStorage
import com.binance.widget.balance.databinding.ActivityMainBinding
import com.binance.widget.balance.network.BinanceApiClient
import com.binance.widget.balance.widget.BinanceBalanceWidgetProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceStorage: PreferenceStorage
    private val apiClient = BinanceApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceStorage = PreferenceStorage(this)
        initViews()
    }

    private fun initViews() {
        val savedApiKey = preferenceStorage.getApiKey()
        val savedSecret = preferenceStorage.getSecretKey()
        val lastBalance = preferenceStorage.getLastBalance()

        if (savedApiKey.isNotEmpty()) {
            binding.etApiKey.setText(savedApiKey)
        }
        if (savedSecret.isNotEmpty()) {
            binding.etApiSecret.setText(savedSecret)
        }
        binding.tvPreviewBalance.text = lastBalance

        binding.btnSaveAndTest.setOnClickListener {
            val key = binding.etApiKey.text?.toString()?.trim() ?: ""
            val secret = binding.etApiSecret.text?.toString()?.trim() ?: ""

            if (key.isEmpty() || secret.isEmpty()) {
                Toast.makeText(this, "请输入 API Key 和 Secret Key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performTestAndSave(key, secret)
        }
    }

    private fun performTestAndSave(key: String, secret: String) {
        binding.btnSaveAndTest.isEnabled = false
        binding.tvStatusMessage.text = "正在连接币安 API 获取资产..."
        binding.tvStatusMessage.setTextColor(getColor(R.color.binance_yellow))

        lifecycleScope.launch {
            val overview = apiClient.fetchTotalAssetUsdt(key, secret)
            binding.btnSaveAndTest.isEnabled = true

            if (overview.isSuccess) {
                // 保存配置
                preferenceStorage.saveCredentials(key, secret)
                preferenceStorage.saveLastBalance(overview.displayString)

                binding.tvPreviewBalance.text = overview.displayString
                binding.tvStatusMessage.text = "✅ 验证成功并已保存！桌面小组件已同步。"
                binding.tvStatusMessage.setTextColor(getColor(R.color.binance_yellow))

                // 同步刷新小组件并启动后台轮询
                BinanceBalanceWidgetProvider.updateAllWidgets(this@MainActivity, overview.displayString)
                BinanceBalanceWidgetProvider.startPeriodicRefresh(this@MainActivity)

                Toast.makeText(this@MainActivity, "保存成功", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvStatusMessage.text = "❌ 失败: ${overview.errorMessage}"
                binding.tvStatusMessage.setTextColor(getColor(android.R.color.holo_red_light))
                Toast.makeText(this@MainActivity, "验证失败，请检查密钥或网络", Toast.LENGTH_LONG).show()
            }
        }
    }
}
