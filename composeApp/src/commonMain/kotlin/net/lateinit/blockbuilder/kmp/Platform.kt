package net.lateinit.blockbuilder.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform