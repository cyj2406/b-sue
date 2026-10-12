package com.bsue.blob.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.accessibility.isBlobWatchOn
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.CardApps
import com.bsue.blob.notify.isPaymentListenerOn
import com.bsue.blob.ui.components.openNotificationListenerSettings
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.WatchedApps
import com.bsue.blob.overlay.OverlayBus
import com.bsue.blob.ui.components.RestrictedSettingsHelp
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobGray
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 설정: 권한 상태 · 하루 출동 제한 · 오늘은 쉬어 · 전시용 도구
@Composable
fun SettingsScreen(
    state: BlobState,
    resumeTick: Int, // 설정 앱에서 돌아올 때마다 바뀜 → 권한 상태 다시 확인
    onDailyLimitChange: (Int) -> Unit,
    onRestingChange: (Boolean) -> Unit,
    onUnlockAll: () -> Unit,
    onResetRecords: () -> Unit,
    onReplayOnboarding: () -> Unit,
    onTestDispatch: () -> Unit,
    onAddCardPackage: (String) -> Unit,
    onRemoveCardPackage: (String) -> Unit,
    onSendFakePayment: () -> Unit,
) {
    val context = LocalContext.current
    val overlayOn = remember(resumeTick) { Settings.canDrawOverlays(context) }
    val accessibilityOn = remember(resumeTick) { isBlobWatchOn(context) }
    val recentPackages by OverlayBus.recentPackages.collectAsState()
    val listenerOn = remember(resumeTick) { isPaymentListenerOn(context) }
    val paymentLike by OverlayBus.paymentLikePackages.collectAsState()
    var newPackage by remember { mutableStateOf("") }
    var askReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("설정", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobBlack)
        Spacer(Modifier.height(10.dp))
        HazardTape(Modifier.fillMaxWidth().height(10.dp))

        // ── 권한 ──
        SectionTitle("블롭 출동 권한")
        PermissionRow(
            title = "다른 앱 위에 표시",
            description = "쇼핑·배달앱 위에 블롭이 떠 있으려면 필요해",
            on = overlayOn,
            onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    )
                )
            },
        )
        Spacer(Modifier.height(10.dp))
        PermissionRow(
            title = "접근성 (앱·결제 화면 감지)",
            description = "빼꼼하고 결제 직전에 출동하려면 필요해 · 막혀 있으면 아래 '제한된 설정' 풀기",
            on = accessibilityOn,
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
        )

        Spacer(Modifier.height(10.dp))
        RestrictedSettingsHelp()
        Spacer(Modifier.height(10.dp))
        PermissionRow(
            title = "알림 읽기 (결제 알림 배웅)",
            description = "카드 승인 알림이 오면 배웅하고 금액·가게·날짜만 폰에 기록해 (잠복수사용) · 등록한 카드사 앱만 봐",
            on = listenerOn,
            onClick = { openNotificationListenerSettings(context) },
        )
        Spacer(Modifier.height(10.dp))
        RestrictedSettingsHelp(forNotifications = true)

        // ── 출동 규칙 ──
        SectionTitle("출동 규칙")
        // 쇼핑앱 없이 출동 화면 바로 보기 (출동 횟수엔 안 셈, [참았다!]는 영수증에 저장돼요)
        Button(
            onClick = onTestDispatch,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
        ) {
            Text("출동 테스트", fontFamily = BlackHanSans, fontSize = 18.sp)
        }
        Spacer(Modifier.height(10.dp))
        SettingCard {
            Column(Modifier.weight(1f)) {
                RowTitle("하루 출동 횟수")
                RowDescription("하루에 블롭이 나오는 최대 횟수야")
            }
            StepButton("−", enabled = state.dailyLimit > BlobState.MIN_DAILY_LIMIT) {
                onDailyLimitChange(state.dailyLimit - 1)
            }
            Text(
                text = "${state.dailyLimit}",
                fontFamily = BlackHanSans,
                fontSize = 24.sp,
                color = BlobOrange,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            StepButton("+", enabled = state.dailyLimit < BlobState.MAX_DAILY_LIMIT) {
                onDailyLimitChange(state.dailyLimit + 1)
            }
        }
        Spacer(Modifier.height(10.dp))
        SettingCard {
            Column(Modifier.weight(1f)) {
                RowTitle("오늘은 쉬어")
                RowDescription("켜면 오늘 하루 블롭이 안 나와. 내일 자동으로 꺼져")
            }
            Switch(
                checked = state.restingToday,
                onCheckedChange = onRestingChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BlobCream,
                    checkedTrackColor = BlobOrange,
                    uncheckedThumbColor = BlobGray,
                    uncheckedTrackColor = BlobCream,
                    uncheckedBorderColor = BlobGray,
                ),
            )
        }

        // ── 지켜보는 앱 ──
        SectionTitle("지켜보는 앱")
        SettingCard {
            Column(Modifier.weight(1f)) {
                WatchedApps.all.forEach { app ->
                    val guardian = Guardians.byId(app.guardianId)
                    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(guardian?.color ?: BlobGray),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(app.label, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlobBlack)
                        Spacer(Modifier.weight(1f))
                        Text(guardian?.name ?: "-", fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // ── 패키지명 확인 ──
        SectionTitle("최근 연 앱 (패키지명 확인용)")
        SettingCard {
            Column(Modifier.weight(1f)) {
                if (!accessibilityOn) {
                    RowDescription("접근성을 켜면 여기에 방금 연 앱의 패키지명이 보여.")
                } else if (recentPackages.isEmpty()) {
                    RowDescription("홈으로 나가서 아무 앱이나 열었다가 돌아와 봐!")
                } else {
                    recentPackages.forEach { pkg ->
                        val watched = WatchedApps.find(pkg)
                        Text(
                            text = if (watched != null) "● $pkg  (${watched.label})" else "○ $pkg",
                            fontFamily = GothicA1,
                            fontWeight = if (watched != null) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (watched != null) BlobOrange else BlobBlack,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }

        // ── 결제 알림 읽을 카드사 ──
        SectionTitle("결제 알림 읽을 카드사 앱")
        SettingCard {
            Column(Modifier.weight(1f)) {
                state.cardPackages.sorted().forEach { pkg ->
                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(CardApps.label(pkg), fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlobBlack)
                            if (CardApps.label(pkg) != pkg) {
                                Text(pkg, fontFamily = GothicA1, fontSize = 11.sp, color = BlobBlack.copy(alpha = 0.55f))
                            }
                        }
                        TextButton(onClick = { onRemoveCardPackage(pkg) }) {
                            Text("빼기", fontFamily = GothicA1, color = BlobGray)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // 패키지명 직접 추가 (예: 삼성카드, BC카드 앱)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPackage,
                        onValueChange = { newPackage = it.trim() },
                        placeholder = { Text("패키지명 (예: com.example.card)", fontSize = 12.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = GothicA1, fontSize = 13.sp),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onAddCardPackage(newPackage); newPackage = "" },
                        enabled = newPackage.contains('.'),
                        colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
                    ) {
                        Text("추가", fontFamily = GothicA1, fontWeight = FontWeight.Bold)
                    }
                }
                // 알림 읽기가 찾아낸 '승인' 알림 앱 → 한 번에 추가
                val suggestions = paymentLike.filter { it !in state.cardPackages }
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    RowDescription("이 앱에서 '승인' 알림이 왔어. 추가할까?")
                    suggestions.forEach { pkg ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(pkg, fontFamily = GothicA1, fontSize = 13.sp, color = BlobOrange, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onAddCardPackage(pkg) }) {
                                Text("추가", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onSendFakePayment, modifier = Modifier.fillMaxWidth()) {
            Text("가짜 결제 알림 보내기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
        }

        // ── 전시용 ── (전시 모드에선 관람객이 못 누르게 숨김 → 관리자 메뉴에서)
        if (!state.exhibitionMode) {
            SectionTitle("전시용")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onUnlockAll, modifier = Modifier.weight(1f)) {
                    Text("도감 전부 해금", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                }
                Button(
                    onClick = { askReset = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
                ) {
                    Text("기록 초기화", fontFamily = GothicA1, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onReplayOnboarding, modifier = Modifier.fillMaxWidth()) {
                Text("첫 안내 다시 보기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (askReset) {
        AlertDialog(
            onDismissRequest = { askReset = false },
            title = { Text("기록을 초기화할까?", fontFamily = BlackHanSans) },
            text = { Text("도감 해금, 지킨 돈, 출동 횟수가 처음으로 돌아가. 출동 횟수 설정은 그대로야.", fontFamily = GothicA1) },
            confirmButton = {
                TextButton(onClick = { askReset = false; onResetRecords() }) {
                    Text("초기화", color = BlobOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { askReset = false }) {
                    Text("취소", color = BlobBlack)
                }
            },
            containerColor = BlobCream,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(24.dp))
    Text(text, fontFamily = BlackHanSans, fontSize = 18.sp, color = BlobOrange)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingCard(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, BlobBlack, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowTitle(text: String) {
    Text(text, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlobBlack)
}

@Composable
private fun RowDescription(text: String) {
    Spacer(Modifier.height(2.dp))
    Text(text, fontFamily = GothicA1, fontSize = 12.sp, lineHeight = 16.sp, color = BlobBlack.copy(alpha = 0.7f))
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    on: Boolean,
    onClick: () -> Unit,
) {
    SettingCard {
        Column(Modifier.weight(1f)) {
            RowTitle(title)
            RowDescription(description)
        }
        Spacer(Modifier.width(10.dp))
        if (on) {
            Text(
                text = "켜짐",
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BlobCream,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BlobBlack)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        } else {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
            ) {
                Text("켜러 가기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(44.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(label, fontFamily = BlackHanSans, fontSize = 20.sp, color = if (enabled) BlobBlack else BlobGray)
    }
}
