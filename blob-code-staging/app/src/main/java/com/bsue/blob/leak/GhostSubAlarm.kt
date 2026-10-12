package com.bsue.blob.leak

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bsue.blob.R
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.Subscription
import com.bsue.blob.data.SubscriptionPresets
import com.bsue.blob.data.formatWon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// 유령 구독 색출 — 매일 저녁 8시에 "내일 결제되는 구독"을 찾아 알림
object GhostSubAlarm {

    private const val CHANNEL_ID = "ghost_sub"
    private const val ALERT_HOUR = 20 // 저녁 8시

    const val ACTION_CHECK = "com.bsue.blob.action.GHOST_CHECK"
    const val ACTION_KEEP = "com.bsue.blob.action.GHOST_KEEP"
    const val EXTRA_SUB_ID = "sub_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"

    // 다음 저녁 8시에 한 번 깨우기 (울린 뒤엔 다시 다음 날로 예약)
    // 정확한 시각 권한 없이 쓰는 방식이라 몇 분 늦을 수 있어요
    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(ALERT_HOUR, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, GhostSubReceiver::class.java).setAction(ACTION_CHECK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    }

    // 내일 결제되는 구독마다 알림. 보낸 개수를 돌려줌
    fun notifyDueTomorrow(context: Context, subscriptions: List<Subscription>, today: LocalDate = LocalDate.now()): Int {
        val tomorrow = today.plusDays(1)
        return subscriptions
            .filter { it.nextBillingDate(tomorrow) == tomorrow }
            .count { notify(context, it, preview = false) }
    }

    // 알림 하나 보내기. preview = 설정·누수 점검의 '미리보기' 버튼
    fun notify(context: Context, sub: Subscription, preview: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "유령 구독 색출", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "구독 결제일 전날 저녁 8시에 알려줘요"
            }
        )

        val notificationId = sub.id.hashCode()
        val amountText = formatWon(sub.monthlyAmount).removePrefix("₩") + "원"
        val lastUsedText = sub.daysSinceLastUsed()?.let { days ->
            if (days == 0L) " (오늘 ${sub.usedVerb} 걸로 기억해)" else " (내 기억엔 ${days}일 전)"
        }.orEmpty()
        val text = "내일 ${sub.shortName} ${amountText} 나가. 마지막에 ${sub.usedVerb} 게 언제야?$lastUsedText"

        val keep = PendingIntent.getBroadcast(
            context, notificationId,
            Intent(context, GhostSubReceiver::class.java)
                .setAction(ACTION_KEEP)
                .putExtra(EXTRA_SUB_ID, sub.id)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val cancelPage = PendingIntent.getActivity(
            context, notificationId,
            Intent(Intent.ACTION_VIEW, Uri.parse(sub.manageUrl ?: SubscriptionPresets.PLAY_SUBSCRIPTIONS_URL)),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blob_notify)
            .setContentTitle(if (preview) "유령 구독 색출 (미리보기)" else "유령 구독 색출")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .addAction(0, "계속 쓸래", keep)
            .addAction(0, "해지하러 가기", cancelPage)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}

// 저녁 8시 알람 · 폰 재시작 · [계속 쓸래] 버튼을 받는 곳
class GhostSubReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val repo = BlobRepository(appContext)
                when (intent.action) {
                    GhostSubAlarm.ACTION_CHECK -> {
                        val subs = repo.state.first().subscriptions
                        GhostSubAlarm.notifyDueTomorrow(appContext, subs)
                        GhostSubAlarm.scheduleNext(appContext)
                    }
                    // 폰을 껐다 켜거나 앱을 업데이트하면 알람이 사라져서 다시 예약
                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
                        GhostSubAlarm.scheduleNext(appContext)
                    // [계속 쓸래] → 마지막 사용일을 오늘로 + 알림 닫기
                    GhostSubAlarm.ACTION_KEEP -> {
                        intent.getStringExtra(GhostSubAlarm.EXTRA_SUB_ID)?.let { repo.markSubscriptionUsedToday(it) }
                        NotificationManagerCompat.from(appContext)
                            .cancel(intent.getIntExtra(GhostSubAlarm.EXTRA_NOTIFICATION_ID, 0))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
