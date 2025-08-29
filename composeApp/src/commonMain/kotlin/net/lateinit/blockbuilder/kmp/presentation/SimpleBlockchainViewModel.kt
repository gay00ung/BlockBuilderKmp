package net.lateinit.blockbuilder.kmp.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lateinit.blockbuilder.kmp.data.Blockchain
import net.lateinit.blockbuilder.kmp.data.Transaction
import net.lateinit.blockbuilder.kmp.data.Wallet

class SimpleBlockchainViewModel : BlockchainViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var autoMiningJob: Job? = null

    override fun mineBlock() {
        errorMessage.value = null
        if (wallets.isNotEmpty()) {
            scope.launch {
                miningInProgress.value = true
                try {
                    // 웹에서는 Default 디스패처가 없을 수 있으니 직접 처리
                    performMining(wallets.first().address)
                } catch (e: Exception) {
                    // 에러 발생 시 기본 동작
                    miningInProgress.value = false
                }
                miningInProgress.value = false
            }
        }
    }

    override fun toggleAutoMining() {
        isAutoMining.value = !isAutoMining.value
        if (isAutoMining.value) {
            startAutoMining()
        } else {
            stopAutoMining()
        }
    }

    private fun startAutoMining() {
        autoMiningJob = scope.launch {
            while (isAutoMining.value) {
                if (pendingTransactionsState.value.isNotEmpty()) {
                    mineBlock()
                }
                delay(3000)
            }
        }
    }

    private fun stopAutoMining() {
        autoMiningJob?.cancel()
        autoMiningJob = null
        miningInProgress.value = false
    }
}
