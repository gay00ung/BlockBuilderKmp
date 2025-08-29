package net.lateinit.blockbuilder.kmp.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.lateinit.blockbuilder.kmp.data.Blockchain
import net.lateinit.blockbuilder.kmp.data.Transaction
import net.lateinit.blockbuilder.kmp.data.Wallet
import net.lateinit.blockbuilder.kmp.getPlatform

open class BlockchainViewModel {
    protected val blockchain = Blockchain()
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(job)

    open val wallets = mutableStateListOf<Wallet>()
    open val chainState = mutableStateOf(blockchain.chain.toList())
    open val pendingTransactionsState = mutableStateOf(blockchain.pendingTransactions.toList())
    open val miningInProgress = mutableStateOf(false)
    open val balances = mutableStateOf<Map<String, Int>>(emptyMap())
    open val errorMessage = mutableStateOf<String?>(null)
    open val isAutoMining = mutableStateOf(false)
    private var autoMiningJob: Job? = null
    open val difficulty = mutableStateOf(blockchain.difficulty)

    init {
        println("[BlockBuilder] BlockchainViewModel init")
        // Lower difficulty on web to keep Wasm responsive
        runCatching {
            if (getPlatform().name.contains("Web", ignoreCase = true)) {
                blockchain.difficulty = 1
            }
        }
        createWallet()
        createWallet()
        // On web, skip heavy initial mining to avoid startup exceptions
        val isWeb = runCatching { getPlatform().name.contains("Web", ignoreCase = true) }.getOrDefault(false)
        if (!isWeb) {
            distributeInitialCoins()
        } else {
            updateBalances()
        }
    }

    private fun distributeInitialCoins() {
        println("[BlockBuilder] distributeInitialCoins start, wallets=${'$'}{wallets.size}")
        if (wallets.isNotEmpty()) {
            wallets.forEach { wallet ->
                val amount = (100..500).random()
                blockchain.createTransaction(Transaction("System", wallet.address, amount))
            }
            blockchain.minePendingTransactions(wallets.first().address)

            chainState.value = blockchain.chain.toList()
            pendingTransactionsState.value = blockchain.pendingTransactions.toList()
            updateBalances()
            println("[BlockBuilder] distributeInitialCoins done, chainSize=${'$'}{chainState.value.size}")
        }
    }

    open fun createWallet() {
        errorMessage.value = null
        wallets.add(Wallet())
        updateBalances()
    }

    open fun createTransaction(from: String, to: String, amount: Int) {
        errorMessage.value = null
        val fromBalance = balances.value[from] ?: 0
        if (fromBalance >= amount) {
            val transaction = Transaction(from, to, amount)
            blockchain.createTransaction(transaction)
            pendingTransactionsState.value = blockchain.pendingTransactions.toList()
        } else {
            errorMessage.value = "잔액이 부족합니다! 먼저 채굴을 통해 보상을 받아야 합니다."
        }
    }

    open fun mineBlock() {
        errorMessage.value = null
        if (wallets.isNotEmpty()) {
            scope.launch {
                miningInProgress.value = true
                println("[BlockBuilder] performMining via ViewModel.mineBlock")
                performMining(wallets.first().address)
                miningInProgress.value = false
            }
        }
    }

    protected fun performMining(minerAddress: String) {
        println("[BlockBuilder] performMining start for $minerAddress with pending=${'$'}{blockchain.pendingTransactions.size}")
        blockchain.minePendingTransactions(minerAddress)
        chainState.value = blockchain.chain.toList()
        pendingTransactionsState.value = blockchain.pendingTransactions.toList()
        updateBalances()
        println("[BlockBuilder] performMining done, newChainSize=${'$'}{chainState.value.size}")
    }

    open fun toggleAutoMining() {
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

    open fun onDifficultyChange(newDifficulty: String) {
        val difficultyValue = newDifficulty.toIntOrNull() ?: return
        difficulty.value = difficultyValue
        blockchain.difficulty = difficultyValue
    }

    private fun updateBalances() {
        val newBalances = wallets.associate { wallet ->
            wallet.address to blockchain.getBalanceOfAddress(wallet.address)
        }
        balances.value = newBalances
    }
}
