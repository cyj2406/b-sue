package com.bsue.blob.mission

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bsue.blob.MainActivity
import com.bsue.blob.R
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.Guardians
import com.bsue.blob.ui.blob.BlobLooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

// 오늘의 미션 알림 — 아침 9시 "미션카드 도착" / 밤 10시 "오늘 어땠어?" [해냈다] [내일 다시]
object MissionAlarm {

    private const val CHANNEL_ID = "daily_mission"
    private const val MORNING_HOUR = 9
    private const val NIGHT_HOUR = 22
    private const val MORNING_NOTIFICATION_ID = 9001
    private const val NIGHT_NOTIFICATION_ID = 9002

    const val ACTION_MORNING = "com.bsue.blob.action.MISSION_MORNING"
    const val ACTION_NIGHT = "com.bsue.blob.action.MISSION_NIGHT"
    const val ACTION_DONE = "com.bsue.blob.action.MISSION_DONE"
    const val ACTION_LATER = "com.bsue.blob.action.MISSION_LATER"

    // 아침·밤 알람 둘 다 다음 시각으로 예약 (정확한 시각 권한 없이 → 몇 분 늦을 수 있어요)
    fun scheduleAll(context: Context) {
        schedule(context, MORNING_HOUR, ACTION_MORNING, requestCode = 1)
        schedule(context, NIGHT_HOUR, ACTION_NIGHT, requestCode = 2)
    }

    private fun schedule(context: Context, hour: Int, action: String, requestCode: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            millis,
            receiverIntent(context, action, requestCode),
        )
    }

    private fun receiverIntent(context: Context, action: String, requestCode: Int) = PendingIntent.getBroadcast(
        context, requestCode,
        Intent(context, MissionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "오늘의 미션", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "아침 9시 미션카드 도착, 밤 10시 오늘 어땠는지 물어봐요"
            }
        )
    }

    private fun openAppIntent(context: Context) = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    // 알림 오른쪽 큰 그림: 그 수호신의 미션 포즈 (작게 줄여서)
    private fun missionArtBitmap(context: Context, guardianId: String): Bitmap? = try {
        BitmapFactory.decodeResource(
            context.resources,
            BlobLooks.missionArt(guardianId),
            BitmapFactory.Options().apply { inSampleSize = 4 },
        )
    } catch (e: Exception) {
        null
    }

    // 아침 9시: 오늘의 미션카드 도착
    fun notifyMorning(context: Context) {
        if (!canNotify(context)) return
        ensureChannel(context)
        val guardian = Guardians.todays()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blob_notify)
            .setLargeIcon(missionArtBitmap(context, guardian.id))
            .setContentTitle("오늘의 미션카드 도착!")
            .setContentText("No.%02d ${guardian.name} — 뒤집어볼래?".format(guardian.number))
            .setStyle(NotificationCompat.BigTextStyle().bigText("No.%02d ${guardian.name}\n${guardian.mission}".format(guardian.number)))
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(MORNING_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
        }
    }

    // 밤 10시: 오늘 어땠어? [해냈다] [내일 다시]
    fun notifyNight(context: Context) {
        if (!canNotify(context)) return
        ensureChannel(context)
        val guardian = Guardians.todays()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blob_notify)
            .setLargeIcon(missionArtBitmap(context, guardian.id))
            .setContentTitle("오늘 어땠어?")
            .setContentText("미션: ${guardian.mission}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("미션: ${guardian.mission}\n못 해도 괜찮아, 내일 또 하면 돼!"))
            .setContentIntent(openAppIntent(context))
            .addAction(0, "해냈다", receiverIntent(context, ACTION_DONE, requestCode = 3))
            .addAction(0, "내일 다시", receiverIntent(context, ACTION_LATER, requestCode = 4))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NIGHT_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
        }
    }

    fun cancelNight(context: Context) {
        NotificationManagerCompat.from(context).cancel(NIGHT_NOTIFICATION_ID)
    }
}

// 미션 알람 · [해냈다]/[내일 다시] 버튼 · 폰 재시작을 받는 곳
class MissionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val repo = BlobRepository(appContext)
                when (intent.action) {
                    MissionAlarm.ACTION_MORNING -> {
                        if (!repo.state.first().restingToday) MissionAlarm.notifyMorning(appContext)
                        MissionAlarm.scheduleAll(appContext)
                    }
                    MissionAlarm.ACTION_NIGHT -> {
                        val state = repo.state.first()
                        // 이미 답했거나 쉬는 날이면 안 물어봄
                        if (state.missionResultToday == null && !state.restingToday) MissionAlarm.notifyNight(appContext)
                        MissionAlarm.scheduleAll(appContext)
                    }
                    // [해냈다] → 오늘의 수호신 해금 (앱을 열면 해금 연출)
                    MissionAlarm.ACTION_DONE -> {
                        repo.setMissionResult(done = true)
                        MissionAlarm.cancelNight(appContext)
                    }
                    MissionAlarm.ACTION_LATER -> {
                        repo.setMissionResult(done = false)
                        MissionAlarm.cancelNight(appContext)
                    }
                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
                        MissionAlarm.scheduleAll(appContext)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
