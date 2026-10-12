package com.bsue.blob.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bsue.blob.accessibility.isBlobWatchOn
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.components.RestrictedSettingsHelp
import com.bsue.blob.ui.components.openAccessibilitySettings
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobGray
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 안내 페이지 순서
private const val PAGE_HELLO = 0
private const val PAGE_OVERLAY = 1
private const val PAGE_ACCESSIBILITY = 2
private const val PAGE_REST = 3
private const val PAGE_COUNT = 4

// 첫 실행 안내 4장 — 블롭이 직접 말하는 형식
@Composable
fun OnboardingScreen(
    resumeTick: Int, // 설정 앱에서 돌아오면 바뀜 → 권한 다시 확인
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableIntStateOf(PAGE_HELLO) }
    val overlayOn = remember(resumeTick) { Settings.canDrawOverlays(context) }
    val watchOn = remember(resumeTick) { isBlobWatchOn(context) }

    // 안드로이드 13 이상에서만 알림 권한을 따로 물어봐요
    val needsNotificationAsk = Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { onFinish() } // 허락하든 안 하든 시작 (고르는 건 사용자)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobCream),
    ) {
        HazardTape(Modifier.fillMaxWidth().height(14.dp))

        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "onboarding",
        ) { current ->
            val line = onboardingLine(current, overlayOn, watchOn)
            // 내용이 짧으면 가운데, 길면 스크롤
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = 28.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // 접근성 페이지는 안내가 길어서 블롭을 조금 작게
                    BlobCharacter(BlobScene.ONBOARDING, Modifier.size(if (current == PAGE_ACCESSIBILITY) 120.dp else 170.dp))
                    Spacer(Modifier.height(20.dp))
                    // 블롭 말풍선
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BlobBlack)
                            .padding(22.dp),
                    ) {
                        Text(
                            text = line.title,
                            fontFamily = BlackHanSans,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            color = BlobOrange,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = line.body,
                            fontFamily = GothicA1,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = BlobCream,
                        )
                    }
                    // APK 설치 시 '제한된 설정' 푸는 법
                    if (current == PAGE_ACCESSIBILITY && !watchOn) {
                        Spacer(Modifier.height(14.dp))
                        RestrictedSettingsHelp()
                    }
                }
            }
        }

        // 페이지 점
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(PAGE_COUNT) { i ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == page) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (i == page) BlobOrange else BlobGray.copy(alpha = 0.4f)),
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        // 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val (label, action) = when (page) {
                PAGE_HELLO -> "좋아, 다음" to { page = PAGE_OVERLAY }
                PAGE_OVERLAY -> if (overlayOn) {
                    "다음" to { page = PAGE_ACCESSIBILITY }
                } else {
                    "허락하러 가기" to {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}"),
                            )
                        )
                    }
                }
                PAGE_ACCESSIBILITY -> if (watchOn) {
                    "다음" to { page = PAGE_REST }
                } else {
                    "허락하러 가기" to { openAccessibilitySettings(context) }
                }
                else -> "알겠어, 출동!" to {
                    if (needsNotificationAsk) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onFinish()
                    }
                }
            }
            Button(
                onClick = action,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
            ) {
                Text(label, fontFamily = BlackHanSans, fontSize = 18.sp)
            }

            // 권한은 강요하지 않음
            val skipTo = when {
                page == PAGE_OVERLAY && !overlayOn -> PAGE_ACCESSIBILITY
                page == PAGE_ACCESSIBILITY && !watchOn -> PAGE_REST
                else -> null
            }
            if (skipTo != null) {
                TextButton(onClick = { page = skipTo }) {
                    Text("나중에 할래", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                }
            } else {
                Spacer(Modifier.height(48.dp).width(1.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private data class OnboardingLine(val title: String, val body: String)

// 블롭 대사 (톤: 친근한 반말, 질문형, 다정하게)
private fun onboardingLine(page: Int, overlayOn: Boolean, watchOn: Boolean): OnboardingLine = when (page) {
    PAGE_HELLO -> OnboardingLine(
        title = "안녕! 나는 탐정 블롭이야",
        body = "결제 직전에 딱 3초만 나타나서 물어볼게.\n막거나 혼내지 않아. 고르는 건 언제나 너야!",
    )
    PAGE_OVERLAY -> if (overlayOn) {
        OnboardingLine(
            title = "고마워! 자리 잡았어",
            body = "이제 다른 앱 위에서도 내가 보여.\n끌어서 옮기고, 톡 하면 말 걸게.",
        )
    } else {
        OnboardingLine(
            title = "네 화면 위에 살아도 될까?",
            body = "쇼핑앱 위에 떠 있으려면 '다른 앱 위에 표시'가 필요해.\n설정에서 '블롭 출동'을 켜고 돌아와 줄래?",
        )
    }
    PAGE_ACCESSIBILITY -> if (watchOn) {
        OnboardingLine(
            title = "좋아, 잠복 시작!",
            body = "쇼핑·배달앱이 열리면 빼꼼하고,\n결제 버튼이 보이면 딱 3초만 멈칫시킬게.",
        )
    } else {
        OnboardingLine(
            title = "어떤 앱 열었는지 살짝 봐도 돼?",
            body = "결제 버튼이 보이면 딱 3초 멈칫시키려고.\n쇼핑·배달앱에서 결제 버튼이랑 금액만 그때 살짝 보고, 저장하거나 폰 밖으로 보내지 않아.\n\n접근성 → 설치된 앱 → '블롭 출동'을 켜줘!",
        )
    }
    else -> OnboardingLine(
        title = "피곤한 날엔 말해줘",
        body = "나를 길게 누르면 '오늘은 쉬어'가 돼.\n알림창의 '집에 보내기'로 언제든 쉬게 할 수도 있어.",
    )
}
