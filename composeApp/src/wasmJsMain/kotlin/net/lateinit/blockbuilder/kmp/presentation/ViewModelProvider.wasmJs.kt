package net.lateinit.blockbuilder.kmp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun viewModel(): BlockchainViewModel {
    return remember { BlockchainViewModel() }
}
