package net.lateinit.blockbuilder.kmp.data

import kotlinx.datetime.Clock

/**
 * 하나의 거래(Transaction)를 나타냅니다.
 * @property fromAddress 보내는 사람의 지갑 주소. 시스템(채굴 보상)일 경우 "System".
 * @property toAddress 받는 사람의 지갑 주소.
 * @property amount 거래 금액.
 * @property timestamp 거래 발생 시간.
 */
data class Transaction(
    val fromAddress: String,
    val toAddress: String,
    val amount: Int,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)