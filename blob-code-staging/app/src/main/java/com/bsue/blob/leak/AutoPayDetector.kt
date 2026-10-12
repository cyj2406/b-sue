package com.bsue.blob.leak

import com.bsue.blob.data.PaymentRecord
import com.bsue.blob.data.Subscription
import java.time.YearMonth
import kotlin.math.abs

// 자동결제 잠복수사 — 결제 알림 기록에서 '구독 같은 결제' 찾기
// 조건: 같은 가게에서 연달아 2달 이상, 매달 비슷한 날(±3일), 비슷한 금액(±10%)
data class AutoPaySuspect(
    val key: String,         // 가게 이름 정리한 값 (넘기기 기록용)
    val merchant: String,
    val amount: Int,         // 가장 최근 금액
    val dayOfMonth: Int,     // 보통 결제되는 날
    val months: Int,         // 몇 달째 반복인지
)

object AutoPayDetector {

    private const val MIN_MONTHS = 2
    private const val DAY_TOLERANCE = 3
    private const val AMOUNT_TOLERANCE = 0.10

    // 띄어쓰기·기호 빼고 소문자로 (예: "Melon (멜론)" → "melon멜론")
    fun normalize(name: String): String = name.lowercase().filter { it.isLetterOrDigit() }

    fun detect(
        records: List<PaymentRecord>,
        subscriptions: List<Subscription>,
        dismissedKeys: Set<String>,
    ): List<AutoPaySuspect> {
        val knownNames = subscriptions.map { normalize(it.name) } + subscriptions.map { normalize(it.shortName) }

        return records
            .groupBy { normalize(it.merchant) }
            .filterKeys { key -> key.isNotEmpty() && key !in dismissedKeys }
            // 이미 구독으로 등록한 건 빼기
            .filterKeys { key -> knownNames.none { known -> known.isNotEmpty() && (key.contains(known) || known.contains(key)) } }
            .mapNotNull { (key, group) -> suspectOrNull(key, group) }
            .sortedByDescending { it.months }
    }

    private fun suspectOrNull(key: String, group: List<PaymentRecord>): AutoPaySuspect? {
        // 한 달에 한 건만 (그 달 첫 결제)
        val monthly = group
            .sortedBy { it.createdAt }
            .groupBy { YearMonth.from(it.date) }
            .map { (month, list) -> month to list.first() }
            .sortedBy { it.first }
        if (monthly.size < MIN_MONTHS) return null

        // 가장 최근부터 거꾸로, 달이 끊기지 않고 이어진 구간만
        val streak = mutableListOf(monthly.last())
        for (i in monthly.size - 2 downTo 0) {
            if (monthly[i].first.plusMonths(1) == streak.last().first) streak.add(monthly[i]) else break
        }
        if (streak.size < MIN_MONTHS) return null

        val records = streak.map { it.second }
        val days = records.map { it.date.dayOfMonth }
        if (days.max() - days.min() > DAY_TOLERANCE * 2) return null
        val amounts = records.map { it.amount }
        if (amounts.any { abs(it - amounts.first()) > amounts.first() * AMOUNT_TOLERANCE }) return null

        val latest = records.first() // streak은 최근 것부터
        return AutoPaySuspect(
            key = key,
            merchant = latest.merchant,
            amount = latest.amount,
            dayOfMonth = days.sorted()[days.size / 2],
            months = streak.size,
        )
    }
}
