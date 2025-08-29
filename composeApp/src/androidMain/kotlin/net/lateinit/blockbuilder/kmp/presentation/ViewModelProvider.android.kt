package net.lateinit.blockbuilder.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun viewModel(): BlockchainViewModel {
    // Simpler, lifecycle-free VM for Web compatibility
    return remember { BlockchainViewModel() }
}
