package net.lateinit.blockbuilder.kmp

import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    println("[BlockBuilder] wasm main() starting…")
    try {
        val root = (document.getElementById("root") ?: document.body) as HTMLElement
        ComposeViewport(root) {
            println("[BlockBuilder] compose content start")
            val result = kotlin.runCatching { App() }
            if (result.isFailure) {
                val e = result.exceptionOrNull()
                println("[BlockBuilder] compose content exception: ${'$'}e")
                Text("Startup failed: ${'$'}e")
            }
            println("[BlockBuilder] compose content end")
        }
    } catch (e: Throwable) {
        println("[BlockBuilder] main() exception before compose: ${'$'}e")
        document.body?.append("Startup error: ${'$'}e")
        throw e
    }
}
