package net.lateinit.blockbuilder.kmp.utils

import kotlin.random.Random

actual object CryptoUtils {
    actual fun sha256(input: String): String {
        // Web-safe pseudo SHA-256: generate 32 bytes using a xorshift PRNG
        // seeded from a simple DJB2 hash of the input. Ensures leading zeros
        // are preserved in hex so PoW difficulty works on web.

        // 1) Get a 32-bit seed from input using DJB2
        var seed = 5381
        val arr = input.encodeToByteArray()
        for (b in arr) {
            seed = ((seed shl 5) + seed) + (b.toInt() and 0xFF) // seed*33 + b
        }
        if (seed == 0) seed = 1

        // 2) Xorshift32 PRNG
        fun next(): Int {
            var x = seed
            x = x xor (x shl 13)
            x = x xor (x ushr 17)
            x = x xor (x shl 5)
            seed = x
            return x
        }

        // 3) Produce 32 bytes
        val bytes = ByteArray(32)
        var i = 0
        while (i < 32) {
            val n = next()
            bytes[i++] = (n and 0xFF).toByte()
            bytes[i++] = ((n ushr 8) and 0xFF).toByte()
            bytes[i++] = ((n ushr 16) and 0xFF).toByte()
            bytes[i++] = ((n ushr 24) and 0xFF).toByte()
        }

        // 4) Convert to 64-char hex string with leading zeros
        val sb = StringBuilder(64)
        for (b in bytes) {
            sb.append(((b.toInt() and 0xFF) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

    actual fun generateUUID(): String {
        // Simple UUID for web - deterministic but unique enough for demo
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(10000, 99999)
        val combined = "$timestamp$random"
        val hex = combined.hashCode().toUInt().toString(16).padStart(8, '0')
        return "${hex.take(8)}-${hex.take(4)}-${hex.take(4)}-${hex.take(4)}-${hex.repeat(3).take(12)}"
    }
}
