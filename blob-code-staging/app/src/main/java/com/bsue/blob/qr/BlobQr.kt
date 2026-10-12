package com.bsue.blob.qr

import com.bsue.blob.data.Guardian
import com.bsue.blob.data.Guardians

// 실물 카드·카드홀더의 QR 내용
//   카드 뒷면 12장: bsue://card/01 ~ bsue://card/12  (번호 = 도감 번호)
//   카드홀더 1개:   bsue://holder
sealed interface BlobQr {
    data class Card(val guardian: Guardian) : BlobQr
    data object Holder : BlobQr

    companion object {
        private val cardRegex = Regex("""^bsue://card/(\d{1,2})/?$""", RegexOption.IGNORE_CASE)
        private val holderRegex = Regex("""^bsue://holder/?$""", RegexOption.IGNORE_CASE)

        // 우리 QR이 아니면 null
        fun parse(raw: String?): BlobQr? {
            val text = raw?.trim() ?: return null
            if (holderRegex.matches(text)) return Holder
            val number = cardRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return null
            return Guardians.all.firstOrNull { it.number == number }?.let { Card(it) }
        }
    }
}
