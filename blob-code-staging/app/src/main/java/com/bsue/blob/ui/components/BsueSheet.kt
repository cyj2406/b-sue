package com.bsue.blob.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bsue.blob.ui.theme.BsueShape
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.Role
import kotlinx.coroutines.delay

// 기본 전환: 250ms, ease-out (0.2, 0, 0, 1) — DESIGN.md 7번
val BsueEase = CubicBezierEasing(0.2f, 0f, 0f, 1f)
const val BSUE_TRANSITION_MS = 250

// ════════════════════════════════════════════════
//  바텀시트 — 위 모서리 28 · 손잡이 36×4 · 안쪽 24 · 뒤 dim
//  결과(환호/괜찮아), 요원 상세, 구독 상세, 미션 체크에 써요.
//  화면 맨 위(Box 안 가장 마지막)에 두면 돼요.
// ════════════════════════════════════════════════
@Composable
fun BsueBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Role.Dim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(tween(BSUE_TRANSITION_MS, easing = BsueEase)) { it },
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(BsueShape.SheetTop)
                    .background(Role.Surface)
                    // 시트 안을 눌러도 닫히지 않게
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                    .navigationBarsPadding()
                    .padding(start = BsueSpace.X24, end = BsueSpace.X24, top = BsueSpace.X12, bottom = BsueSpace.X16),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(BsueShape.Pill)
                        .background(Role.Divider),
                )
                Spacer(Modifier.height(BsueSpace.X24))
                if (title != null) {
                    Text(title, style = BsueType.Title1, color = Role.TextPrimary)
                    Spacer(Modifier.height(BsueSpace.X20))
                }
                content()
            }
        }
    }
}

// ════════════════════════════════════════════════
//  토스트 — 진한 바탕 · 흰 글씨 body2 · 모서리 12 · 탭바 위 16 · 2초
//  쓰는 법:
//    val toast = rememberToastState()
//    toast.show("갤러리에 저장했어")
//    ToastHost(toast, bottomPadding = 탭바 높이)   ← 화면 맨 위 Box 안에
// ════════════════════════════════════════════════
@Stable
class ToastState {
    var message by mutableStateOf<String?>(null)
        private set
    var id by mutableStateOf(0L)
        private set

    fun show(text: String) {
        message = text
        id = System.nanoTime()
    }

    internal fun clear() {
        message = null
    }
}

@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

@Composable
fun ToastHost(state: ToastState, modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    LaunchedEffect(state.id) {
        if (state.message != null) {
            delay(2000)
            state.clear()
        }
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = state.message != null,
            enter = fadeIn(tween(BSUE_TRANSITION_MS)) + slideInVertically(tween(BSUE_TRANSITION_MS, easing = BsueEase)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
        ) {
            Text(
                state.message.orEmpty(),
                style = BsueType.Body2,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier
                    .padding(start = BsueSpace.ScreenSide, end = BsueSpace.ScreenSide, bottom = bottomPadding + BsueSpace.X16)
                    .fillMaxWidth()
                    .clip(BsueShape.Toast)
                    .background(Role.TextPrimary)
                    .padding(horizontal = BsueSpace.X16, vertical = 14.dp),
            )
        }
    }
}
