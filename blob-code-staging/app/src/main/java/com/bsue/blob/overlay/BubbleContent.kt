package com.bsue.blob.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.coffeeCupsLabel
import com.bsue.blob.data.formatWon
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 블롭 크기 / 창 크기 (창이 조금 더 커야 말랑하게 늘어날 때 안 잘려요)
const val BLOB_DP = 68
const val BUBBLE_WINDOW_DP = 88

// 서비스가 바꾸고, 화면이 따라 그리는 상태
class BubbleUiState {
    var dragging by mutableStateOf(false)
    var dockedRight by mutableStateOf(true)
    var resting by mutableStateOf(false)
    var squish by mutableIntStateOf(0) // 가장자리에 붙을 때마다 +1 → 말랑 애니메이션
    var peek by mutableIntStateOf(0)   // 빼꼼할 때마다 +1 → 고개 갸웃 애니메이션
    var scene by mutableStateOf(BlobScene.FLOATING) // 지금 어떤 포즈인지 (평소 / 빼꼼 / 배웅)
}

// 떠 있는 블롭
@Composable
fun FloatingBlob(ui: BubbleUiState) {
    // 벽에 붙는 순간: 납작하게 눌렸다가 통통 튀며 원래대로
    val wobble = remember { Animatable(1f) }
    LaunchedEffect(ui.squish) {
        if (ui.squish > 0) {
            wobble.snapTo(0.78f)
            wobble.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.28f, stiffness = Spring.StiffnessMediumLow),
            )
        }
    }
    // 빼꼼: 화면 안쪽으로 고개를 살짝 갸웃했다가 똑바로
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(ui.peek) {
        if (ui.peek > 0) {
            tilt.snapTo(if (ui.dockedRight) -10f else 10f)
            tilt.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessLow),
            )
        }
    }
    // 잡고 있는 동안 살짝 커짐
    val holdScale by animateFloatAsState(
        targetValue = if (ui.dragging) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "hold",
    )
    // 쉬는 중이면 반투명
    val alpha by animateFloatAsState(if (ui.resting) 0.55f else 1f, label = "rest")

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 포즈는 ui/blob/BlobPose.kt 표대로 (빼꼼은 오른쪽 벽이면 좌우 반전)
        BlobCharacter(
            scene = if (ui.resting && ui.scene == BlobScene.FLOATING) BlobScene.FLOATING_RESTING else ui.scene,
            fromRight = ui.dockedRight,
            modifier = Modifier
                .size(BLOB_DP.dp)
                .graphicsLayer {
                    scaleY = holdScale * wobble.value
                    scaleX = holdScale * (2f - wobble.value)
                    this.alpha = alpha
                    rotationZ = tilt.value
                    // 붙은 벽 쪽을 기준으로 눌림
                    transformOrigin = TransformOrigin(if (ui.dockedRight) 1f else 0f, 0.6f)
                },
        )
    }
}

// 블롭 옆에 뜨는 말풍선
// - 톡 했을 때: 대사 + "사무실 가기 ›" (누르면 앱 열림)
// - 빼꼼했을 때: 수호신 그림 + 이름(카드 색) + 멈칫 질문 (누르면 닫힘)
@Composable
fun SpeechBubble(
    text: String,
    onClick: () -> Unit,
    title: String? = null,
    accent: Color? = null,
    showOfficeLink: Boolean = true,
    @DrawableRes art: Int? = null,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BlobBlack)
            .clickable(onClick = onClick),
    ) {
        if (accent != null) {
            Box(Modifier.fillMaxWidth().height(6.dp).background(accent))
        }
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (art != null) {
            Image(
                painter = painterResource(art),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                modifier = Modifier.size(58.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Column {
            if (title != null) {
                Text(title, fontFamily = BlackHanSans, fontSize = 13.sp, color = accent ?: BlobOrange)
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = text,
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = if (title != null) 17.sp else 14.sp,
                lineHeight = if (title != null) 23.sp else 20.sp,
                color = BlobCream,
            )
            if (showOfficeLink) {
                Spacer(Modifier.height(6.dp))
                Text("사무실 가기 ›", fontFamily = BlackHanSans, fontSize = 13.sp, color = BlobOrange)
            }
        }
        }
    }
}

// 톡 했을 때 블롭 대사 (톤: 친근한 반말, 다정하게)
val TapLines = listOf(
    "나 여기서 지켜보고 있을게.\n결제 직전에 딱 3초만!",
    "오늘의 미션카드, 벌써 뒤집어봤어?",
    "못 참아도 괜찮아.\n고르는 건 언제나 너야!",
    "심심하면 사무실 놀러 와.\n도감 같이 보자!",
)
const val SleepingLine = "쿨쿨… 오늘은 쉬는 중이야.\n길게 누르면 다시 일어날게!"
const val GoRestLine = "알겠어, 오늘은 쉴게.\n내일 다시 출동!"
const val WakeUpLine = "다시 출동 준비 완료!"

// 배웅 대사 (결제 알림을 받았을 때) — 혼내지 않고 손 흔들어 보내주기
fun farewellLine(amount: Int?, merchant: String?): String {
    val money = amount?.let { formatWon(it) } ?: "돈"
    val cups = amount?.let { " (커피 ${coffeeCupsLabel(it)}잔)" }.orEmpty()
    return if (merchant != null) {
        "잘 가, $money$cups…\n${merchant}에서 잘 쓰고 와!"
    } else {
        "잘 가, $money$cups…\n손 흔들어 줄게. 잘 쓰기!"
    }
}
