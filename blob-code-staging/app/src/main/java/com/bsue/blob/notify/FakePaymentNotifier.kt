package com.bsue.blob.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bsue.blob.R
import com.bsue.blob.data.formatWon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 체험용 가짜 카드 승인 알림 — "[비상카드] 승인 23,000원 / 치킨나라 · 일시불 · 10/03 21:14"
// 진짜 카드사 알림과 같은 길(알림 읽기)로 블롭에게 전달돼요.
object FakePaymentNotifier {

    private const val CHANNEL_ID = "fake_payment"
    const val EXTRA_FAKE = "blob_fake_payment" // 우리 앱이 만든 가짜 알림 표시

    data class Sample(val amount: Int, val merchant: String)

    // 버튼 누를 때마다 돌아가며
    val samples = listOf(
        Sample(23_000, "치킨나라"),
        Sample(38_700, "런웨이슈즈"),
        Sample(4_800, "달빛커피"),
        Sample(189_000, "소리공방"),
        Sample(3_600, "다모아편의점"),
    )

    // 알림을 보냈으면 true (알림 권한이 꺼져 있으면 false)
    fun send(context: Context, sample: Sample): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "가짜 결제 알림 (체험용)", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "지름마켓 체험에서 보내는 가짜 카드 승인 알림"
            }
        )

        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        val amountText = formatWon(sample.amount).removePrefix("₩") + "원"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blob_notify)
            .setContentTitle("[비상카드] 승인 $amountText")
            .setContentText("${sample.merchant} · 일시불 · $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addExtras(Bundle().apply { putBoolean(EXTRA_FAKE, true) })
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
