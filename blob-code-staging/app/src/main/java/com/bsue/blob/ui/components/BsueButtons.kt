package com.bsue.blob.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import com.bsue.blob.ui.theme.BsueShape
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.Role

// ════════════════════════════════════════════════
//  버튼 4종 + 하단 고정 버튼 — DESIGN.md 5번
//  누르면 100ms 동안 0.97로 살짝 작아지고, 배경이 한 단계 진해져요.
// ════════════════════════════════════════════════

// 보조 버튼이 놓이는 바탕
enum class SecondaryOn { Light, Grey, Dark } // 흰 카드·시트 위 / 회색 화면 위 / 어두운 출동 화면 위

// 모든 버튼의 공통 틀
@Composable
private fun BsueButtonBase(
    label: String,
    background: Color,
    textColor: Color,
    height: androidx.compose.ui.unit.Dp,
    textStyle: TextStyle,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(100), label = "press")
    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(BsueShape.Button)
            .background(if (enabled) background else Role.Divider)
            // 눌림: 배경 한 단계 진하게 (검정 8% 덮기)
            .background(if (pressed) Color.Black.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = BsueSpace.X16),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = textStyle, color = if (enabled) textColor else Role.TextDisabled, maxLines = 1)
    }
}

// Primary — 화면당 1개. "참았다!", "결제하기", "시작하기"
@Composable
fun PrimaryButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    BsueButtonBase(label, Role.Point, Color.White, BsueSpace.ButtonHeight, BsueType.Button, modifier, enabled, onClick)

// Secondary — Primary 옆 보조. "그래도 살래", "닫기"
@Composable
fun SecondaryButton(
    label: String,
    modifier: Modifier = Modifier,
    on: SecondaryOn = SecondaryOn.Light,
    onClick: () -> Unit,
) {
    val (bg, fg) = when (on) {
        SecondaryOn.Light -> Role.SecondaryBg to Role.SecondaryText
        SecondaryOn.Grey -> Color.White to Role.SecondaryText
        SecondaryOn.Dark -> Role.DarkSecondaryBg to Role.DarkSecondaryText
    }
    BsueButtonBase(label, bg, fg, BsueSpace.ButtonHeight, BsueType.Button, modifier, true, onClick)
}

// Weak — 카드 안 작은 행동. "오늘 해냈어", "QR로 합류", "영수증 보기"
@Composable
fun WeakButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) =
    BsueButtonBase(
        label, Role.PointWeakBg, Role.Point, BsueSpace.SmallButtonHeight,
        BsueType.Body2.copy(fontWeight = BsueType.Button.fontWeight), modifier, true, onClick,
    )

// Text — 배경 없는 버튼. "나중에 할래", "오늘은 쉬어", "+ 구독 추가"
@Composable
fun BsueTextButton(label: String, modifier: Modifier = Modifier, color: Color = Role.TextSecondary, onClick: () -> Unit) =
    BsueButtonBase(label, Color.Transparent, color, BsueSpace.SmallButtonHeight, BsueType.Body2, modifier, true, onClick)

// 하단 고정 버튼 (토스 CTA)
// 좌우 24 · 아래 16 + 시스템 바 · 위에 흰색 24 그라데이션 페이드
// 버튼 2개면 가로 1:2 (보조 왼쪽, 주 버튼 오른쪽). textAction 은 주 버튼 아래 텍스트 버튼.
@Composable
fun BottomCta(
    primary: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    textAction: String? = null,
    onTextAction: () -> Unit = {},
    primaryEnabled: Boolean = true,
    fadeColor: Color = Role.Surface,
) {
    Column(modifier.fillMaxWidth()) {
        // 위 페이드: 스크롤되는 내용이 버튼 뒤로 자연스럽게 사라지게
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(BsueSpace.X24)
                .background(Brush.verticalGradient(listOf(fadeColor.copy(alpha = 0f), fadeColor))),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .background(fadeColor)
                .navigationBarsPadding()
                .padding(start = BsueSpace.ScreenSide, end = BsueSpace.ScreenSide, bottom = BsueSpace.X16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (secondary != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(BsueSpace.X8)) {
                    SecondaryButton(secondary, Modifier.weight(1f), onClick = onSecondary)
                    PrimaryButton(primary, Modifier.weight(2f), primaryEnabled, onPrimary)
                }
            } else {
                PrimaryButton(primary, Modifier.fillMaxWidth(), primaryEnabled, onPrimary)
            }
            if (textAction != null) {
                BsueTextButton(textAction, onClick = onTextAction)
            }
        }
    }
}
