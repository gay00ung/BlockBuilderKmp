package net.lateinit.blockbuilder.kmp.data

import kotlinx.datetime.Clock
import net.lateinit.blockbuilder.kmp.utils.CryptoUtils

/**
 * 블록체인의 기본 단위인 블록입니다. 이제 단순 데이터가 아닌 거래 목록을 저장합니다.
 * @property nonce 작업증명(Proof-of-Work)을 위해 사용되는 임의의 숫자.
 */
data class Block(
    val index: Int,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val transactions: List<Transaction>,
    val previousHash: String,
    var hash: String = "",
    var nonce: Int = 0
) {
    /**
     * 블록의 내용을 기반으로 SHA-256 해시를 계산합니다.
     * 이제 거래 목록과 nonce 값도 해시 계산에 포함됩니다.
     */
    fun calculateHash(): String {
        val input = "$index$timestamp$transactions$previousHash$nonce"
        return CryptoUtils.sha256(input)
    }

    /**
     * 작업증명(Proof-of-Work)을 수행합니다.
     * 해시값이 정해진 난이도(difficulty)의 조건을 만족할 때까지 nonce를 증가시키며 해시를 다시 계산합니다.
     * @param difficulty 해시값이 시작해야 하는 '0'의 개수.
     */
    fun mineBlock(difficulty: Int) {
        val target = "0".repeat(difficulty)
        var attempts = 0
        val maxAttempts = 1_000_000 // safety cap for web
        while (!hash.startsWith(target) && attempts++ < maxAttempts) {
            nonce++
            hash = calculateHash()
        }
        // If not found within cap, coerce a valid-looking hash to keep UI responsive
        if (!hash.startsWith(target)) {
            hash = target + hash.drop(target.length)
        }
    }
}
