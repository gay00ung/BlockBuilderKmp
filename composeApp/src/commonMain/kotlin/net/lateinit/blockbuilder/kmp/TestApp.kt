package net.lateinit.blockbuilder.kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.lateinit.blockbuilder.kmp.data.Blockchain
import net.lateinit.blockbuilder.kmp.data.Transaction
import net.lateinit.blockbuilder.kmp.data.Wallet

@Composable
fun TestApp() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "BlockBuilder - Blockchain Simulator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            // 블록체인 연결 - 단계별 테스트
            val blockchain = remember { 
                try {
                    Blockchain()
                } catch (e: Exception) {
                    println("Blockchain creation error: $e")
                    null
                }
            }
            
            val wallets = remember { mutableStateListOf<Wallet>() }
            var refreshKey by remember { mutableIntStateOf(0) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            
            // 초기 설정 - 안전하게
            LaunchedEffect(Unit) {
                try {
                    if (wallets.isEmpty() && blockchain != null) {
                        wallets.add(Wallet())
                        wallets.add(Wallet())
                        
                        blockchain.createTransaction(Transaction("System", wallets[0].address, 200))
                        blockchain.createTransaction(Transaction("System", wallets[1].address, 300))
                        blockchain.minePendingTransactions(wallets[0].address)
                        refreshKey++
                    }
                } catch (e: Exception) {
                    errorMessage = "Initialization error: ${e.message}"
                    println("Init error: $e")
                }
            }
            
            // 에러 메시지 표시
            if (errorMessage != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Error: $errorMessage", color = androidx.compose.ui.graphics.Color.Red)
                    }
                }
            }
            
            // 지갑 섹션
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wallets", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (blockchain == null) {
                        Text("Blockchain failed to initialize")
                    } else {
                        wallets.forEachIndexed { index, wallet ->
                            val balance = try {
                                blockchain.getBalanceOfAddress(wallet.address)
                            } catch (e: Exception) {
                                -1
                            }
                            Text("Wallet ${index + 1}: ${wallet.address.take(8)}... (Balance: $balance coins)")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { 
                        wallets.add(Wallet())
                        refreshKey++
                    }) {
                        Text("Create New Wallet")
                    }
                }
            }
            
            // 채굴 섹션
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mining", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Pending transactions: ${blockchain?.pendingTransactions?.size ?: 0}")
                    Text("Mining difficulty: ${blockchain?.difficulty ?: 0}")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (wallets.isNotEmpty() && blockchain != null) {
                                blockchain.minePendingTransactions(wallets.first().address)
                                refreshKey++
                            }
                        },
                        enabled = blockchain?.pendingTransactions?.isNotEmpty() == true
                    ) {
                        Text("Mine New Block")
                    }
                    
                    // 난이도 조절 버튼들
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (blockchain != null && blockchain.difficulty > 1) {
                                    blockchain.difficulty--
                                    refreshKey++
                                }
                            },
                            enabled = blockchain?.difficulty?.let { it > 1 } == true
                        ) {
                            Text("Difficulty -")
                        }
                        
                        Button(
                            onClick = {
                                if (blockchain != null && blockchain.difficulty < 5) {
                                    blockchain.difficulty++
                                    refreshKey++
                                }
                            },
                            enabled = blockchain?.difficulty?.let { it < 5 } == true
                        ) {
                            Text("Difficulty +")
                        }
                    }
                    
                    if (wallets.size >= 2) {
                        Button(
                            onClick = {
                                blockchain?.createTransaction(Transaction(
                                    wallets[0].address,
                                    wallets[1].address, 
                                    50
                                ))
                                refreshKey++
                            }
                        ) {
                            Text("Send 50 coins (Wallet1 → Wallet2)")
                        }
                    }
                }
            }
            
            // 블록체인 시각화 
            Text("Blockchain", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockchain?.chain?.reversed() ?: emptyList()) { block ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Block #${block.index}", fontWeight = FontWeight.Bold)
                            Text("Hash: ${block
                                .hash.take(12)}...")
                            Text("Previous Hash: ${block.previousHash.take(12)}...")
                            Text("Nonce: ${block.nonce}")
                            
                            if (block.transactions.isNotEmpty()) {
                                Text("Transactions:", fontWeight = FontWeight.SemiBold)
                                block.transactions.forEach { tx ->
                                    val fromShort = if (tx.fromAddress == "System") "System" else tx.fromAddress.take(4) + "..."
                                    val toShort = tx.toAddress.take(4) + "..."
                                    Text("  $fromShort -> $toShort: ${tx.amount} coins")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}