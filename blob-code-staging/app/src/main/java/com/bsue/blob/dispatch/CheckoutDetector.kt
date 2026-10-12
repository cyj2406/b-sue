package com.bsue.blob.dispatch

import com.bsue.blob.data.WatchedApp
import kotlin.math.min

// 화면 글자 목록을 보고 "결제 직전인지", "얼마인지", "어느 수호신이 나갈지" 판단
object CheckoutDetector {

    // 결제 직전 신호: 버튼 글자 (긴 설명문 속 단어는 무시하려고 30자 이하만 봄)
    private val checkoutRegex = Regex("""결제하기|주문하기|구매하기|[\d,]+\s*원\s*결제""")
    private const val MAX_BUTTON_TEXT = 30

    // 금액: "₩12,300" 또는 "12,300원"
    private val amountRegex = Regex("""₩\s*([\d,]{3,})|([\d,]{3,})\s*원""")
    private const val MIN_AMOUNT = 1_000
    private const val MAX_AMOUNT = 10_000_000

    // 총액 라벨 (이 글자 근처의 금액을 우선)
    private val totalLabelRegex = Regex("""총\s*결제|결제\s*금액|최종\s*결제|총\s*주문\s*금액|총\s*금액|합계""")

    // 수호신 고르는 신호
    private const val FREE_SHIPPING_TEXT = "무료배송까지"
    private val saleRegex = Regex("""할인|세일|SALE|\d\s*%""", RegexOption.IGNORE_CASE)

    enum class Reason { FREE_SHIPPING, SALE, APP }

    data class Scan(
        val amount: Int?,       // 못 읽으면 null
        val guardianId: String,
        val reason: Reason,
        val triggerText: String, // 감지된 버튼 글자 (예: "23,800원 결제하기")
    )

    fun analyze(texts: List<String>, app: WatchedApp): Scan? {
        val trigger = texts.firstOrNull { it.length <= MAX_BUTTON_TEXT && checkoutRegex.containsMatchIn(it) }
            ?: return null

        // 금액 우선순위: ① 버튼 안 금액 → ② '총 결제금액' 근처 → ③ 화면에서 가장 큰 금액
        val amount = amountsIn(trigger).maxOrNull()
            ?: amountNearTotalLabel(texts)
            ?: texts.flatMap { amountsIn(it) }.maxOrNull()

        // 수호신: '무료배송까지'(가장 구체적) → 할인·세일·% → 앱 담당
        val allText = texts.joinToString("\n")
        val (guardianId, reason) = when {
            allText.contains(FREE_SHIPPING_TEXT) -> "free_shipping" to Reason.FREE_SHIPPING
            saleRegex.containsMatchIn(allText) -> "sale" to Reason.SALE
            else -> app.guardianId to Reason.APP
        }
        return Scan(amount, guardianId, reason, trigger)
    }

    fun hasCheckoutText(texts: List<String>): Boolean =
        texts.any { it.length <= MAX_BUTTON_TEXT && checkoutRegex.containsMatchIn(it) }

    private fun amountsIn(text: String): List<Int> =
        amountRegex.findAll(text).mapNotNull { m ->
            val digits = (m.groupValues[1].ifEmpty { m.groupValues[2] }).replace(",", "")
            digits.toIntOrNull()?.takeIf { it in MIN_AMOUNT..MAX_AMOUNT }
        }.toList()

    private fun amountNearTotalLabel(texts: List<String>): Int? {
        for (i in texts.indices) {
            if (!totalLabelRegex.containsMatchIn(texts[i])) continue
            amountsIn(texts[i]).maxOrNull()?.let { return it }
            // 라벨 바로 뒤 3줄 안에서 찾기 (라벨과 금액이 따로 적힌 화면)
            for (j in i + 1..min(i + 3, texts.lastIndex)) {
                amountsIn(texts[j]).maxOrNull()?.let { return it }
            }
        }
        return null
    }
}
