# 币安模型与网络类保留
-keep class com.binance.widget.balance.model.** { *; }
-keep class com.binance.widget.balance.widget.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
