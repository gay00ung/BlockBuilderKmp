package net.lateinit.blockbuilder.kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import net.lateinit.blockbuilder.kmp.presentation.viewModel
import net.lateinit.blockbuilder.kmp.presentation.ui.screen.BlockBuilderScreen

@Composable
fun App() {
    MaterialTheme {
        val blockchainViewModel = viewModel()
        BlockBuilderScreen(blockchainViewModel)
    }
}
