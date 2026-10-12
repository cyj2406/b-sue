package com.bsue.blob.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// 구독 하나 (넷플릭스, 유튜브 프리미엄 …)
data class Subscription(
    val id: String,
    val name: String,             // 서비스 이름
    val shortName: String,        // 알림에 쓰는 짧은 이름 (예: 넷플)
    val monthlyAmount: Int,       // 월 금액(원)
    val billingDay: Int,          // 매달 결제일 1~31 (31일인데 그 달이 30일까지면 말일)
    val lastUsed: LocalDate?,     // 마지막 사용일 (모르면 null)
    val usedVerb: String = "쓴",  // "마지막에 본/쓴/들은 게 언제야?"
    val manageUrl: String? = null, // [해지하러 가기] 주소
) {
    // from 날짜 이후(당일 포함) 가장 가까운 결제일
    fun nextBillingDate(from: LocalDate = LocalDate.now()): LocalDate {
        val thisMonth = billingDateIn(from.year, from.monthValue)
        return if (!thisMonth.isBefore(from)) thisMonth else {
            val next = from.plusMonths(1)
            billingDateIn(next.year, next.monthValue)
        }
    }

    private fun billingDateIn(year: Int, month: Int): LocalDate {
        val first = LocalDate.of(year, month, 1)
        return first.withDayOfMonth(billingDay.coerceAtMost(first.lengthOfMonth()))
    }

    fun daysSinceLastUsed(today: LocalDate = LocalDate.now()): Long? =
        lastUsed?.let { ChronoUnit.DAYS.between(it, today) }

    // 한 달 넘게 안 썼으면 '유령 의심'
    fun looksGhost(today: LocalDate = LocalDate.now()): Boolean = (daysSinceLastUsed(today) ?: 0) >= GHOST_DAYS

    companion object {
        const val GHOST_DAYS = 30

        fun encodeList(list: List<Subscription>): String {
            val array = JSONArray()
            list.forEach { s ->
                array.put(
                    JSONObject()
                        .put("id", s.id)
                        .put("name", s.name)
                        .put("short", s.shortName)
                        .put("amount", s.monthlyAmount)
                        .put("day", s.billingDay)
                        .put("last", s.lastUsed?.toString() ?: JSONObject.NULL)
                        .put("verb", s.usedVerb)
                        .put("url", s.manageUrl ?: JSONObject.NULL)
                )
            }
            return array.toString()
        }

        fun decodeList(json: String?): List<Subscription> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val o = array.getJSONObject(i)
                    Subscription(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        shortName = o.optString("short", o.getString("name")),
                        monthlyAmount = o.getInt("amount"),
                        billingDay = o.getInt("day"),
                        lastUsed = if (o.isNull("last")) null else LocalDate.parse(o.getString("last")),
                        usedVerb = o.optString("verb", "쓴"),
                        manageUrl = if (o.isNull("url")) null else o.getString("url"),
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

// 구독 프리셋 — 금액은 기본값이라 등록할 때 고칠 수 있어요 (요금제마다 달라요)
data class SubscriptionPreset(
    val name: String,
    val shortName: String,
    val amount: Int,
    val usedVerb: String,
    val manageUrl: String?,
)

object SubscriptionPresets {
    // 앱 안에서 결제한 구독은 구글 플레이 구독 관리에서 해지할 수 있어요
    const val PLAY_SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions"

    val all = listOf(
        SubscriptionPreset("넷플릭스", "넷플", 17_000, "본", "https://www.netflix.com/cancelplan"),
        SubscriptionPreset("유튜브 프리미엄", "유튜브 프리미엄", 14_900, "본", "https://www.youtube.com/paid_memberships"),
        SubscriptionPreset("쿠팡 와우", "쿠팡 와우", 7_890, "쓴", "https://www.coupang.com"),
        SubscriptionPreset("디즈니+", "디즈니+", 9_900, "본", "https://www.disneyplus.com/account"),
        SubscriptionPreset("티빙", "티빙", 13_900, "본", "https://www.tving.com"),
        SubscriptionPreset("멜론", "멜론", 10_900, "들은", "https://www.melon.com"),
        SubscriptionPreset("스포티파이", "스포티파이", 11_990, "들은", "https://www.spotify.com/account/subscription/"),
        SubscriptionPreset("배민클럽", "배민클럽", 3_990, "쓴", null),
        SubscriptionPreset("네이버플러스 멤버십", "네이버플러스", 4_900, "쓴", "https://nid.naver.com/membership/my"),
    )
}
