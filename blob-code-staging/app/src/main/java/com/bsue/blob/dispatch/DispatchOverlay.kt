package com.bsue.blob.dispatch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bsue.blob.data.Guardian
import com.bsue.blob.data.coffeeCupsLabel
import com.bsue.blob.data.formatWon
import com.bsue.blob.ui.blob.BlobLooks
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.BsueBottomSheet
import com.bsue.blob.ui.components.PrimaryButton
import com.bsue.blob.ui.components.SecondaryButton
import com.bsue.blob.ui.components.SecondaryOn
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobGray
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.BsueShape
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.floatingShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 출동 한 번에 필요한 정보
data class DispatchRequest(
    val guardian: Guardian,
    val amount: Int?,          // 화면에서 못 읽었으면 null
    val appLabel: String,      // 예: 쿠팡
    val isTest: Boolean = false,
)

enum class DispatchResult { HELD, BOUGHT } // 참았다 / 그래도 살래

// ① 멈칫(0~3초) → ② 선택 → ③ 결과 바텀시트
private enum class Step { PAUSE, CHOOSE, SHEET_HELD, SHEET_BOUGHT }

private const val PAUSE_MS = 3000
private val White = Color.White // Compose 기본 흰색 (팔레트에 새로 넣은 색 아님)

// ════════════════════════════════════════════════════════════════
//  출동 화면 — 토스 '레벨업 카드' 구조
//  어두운 한 색 배경 · 흰 제목 한 줄 · 가운데 카드 한 장 · 아래 큰 버튼
//  글자는 Pretendard 만 (Black Han Sans · 경고테이프 없음)
//  실제 출동(접근성 창)과 앱 안 '출동 테스트'가 같이 써요.
// ════════════════════════════════════════════════════════════════
@Composable
fun DispatchOverlay(
    request: DispatchRequest,
    savedThisMonth: Int? = null,          // 결과 시트 '이번 달 지킨 돈' (이번 금액을 더하기 전 값)
    onOpenReceipts: (() -> Unit)? = null, // '영수증 보기 ›' (없으면 링크 숨김)
    onFinished: (DispatchResult) -> Unit,
) {
    var step by remember { mutableStateOf(Step.PAUSE) }
    var count by remember { mutableIntStateOf(3) }
    val progress = remember { Animatable(0f) }

    // 3초: 버튼 안이 왼쪽부터 차오르고 3·2·1
    LaunchedEffect(Unit) {
        launch { progress.animateTo(1f, tween(PAUSE_MS, easing = LinearEasing)) }
        for (i in 3 downTo 1) {
            count = i
            delay(1000)
        }
        step = Step.CHOOSE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 뒤 화면 글자가 읽히지 않게 94% 로 덮기 (접근성 창에선 블러도 같이)
            .background(BlobBlack.copy(alpha = 0.94f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = BsueSpace.ScreenSide),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            // 위: 작은 보조 문구 + 흰 제목 (화면에서 가장 큰 글자는 이 하나)
            Text("잠깐, 딱 3초", style = BsueType.Caption, color = White.copy(alpha = 0.6f))
            Spacer(Modifier.height(BsueSpace.X8))
            Text("정말 긁을 거야?", style = BsueType.Title1, color = White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(BsueSpace.X32))

            // 가운데: 카드 한 장
            HeroCard(request = request, count = count)

            Spacer(Modifier.weight(1f))

            // 아래: 진행 버튼 → (3초 뒤) 버튼 두 개
            AnimatedContent(
                targetState = step == Step.PAUSE,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "bottomAction",
            ) { pausing ->
                if (pausing) {
                    ProgressButton(progress = progress.value, count = count)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(BsueSpace.X8)) {
                        // 보조는 왼쪽(1), 주 버튼은 오른쪽(2) — 토스 순서
                        SecondaryButton("그래도 살래", Modifier.weight(1f), on = SecondaryOn.Dark) { step = Step.SHEET_BOUGHT }
                        PrimaryButton("참았다!", Modifier.weight(2f)) { step = Step.SHEET_HELD }
                    }
                }
            }
            Spacer(Modifier.height(BsueSpace.X16))
        }

        // ③ 결과 바텀시트
        when (step) {
            Step.SHEET_HELD -> BsueBottomSheet(
                title = request.amount?.let { "${wonPlain(it)}원 지켰어!" } ?: "참았다! 멋져",
                onDismiss = { onFinished(DispatchResult.HELD) },
            ) {
                val total = (savedThisMonth ?: 0) + (request.amount ?: 0)
                SheetPreviewCard(scene = BlobScene.HELD) {
                    Text("이번 달 지킨 돈", style = BsueType.Caption, color = BlobGray)
                    Text("${wonPlain(total)}원", style = BsueType.Title3, color = BlobBlack)
                    Text("커피 ${coffeeCupsLabel(total)}잔", style = BsueType.Caption, color = BlobOrange)
                }
                if (onOpenReceipts != null) {
                    Spacer(Modifier.height(BsueSpace.X16))
                    Text(
                        "영수증 보기 ›",
                        style = BsueType.Body2,
                        color = BlobOrange,
                        modifier = Modifier.clickable {
                            onFinished(DispatchResult.HELD)
                            onOpenReceipts()
                        },
                    )
                }
                Spacer(Modifier.height(BsueSpace.X24))
                Row(horizontalArrangement = Arrangement.spacedBy(BsueSpace.X8)) {
                    SecondaryButton("닫기", Modifier.weight(1f)) { onFinished(DispatchResult.HELD) }
                    PrimaryButton("쇼핑 그만하기", Modifier.weight(2f)) { onFinished(DispatchResult.HELD) }
                }
            }
            Step.SHEET_BOUGHT -> BsueBottomSheet(
                title = "못 참아도 괜찮아",
                onDismiss = { onFinished(DispatchResult.BOUGHT) },
            ) {
                SheetPreviewCard(scene = BlobScene.BOUGHT) {
                    Text("대신 잘 쓰기!", style = BsueType.Body1, color = BlobBlack)
                    Text("다음에 또 3초만 같이 멈칫하자", style = BsueType.Caption, color = BlobGray)
                }
                Spacer(Modifier.height(BsueSpace.X24))
                PrimaryButton("확인", Modifier.fillMaxWidth()) { onFinished(DispatchResult.BOUGHT) }
            }
            else -> Unit
        }
    }
}

// ── 가운데 카드: 걸친 말풍선 + 블롭 + 이름 배지 + 금액 ────────────────
@Composable
private fun HeroCard(request: DispatchRequest, count: Int) {
    val enter = remember { Animatable(0f) }  // 카드: 아래에서 살짝 올라오며 (250ms)
    val bubble = remember { Animatable(0f) } // 말풍선: 0.3초 뒤에 팝
    LaunchedEffect(Unit) {
        launch { enter.animateTo(1f, tween(250, easing = FastOutSlowInEasing)) }
        delay(300)
        bubble.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
    }

    Box(
        modifier = Modifier
            .width(280.dp)
            .graphicsLayer {
                translationY = (1f - enter.value) * 40.dp.toPx()
                alpha = enter.value
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        // 카드 (흰색 → 포인트색 아주 옅게)
        Column(
            modifier = Modifier
                .padding(top = 26.dp) // 말풍선이 카드 위에 걸치도록
                .fillMaxWidth()
                .clip(BsueShape.HeroCard)
                .background(Brush.verticalGradient(listOf(White, BlobOrange.copy(alpha = 0.07f).compositeOn(White))))
                .padding(top = BsueSpace.X40, bottom = BsueSpace.X24, start = BsueSpace.X24, end = BsueSpace.X24),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 블롭: 카드 안에서 통 튀어나옴 (등장 squash), 숫자마다 살짝 꾹
            BlobCharacter(
                scene = BlobScene.DISPATCH,
                motionKey = count,
                modifier = Modifier.height(140.dp).aspectRatio(BlobLooks.ASPECT),
            )
            Spacer(Modifier.height(BsueSpace.X12))
            // 수호신 이름 배지
            Text(
                request.guardian.name,
                style = BsueType.Small,
                color = BlobOrange,
                modifier = Modifier
                    .clip(BsueShape.Pill)
                    .background(BlobOrange.copy(alpha = 0.1f))
                    .padding(horizontal = BsueSpace.X12, vertical = BsueSpace.X4),
            )
            Spacer(Modifier.height(BsueSpace.X12))
            if (request.amount != null) {
                Text("${wonPlain(request.amount)}원", style = BsueType.Display, color = BlobBlack)
                Text(
                    "${request.appLabel} · 커피 ${coffeeCupsLabel(request.amount)}잔",
                    style = BsueType.Caption,
                    color = BlobGray,
                )
            } else {
                Text("얼마인지 한 번 더 볼래?", style = BsueType.Title3, color = BlobBlack)
                Text(request.appLabel, style = BsueType.Caption, color = BlobGray)
            }
        }

        // 카드 위에 걸친 흰 말풍선: 담당 수호신의 질문
        SpeechBubbleDown(
            text = request.guardian.question,
            modifier = Modifier.graphicsLayer {
                val s = 0.6f + 0.4f * bubble.value
                scaleX = s
                scaleY = s
                alpha = bubble.value.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
        )
    }
}

// 아래로 꼬리가 달린 흰 말풍선
@Composable
private fun SpeechBubbleDown(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text,
            style = BsueType.Body2,
            color = BlobBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 240.dp)
                .floatingShadow(shape)
                .clip(shape)
                .background(White)
                .padding(horizontal = BsueSpace.X16, vertical = 14.dp),
        )
        // 꼬리
        Canvas(Modifier.size(width = 16.dp, height = 8.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2, size.height)
                close()
            }
            drawPath(path, White)
        }
    }
}

// ── 3초 진행 버튼: 비활성 + 왼쪽부터 차오름 + 숫자 ──────────────────
@Composable
private fun ProgressButton(progress: Float, count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BsueSpace.ButtonHeight)
            .clip(BsueShape.Button)
            .background(White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(BlobOrange.copy(alpha = 0.45f)),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("3초만 생각해보자", style = BsueType.Button, color = White.copy(alpha = 0.75f))
            Spacer(Modifier.width(BsueSpace.X8))
            AnimatedContent(
                targetState = count,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 1.4f)) togetherWith fadeOut() },
                label = "count",
            ) { n ->
                Text("$n", style = BsueType.Button, color = White)
            }
        }
    }
}

// ── 결과 바텀시트는 공통 BsueBottomSheet (ui/components/BsueSheet.kt) ──

// 시트 안 미리보기 카드: 블롭(환호/괜찮아) + 글자
@Composable
private fun SheetPreviewCard(scene: BlobScene, text: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BsueShape.Card)
            .background(BlobCream)
            .padding(BsueSpace.X20),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BlobCharacter(scene = scene, modifier = Modifier.size(96.dp))
        Spacer(Modifier.width(BsueSpace.X16))
        Column { text() }
    }
}

// ── 버튼은 공통 PrimaryButton · SecondaryButton (ui/components/BsueButtons.kt) ──

// 12900 → "12,900" (원 표시 없이)
private fun wonPlain(amount: Int): String = formatWon(amount).removePrefix("₩")

// 반투명 색을 흰 바탕 위에 칠했을 때의 색 (그라데이션 끝색 — 새 색이 아니라 투명도 계산)
private fun Color.compositeOn(base: Color): Color = Color(
    red = red * alpha + base.red * (1 - alpha),
    green = green * alpha + base.green * (1 - alpha),
    blue = blue * alpha + base.blue * (1 - alpha),
    alpha = 1f,
)
