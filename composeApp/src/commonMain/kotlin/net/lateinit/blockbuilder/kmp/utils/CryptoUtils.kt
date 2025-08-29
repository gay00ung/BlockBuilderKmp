package net.lateinit.blockbuilder.kmp.utils

import kotlin.random.Random

/**
 * Multiplatform crypto utilities
 */
expect object CryptoUtils {
    fun sha256(input: String): String
    fun generateUUID(): String
}

/**
 * Common random number generator
 */
fun generateRandomId(): String {
    val chars = "0123456789abcdef"
    return buildString {
        repeat(32) {
            append(chars[Random.nextInt(chars.length)])
        }
    }
}