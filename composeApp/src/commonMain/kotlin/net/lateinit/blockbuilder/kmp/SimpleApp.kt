package net.lateinit.blockbuilder.kmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.lateinit.blockbuilder.kmp.data.Blockchain
import net.lateinit.blockbuilder.kmp.data.Transaction
import net.lateinit.blockbuilder.kmp.data.Wallet

@Composable
fun SimpleApp() {
    MaterialTheme {
        val blockchain = remember { Blockchain() }
        val wallets = remember { mutableStateListOf<Wallet>() }
        var miningInProgress by remember { mutableStateOf(false) }
        
        // 초기 지갑 생성
        LaunchedEffect(Unit) {
            repeat(2) { wallets.add(Wallet()) }
            // 초기 코인 분배
            wallets.forEach { wallet ->
                blockchain.createTransaction(Transaction("System", wallet.address, (100..500).random()))
            }
            blockchain.minePendingTransactions(wallets.first().address)
        }

        Scaffold { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "🔗 BlockBuilder - 블록체인 시뮬레이터",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💰 지갑 목록", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            wallets.forEachIndexed { _, wallet ->
                                val balance = blockchain.getBalanceOfAddress(wallet.address)
                                Text("주소: ${wallet.address.take(10)}... (${balance} 코인)")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { wallets.add(Wallet()) }) {
                                Text("새 지갑 생성")
                            }
                        }
                    }
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⛏️ 채굴", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("대기 중인 거래: ${blockchain.pendingTransactions.size}개")
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (wallets.isNotEmpty() && !miningInProgress) {
                                        miningInProgress = true
                                        blockchain.minePendingTransactions(wallets.first().address)
                                        miningInProgress = false
                                    }
                                },
                                enabled = !miningInProgress && blockchain.pendingTransactions.isNotEmpty()
                            ) {
                                if (miningInProgress) {
                                    Text("채굴 중...")
                                } else {
                                    Text("새 블록 채굴!")
                                }
                            }
                        }
                    }
                }
                
                item {
                    Text("🔗 블록체인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                items(blockchain.chain.asReversed()) { block ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("블록 #${block.index}", fontWeight = FontWeight.Bold)
                            Text("해시: ${block.hash.take(15)}...")
                            Text("Nonce: ${block.nonce}")
                            if (block.transactions.isNotEmpty()) {
                                Text("거래:", fontWeight = FontWeight.SemiBold)
                                block.transactions.forEach { tx ->
                                    Text("  💰 ${tx.fromAddress.take(6)} → ${tx.toAddress.take(6)}: ${tx.amount} 코인")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}