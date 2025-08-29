package net.lateinit.blockbuilder.kmp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.lateinit.blockbuilder.kmp.data.Blockchain
import net.lateinit.blockbuilder.kmp.data.Transaction
import net.lateinit.blockbuilder.kmp.data.Wallet

@Composable
fun VerySimpleApp() {
    MaterialTheme {
        val blockchain = remember { Blockchain() }
        val wallets = remember { mutableStateListOf<Wallet>() }
        var refreshKey by remember { mutableIntStateOf(0) }
        
        // 초기 설정
        LaunchedEffect(Unit) {
            if (wallets.isEmpty()) {
                repeat(2) { 
                    wallets.add(Wallet())
                }
                // 초기 거래
                blockchain.createTransaction(Transaction("System", wallets[0].address, 200))
                blockchain.createTransaction(Transaction("System", wallets[1].address, 300))
                blockchain.minePendingTransactions(wallets[0].address)
                refreshKey++
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "🔗 BlockBuilder",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💰 지갑", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    for (i in wallets.indices) {
                        val wallet = wallets[i]
                        val balance = blockchain.getBalanceOfAddress(wallet.address)
                        Text("지갑 ${i+1}: ${wallet.address.take(8)}... (${balance} 코인)")
                    }
                    
                    Button(onClick = { 
                        wallets.add(Wallet())
                        refreshKey++
                    }) {
                        Text("지갑 추가")
                    }
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⛏️ 채굴", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("대기 거래: ${blockchain.pendingTransactions.size}개")
                    
                    Button(
                        onClick = {
                            if (wallets.isNotEmpty()) {
                                blockchain.minePendingTransactions(wallets.first().address)
                                refreshKey++
                            }
                        },
                        enabled = blockchain.pendingTransactions.isNotEmpty()
                    ) {
                        Text("블록 채굴")
                    }
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp).padding(16.dp)) {
                    item { 
                        Text("🔗 블록체인", fontSize = 18.sp, fontWeight = FontWeight.Bold) 
                    }
                    
                    items(blockchain.chain.reversed()) { block ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("블록 #${block.index}", fontWeight = FontWeight.Bold)
                                Text("해시: ${block.hash.take(12)}...")
                                if (block.transactions.isNotEmpty()) {
                                    Text("거래:")
                                    for (tx in block.transactions) {
                                        Text("  ${tx.fromAddress.take(4)}→${tx.toAddress.take(4)}: ${tx.amount}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}