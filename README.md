# 币安账户总资产实时小组件 (Binance Balance Widget)

专为 **一加 Ace 2 (Android 16 / ColorOS)** 深度定制的极简桌面小组件。

---

## 🌟 核心特性

1. **极致极简**：
   - 桌面只显示纯资产数字（单位 USDT，例：`12,345.67`），无任何图标、标签、多余边框干扰。
   - 白色高辨识度粗体字，自带微立体投影，无论浅色、深色还是彩色壁纸都能清晰阅读。
2. **尺寸自由缩放**：
   - 支持系统级自由拉伸（`resizeMode="horizontal|vertical"`），可随意调整为 1x1、2x1、3x1、2x2、4x1 等任意尺寸。
   - 采用 Android 原生自适应动态字号（Auto-sizing TextView），放大缩小组件时数字平滑缩放、永不换行。
3. **实时与即刻刷新**：
   - **后台自动轮询**：基于 AndroidX WorkManager 调度，默认每 15 分钟后台静默更新。
   - **点击秒级刷新**：手指轻点桌面上的数字，立即发起实时拉取并在 1 秒内同步更新。
4. **资金绝对安全**：
   - 币安 API Key / Secret **仅保存在手机本地 SharedPreferences**，绝不上报任何第三方服务器。
   - 仅需在币安后台开通**只读权限**（切勿开启交易、提现或划转权限）。

---

## 🚀 手机获取并安装 APK 的两种方式

### 方式一：GitHub Actions 云端自动构建（强烈推荐，手机直接下载）

本工程已完整配置好了 [`.github/workflows/build-apk.yml`](file:///.github/workflows/build-apk.yml)。你无需在电脑上配置庞大的 Android 开发环境：

1. 在 GitHub 上新建一个仓库（公开或私有均可，例如 `binance-widget`）。
2. 在本项目根目录下执行以下命令推送代码：
   ```bash
   git init
   git add .
   git commit -m "init binance balance widget"
   git branch -M main
   git remote add origin https://github.com/你的用户名/你的仓库名.git
   git push -u origin main
   ```
3. 打开 GitHub 仓库页面，点击顶部的 **Actions** 标签页，你会看到自动运行的 `Build Binance Widget APK` 任务。
4. 约 1~2 分钟构建完成后，在页面下方的 **Artifacts** 处或 **Releases** 处，直接用一加手机浏览器点击下载 `BinanceBalanceWidget-apk`（解压即可得到 `app-debug.apk`）进行安装！

---

### 方式二：电脑 Android Studio 本地打包

1. 启动 **Android Studio**，选择 `Open` 并选中本项目根目录。
2. 等待 Gradle 同步完成，点击顶部菜单：
   `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
3. 编译完成后，点击右下角提示中的 `locate`，即可找到 `app-debug.apk`。
4. 通过微信、QQ、USB 数据线或微信传输助手发送至一加 Ace 2 手机安装。

---

## 📱 一加 Ace 2 (ColorOS / Android 16) 配置与使用指南

### 第一步：配置币安 API
1. 安装并打开 **币安总资产小组件** App。
2. 登录币安官网或 App，进入【个人中心】->【API 管理】->【创建 API】（选“系统生成”）。
3. **安全注意**：权限仅勾选默认的【只读】（Enable Reading），**严禁勾选“提现”、“开启现货及杠杆交易”、“开启合约”**。
4. 将生成的 `API Key` 和 `Secret Key` 填入 App。
5. 点击【保存并测试拉取】，若显示 `✅ 验证成功并已保存！` 即表示连接成功。

### 第二步：添加与调节桌面小组件
1. 返回一加手机桌面，**双指捏合**或**长按桌面空白处**进入桌面编辑模式。
2. 点击底部的【微件 / 插件 / 桌面小组件】。
3. 在应用列表中找到【币安总资产小组件】，拖动小组件到桌面上。
4. **调节大小**：长按桌面上的资产数字，周围会出现调节边框，拖动四边的圆点即可随心所欲拉伸大小，数字会自动按比例缩放适配。
5. **即时刷新**：随时轻点一下桌面数字，即可立即触发最新资产拉取！

### 第三步：ColorOS 后台保活建议（确保后台定期自动刷新）
由于 ColorOS 对后台进程管控较严格，为了保证每 15 分钟后台能自动更新资产：
1. 长按桌面的应用图标 -> 点击【应用信息】。
2. 进入【耗电管理 / 电池】-> 开启【允许完全后台行为】。
3. 在【应用自启动】中允许自启动与关联启动。
