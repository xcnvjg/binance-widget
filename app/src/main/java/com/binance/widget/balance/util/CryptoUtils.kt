package com.binance.widget.balance.util

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val HMAC_SHA256 = "HmacSHA256"

    /**
     * 对请求参数计算 HMAC-SHA256 签名，输出十六进制字符串
     */
    fun hmacSha256(data: String, secret: String): String {
        val secretKeySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256)
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(hash)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}
