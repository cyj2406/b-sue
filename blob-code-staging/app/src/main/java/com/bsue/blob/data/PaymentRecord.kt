package com.bsue.blob.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// 결제 알림 기록 한 줄 — 금액·가게·날짜만 폰 안에 저장 ('자동결제 잠복수사'용)
data class PaymentRecord(
    val createdAt: Long,     // 알림 받은 시각 (밀리초)
    val amount: Int,
    val merchant: String,
    val source: String,      // 카드사 앱 이름
) {
    val date: LocalDate
        get() = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()

    companion object {
        fun encodeList(list: List<PaymentRecord>): String {
            val array = JSONArray()
            list.forEach { r ->
                array.put(
                    JSONObject()
                        .put("t", r.createdAt)
                        .put("a", r.amount)
                        .put("m", r.merchant)
                        .put("s", r.source)
                )
            }
            return array.toString()
        }

        fun decodeList(json: String?): List<PaymentRecord> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val o = array.getJSONObject(i)
                    PaymentRecord(o.getLong("t"), o.getInt("a"), o.getString("m"), o.optString("s"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
