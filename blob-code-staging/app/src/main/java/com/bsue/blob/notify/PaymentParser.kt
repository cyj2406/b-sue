package com.bsue.blob.notify

// 결제 알림 한 건 (블롭이 '배웅'할 내용)
data class PaymentNotice(
    val amount: Int?,        // 못 읽으면 null
    val merchant: String?,   // 가맹점 (못 읽으면 null)
    val sourceLabel: String, // 어느 카드사 앱에서 왔는지
)

// 카드사 알림 글자에서 '승인'·금액·가맹점 읽기
object PaymentParser {

    private val approvedRegex = Regex("""승인|결제\s*완료|사용""")
    private val cancelRegex = Regex("""취소|거절|실패""")
    private val amountRegex = Regex("""([\d,]{3,})\s*원""")
    private val separatorRegex = Regex("""[\s·|]+""")
    private val skipWords = setOf("일시불", "할부", "누적", "잔액", "승인", "체크", "신용", "결제")

    // 결제 알림처럼 보이면 PaymentNotice, 아니면 null
    fun parse(title: String, text: String, sourceLabel: String): PaymentNotice? {
        val all = "$title\n$text"
        if (!approvedRegex.containsMatchIn(all) || cancelRegex.containsMatchIn(all)) return null
        val match = amountRegex.find(all) ?: return null // 금액이 없으면 결제 알림이 아니라고 봄
        val amount = match.groupValues[1].replace(",", "").toIntOrNull() ?: return null
        return PaymentNotice(amount, findMerchant(all.substring(match.range.last + 1)), sourceLabel)
    }

    // 금액 뒤에 나오는 낱말 중 가게 이름처럼 생긴 첫 번째
    // 예: "23,000원\n치킨나라 · 일시불" → 치킨나라
    //     "12,500원 일시불 10/03 13:20 스타벅스 누적…" → 스타벅스
    private fun findMerchant(afterAmount: String): String? =
        afterAmount.split(separatorRegex)
            .map { it.trim() }
            .firstOrNull { word ->
                word.length in 2..20 &&
                    word.any { it.isLetter() } &&   // 날짜·시간·숫자만 있는 건 제외
                    !word.contains('*') &&          // 가려진 이름(김*정) 제외
                    skipWords.none { word.startsWith(it) }
            }
}
