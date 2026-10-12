package com.bsue.blob.ui.exhibition

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.bsue.blob.accessibility.isBlobWatchOn
import com.bsue.blob.notify.isPaymentListenerOn
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.components.openAccessibilitySettings
import com.bsue.blob.ui.components.openNotificationListenerSettings
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobGray
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

private val OkGreen = Color(0xFF2E9E4F)

// 숨은 관리자 메뉴 (홈 '탐정 사무실' 글자를 5번 연속 탭)
@Composable
fun AdminMenu(
    exhibitionMode: Boolean,
    resumeTick: Int, // 폰 설정에서 돌아오면 점검 결과 다시 확인
    onExhibitionModeChange: (Boolean) -> Unit,
    onResetAll: () -> Unit,
    onLoadDemo: () -> Unit,
    onUnlockAll: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onOpenPoseLab: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val checks = remember(resumeTick) { readinessChecks(context) }
    var confirmReset by remember { mutableStateOf(false) }
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobCream)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("관리자 메뉴", fontFamily = BlackHanSans, fontSize = 28.sp, color = BlobBlack, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) {
                Text("닫기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
        }
        Spacer(Modifier.height(8.dp))
        HazardTape(Modifier.fillMaxWidth().height(10.dp))

        // ── 전시 모드 ──
        Title("전시 모드")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, BlobBlack, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("전시 모드", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlobBlack)
                Text(
                    "시작 가이드 30초 · 60초 안 만지면 처음 화면 · 출동 횟수 제한 없음 · 화면 안 꺼짐 · 설정의 전시용 버튼 숨김",
                    fontFamily = GothicA1, fontSize = 12.sp, lineHeight = 17.sp, color = BlobBlack.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = exhibitionMode,
                onCheckedChange = onExhibitionModeChange,
                colors = SwitchDefaults.colors(checkedTrackColor = BlobOrange, checkedThumbColor = BlobCream),
            )
        }

        // ── 데이터 ──
        Title("데이터")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onLoadDemo, modifier = Modifier.weight(1f)) {
                Text("데모 데이터 넣기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
            Button(
                onClick = { confirmReset = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
            ) {
                Text("데이터 초기화", fontFamily = GothicA1, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onUnlockAll, modifier = Modifier.weight(1f)) {
                Text("도감 전부 해금", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
            OutlinedButton(onClick = onReplayOnboarding, modifier = Modifier.weight(1f)) {
                Text("첫 안내 다시", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenPoseLab, modifier = Modifier.fillMaxWidth()) {
            Text("캐릭터 테스트 (그림 18장 × 움직임 7개)", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
        }
        Text(
            "데모 데이터: 구독 3개(넷플릭스 내일 결제) · 멜론 반복 결제 기록 · 이번 달 영수증 4장",
            fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp),
        )

        // ── 전시 준비 점검 ──
        Title("전시 준비 점검")
        checks.forEach { check ->
            CheckRow(check)
            Spacer(Modifier.height(8.dp))
        }
        Text(
            "✗ 줄을 누르면 해당 설정 화면이 열려. 고치고 돌아오면 다시 확인해!",
            fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(24.dp))
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = BlobCream,
            title = { Text("모든 기록을 지울까?", fontFamily = BlackHanSans) },
            text = {
                Text(
                    "도감·영수증·지킨 돈·구독·결제 기록이 처음으로 돌아가. 권한과 전시 모드 설정은 그대로야.",
                    fontFamily = GothicA1,
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetAll() }) {
                    Text("초기화", color = BlobOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("취소", color = BlobBlack) }
            },
        )
    }
}

// ── 전시 준비 점검 항목 ────────────────────────────────

private data class ReadinessCheck(
    val label: String,
    val ok: Boolean,
    val detail: String,
    val fix: (Context) -> Unit,
)

@SuppressLint("BatteryLife")
private fun readinessChecks(context: Context): List<ReadinessCheck> {
    val power = context.getSystemService(PowerManager::class.java)
    val notifications = context.getSystemService(NotificationManager::class.java)
    val screenOffMs = Settings.System.getLong(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 0L)
    val packageUri = Uri.parse("package:${context.packageName}")

    return listOf(
        ReadinessCheck(
            "① 다른 앱 위에 표시", Settings.canDrawOverlays(context), "떠 있는 블롭",
        ) { it.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri)) },
        ReadinessCheck(
            "② 접근성", isBlobWatchOn(context), "빼꼼 · 결제 직전 출동",
        ) { openAccessibilitySettings(it) },
        ReadinessCheck(
            "③ 알림 읽기", isPaymentListenerOn(context), "결제 알림 배웅",
        ) { openNotificationListenerSettings(it) },
        ReadinessCheck(
            "알림 보내기 권한", NotificationManagerCompat.from(context).areNotificationsEnabled(), "가짜 결제 알림 · 미션 · 유령 구독",
        ) {
            it.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, it.packageName))
        },
        ReadinessCheck(
            "배터리 최적화 제외", power.isIgnoringBatteryOptimizations(context.packageName), "블롭이 몰래 꺼지지 않게",
        ) { it.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)) },
        ReadinessCheck(
            "화면 꺼짐 시간 10분 이상",
            screenOffMs >= 10 * 60 * 1000L,
            if (screenOffMs > 0) "지금 ${screenOffMs / 1000 / 60}분 ${screenOffMs / 1000 % 60}초 (전시 모드에선 앱이 화면을 켜둬요)" else "확인 못 함",
        ) { it.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) },
        ReadinessCheck(
            "방해금지 모드 꺼짐",
            notifications.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL,
            "켜져 있으면 가짜 결제 알림이 안 보여",
        ) { it.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)) },
    )
}

@Composable
private fun CheckRow(check: ReadinessCheck) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (check.ok) OkGreen.copy(alpha = 0.1f) else BlobOrange.copy(alpha = 0.12f))
            .clickable {
                // 설정 화면이 없는 폰도 있어서 실패해도 앱이 꺼지지 않게
                try { check.fix(context) } catch (e: Exception) { }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (check.ok) "✓" else "✗",
            fontFamily = BlackHanSans, fontSize = 22.sp,
            color = if (check.ok) OkGreen else BlobOrange,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(check.label, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlobBlack)
            Text(check.detail, fontFamily = GothicA1, fontSize = 12.sp, color = BlobGray)
        }
    }
}

@Composable
private fun Title(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(text, fontFamily = BlackHanSans, fontSize = 18.sp, color = BlobOrange)
    Spacer(Modifier.height(10.dp))
}
