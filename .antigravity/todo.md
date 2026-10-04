# Binance Balance Widget 开发任务清单

- [x] 1. 初始化项目骨架与 Gradle 构建配置 (app/build.gradle.kts, settings.gradle.kts, AndroidManifest.xml)
- [x] 2. 契约模型与安全存储实现 (Contracts, PreferenceStorage, CryptoUtils)
- [x] 3. 币安 API 客户端与资产计算逻辑 (BinanceApiClient: HMAC-SHA256 签名, /sapi/v1/asset/wallet/balance 与兜底汇率解析)
- [x] 4. 桌面小组件布局设计与自适应纯数字展示 (appwidget-provider xml, widget_pure_number.xml 自由缩放/纯数字居中)
- [x] 5. AppWidgetProvider 与后台刷新服务 (WidgetProvider, RefreshWorker 定时轮询与点击即时刷新)
- [x] 6. 主配置页面 UI (MainActivity: 密钥输入、测试连接、查看当前余额、保存配置)
- [x] 7. GitHub Actions 自动化构建脚本与本地一键 APK 构建脚本编写
- [ ] 8. 验证构建、清理与交付 (APK 打包与手机安装指导)
