package net.lateinit.blockbuilder.kmp.utils

import platform.Foundation.NSUUID

/**
 * iOS actual implementation using a pure Kotlin SHA-256.
 * This avoids fake hashes that trivially satisfy PoW (which made nonce always 1).
 */
actual object CryptoUtils {
    actual fun sha256(input: String): String {
        val bytes = input.encodeToByteArray()
        val digest = sha256Bytes(bytes)
        return toHex(digest)
    }

    actual fun generateUUID(): String = NSUUID().UUIDString

    // --- SHA-256 (pure Kotlin) ---
    private fun toHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        var i = 0
        var j = 0
        while (i < bytes.size) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[j++] = hexArray[v ushr 4]
            hexChars[j++] = hexArray[v and 0x0F]
            i++
        }
        return hexChars.concatToString()
    }

    private fun sha256Bytes(message: ByteArray): ByteArray {
        // SHA-256 constants
        val k = intArrayOf(
            0x428a2f98, 0x71374491, 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(), 0x3956c25b,
            0x59f111f1, 0x923f82a4.toInt(), 0xab1c5ed5.toInt(), 0xd807aa98.toInt(), 0x12835b01,
            0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
            0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa,
            0x5cb0a9dc, 0x76f988da, 0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(),
            0xbf597fc7.toInt(), 0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e.toInt(),
            0x92722c85.toInt(), 0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
            0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070, 0x19a4c116, 0x1e376c08,
            0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f,
            0x84c87814.toInt(), 0x8cc70208.toInt(), 0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
        )

        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        // Padding
        val bitLen = message.size.toLong() * 8L
        val withOne = message + byteArrayOf(0x80.toByte())
        var padLen = (56 - (withOne.size % 64) + 64) % 64
        val padding = ByteArray(padLen)
        val lengthBytes = ByteArray(8)
        for (i in 0 until 8) {
            lengthBytes[7 - i] = ((bitLen ushr (8 * i)) and 0xFF).toByte()
        }
        val padded = withOne + padding + lengthBytes

        // Process each 512-bit chunk
        val w = IntArray(64)
        var offset = 0
        while (offset < padded.size) {
            // Prepare message schedule w[0..63]
            var i = 0
            while (i < 16) {
                val j = offset + i * 4
                w[i] = ((padded[j].toInt() and 0xFF) shl 24) or
                        ((padded[j + 1].toInt() and 0xFF) shl 16) or
                        ((padded[j + 2].toInt() and 0xFF) shl 8) or
                        (padded[j + 3].toInt() and 0xFF)
                i++
            }
            while (i < 64) {
                val s0 = (w[i - 15].rotateRight(7)) xor (w[i - 15].rotateRight(18)) xor (w[i - 15] ushr 3)
                val s1 = (w[i - 2].rotateRight(17)) xor (w[i - 2].rotateRight(19)) xor (w[i - 2] ushr 10)
                w[i] = (w[i - 16] + s0 + w[i - 7] + s1)
                i++
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (t in 0 until 64) {
                val S1 = (e.rotateRight(6)) xor (e.rotateRight(11)) xor (e.rotateRight(25))
                val ch = (e and f) xor ((e.inv()) and g)
                val temp1 = h + S1 + ch + k[t] + w[t]
                val S0 = (a.rotateRight(2)) xor (a.rotateRight(13)) xor (a.rotateRight(22))
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = S0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h

            offset += 64
        }

        val out = ByteArray(32)
        fun writeInt(value: Int, pos: Int) {
            out[pos] = ((value ushr 24) and 0xFF).toByte()
            out[pos + 1] = ((value ushr 16) and 0xFF).toByte()
            out[pos + 2] = ((value ushr 8) and 0xFF).toByte()
            out[pos + 3] = (value and 0xFF).toByte()
        }

        writeInt(h0, 0)
        writeInt(h1, 4)
        writeInt(h2, 8)
        writeInt(h3, 12)
        writeInt(h4, 16)
        writeInt(h5, 20)
        writeInt(h6, 24)
        writeInt(h7, 28)
        return out
    }

    private fun Int.rotateRight(bits: Int): Int = (this ushr bits) or (this shl (32 - bits))
}
