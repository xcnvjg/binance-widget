# Lessons Learned

- [Android16/ColorOS] 小组件自由拉伸时，必须在 appwidget-provider 配置 resizeMode="horizontal|vertical" 并配合 autoSizeTextType="uniform" 才能防止极端尺寸下文字换行或溢出。
- [Binance API] /sapi/v1/asset/wallet/balance 汇总各钱包资产为 BTC 估值，需联动现货价格折算为 USDT 才能保证与 App 资产估值完全一致。
