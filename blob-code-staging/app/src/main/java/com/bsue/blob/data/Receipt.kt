package com.bsue.blob.data

import org.json.JSONArray
import org.json.JSONObject

// "안 지른 영수증" 한 장 — [참았다!]를 누를 때마다 하나씩 쌓여요
data class Receipt(
    val createdAt: Long,     // 저장한 시각 (밀리초)
    val amount: Int?,        // 금액 (화면에서 못 읽었으면 null)
    val guardianId: String,  // 출동한 수호신
    val appLabel: String,    // 어느 앱에서 참았는지 (예: 배달의민족)
) {
    companion object {
        // 폰 저장소에 글자 하나로 넣었다 꺼내기 (JSON)
        fun encodeList(list: List<Receipt>): String {
            val array = JSONArray()
            list.forEach { r ->
                array.put(
                    JSONObject()
                        .put("t", r.createdAt)
                        .put("a", r.amount ?: JSONObject.NULL)
                        .put("g", r.guardianId)
                        .put("app", r.appLabel)
                )
            }
            return array.toString()
        }

        fun decodeList(json: String?): List<Receipt> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val o = array.getJSONObject(i)
                    Receipt(
                        createdAt = o.getLong("t"),
                        amount = if (o.isNull("a")) null else o.getInt("a"),
                        guardianId = o.getString("g"),
                        appLabel = o.optString("app"),
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
