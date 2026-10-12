package com.bsue.blob.data

import java.text.NumberFormat
import java.util.Locale

// 커피 한 잔 값 — '커피 몇 잔' 환산 기준
const val COFFEE_PRICE = 4500

// 12345 → "₩12,345"
fun formatWon(amount: Int): String =
    "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(amount)

// 12345 → 2 (커피 2잔)
fun coffeeCups(amount: Int): Int = amount / COFFEE_PRICE

// 12345 → "2.7", 54000 → "12" (10잔 미만은 소수 한 자리까지)
fun coffeeCupsLabel(amount: Int): String {
    val cups = amount.toDouble() / COFFEE_PRICE
    return if (cups < 10) {
        val rounded = Math.round(cups * 10) / 10.0
        if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    } else {
        cups.toInt().toString()
    }
}
