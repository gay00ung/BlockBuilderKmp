package net.lateinit.blockbuilder.kmp.utils

import java.security.MessageDigest
import java.util.UUID

actual object CryptoUtils {
    actual fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    actual fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}