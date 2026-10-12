package com.bsue.blob.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.Guardian
import com.bsue.blob.data.GuardianType
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.Confetti
import com.bsue.blob.ui.components.FlipCard
import com.bsue.blob.ui.components.GuardianCardFace
import com.bsue.blob.ui.components.MissionCardBack
import com.bsue.blob.ui.components.MovingHazardTape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.TapeYellow
import com.bsue.blob.ui.theme.contentColorOn
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 공통: 어두운 전체화면 판 (뒤 화면 터치 막기)
@Composable
private fun CelebrationBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobBlack.copy(alpha = 0.9f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// 블롭: cheering = true 면 환호 포즈(점프 2번 + 반짝), 아니면 기본 포즈
// 포즈가 바뀌는 순간 0.15초 크로스페이드 + 살짝 튐 (BlobCharacter가 처리)
@Composable
private fun CheeringBlob(size: Int, cheering: Boolean) {
    BlobCharacter(
        scene = if (cheering) BlobScene.UNLOCK else BlobScene.HOME,
        modifier = Modifier.size(size.dp),
    )
}

// ── 해금 연출: 카드 뒤집힘 + 블롭 환호 ─────────────────────

@Composable
fun UnlockCelebration(
    guardian: Guardian,
    title: String = "새 수호신 해금!",
    onDone: () -> Unit,
) {
    var flipped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600) // 뒷면을 잠깐 보여주고
        flipped = true
    }
    BackHandler(onBack = onDone)

    CelebrationBackdrop {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text(title, fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobOrange)
            Spacer(Modifier.height(16.dp))
            FlipCard(
                flipped = flipped,
                onFlip = { flipped = true },
                modifier = Modifier.width(210.dp).aspectRatio(0.66f),
                hidden = { MissionCardBack(Modifier.fillMaxSize()) },
                revealed = { GuardianCardFace(guardian, Modifier.fillMaxSize(), compact = true) },
            )
            Spacer(Modifier.height(4.dp))
            Box(contentAlignment = Alignment.Center) {
                CheeringBlob(size = 120, cheering = flipped)
                if (flipped) Confetti(Modifier.matchParentSize(), accent = guardian.color, count = 36)
            }
            Text(
                "No.%02d ${guardian.name}".format(guardian.number),
                fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobCream,
            )
            Spacer(Modifier.height(4.dp))
            Text("도감에 들어왔어!", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlobCream)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
            ) {
                Text("도감에 넣기", fontFamily = BlackHanSans, fontSize = 18.sp)
            }
        }
    }
}

// ── 카드홀더 QR: 블롭 소환! ─────────────────────────────

@Composable
fun SummonCelebration(onDone: () -> Unit) {
    val spin = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        spin.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
    }
    BackHandler(onBack = onDone)

    CelebrationBackdrop {
        Column(Modifier.fillMaxSize()) {
            MovingHazardTape(Modifier.fillMaxWidth().height(26.dp))
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("블롭 소환!", fontFamily = BlackHanSans, fontSize = 46.sp, color = BlobOrange)
                Spacer(Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center) {
                    // 빙글빙글 돌면서 커지며 등장
                    Box(
                        Modifier.graphicsLayer {
                            scaleX = spin.value
                            scaleY = spin.value
                            rotationZ = (1f - spin.value) * 720f
                        },
                    ) { CheeringBlob(size = 180, cheering = spin.value > 0.95f) }
                    if (spin.value > 0.6f) Confetti(Modifier.matchParentSize(), accent = TapeYellow, count = 40)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "불렀어? 탐정 블롭,\n출동 준비 완료!",
                    fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp,
                    color = BlobCream, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
                ) {
                    Text("좋아!", fontFamily = BlackHanSans, fontSize = 18.sp)
                }
            }
            MovingHazardTape(Modifier.fillMaxWidth().height(26.dp), reverse = true)
        }
    }
}

// ── 6신 완성: 포토카드 교환 화면 ──────────────────────────
// 화면을 캡처해서 재사용하지 못하게 테이프가 흐르고 시계가 초 단위로 움직여요

@Composable
fun PhotoCardScreen(
    type: GuardianType,
    guardians: List<Guardian>,
    onClose: () -> Unit,
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }
    BackHandler(onBack = onClose)

    Box(Modifier.fillMaxSize().background(BlobCream).systemBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            MovingHazardTape(Modifier.fillMaxWidth().height(30.dp))
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("${type.label} 수호신 6신 완성!", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobOrange)
                Spacer(Modifier.height(16.dp))
                // 6장 미니 카드 (3 x 2)
                guardians.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        row.forEach { g ->
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .aspectRatio(0.72f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(g.color)
                                    .border(2.dp, BlobBlack, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                            ) {
                                Text("No.%02d".format(g.number), fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = contentColorOn(g.color))
                                Spacer(Modifier.weight(1f))
                                Text(g.name, fontFamily = BlackHanSans, fontSize = 13.sp, lineHeight = 16.sp, color = contentColorOn(g.color))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                CheeringBlob(size = 100, cheering = true)
                Text(
                    "전시장 스태프에게\n이 화면을 보여줘!",
                    fontFamily = BlackHanSans, fontSize = 26.sp, lineHeight = 32.sp,
                    color = BlobBlack, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text("포토카드를 줄게 📸", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BlobBlack)
                Spacer(Modifier.height(14.dp))
                // 살아 있는 시계 (스크린샷이 아니라는 표시)
                Text(
                    now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd  HH:mm:ss")),
                    fontFamily = BlackHanSans, fontSize = 22.sp, color = BlobCream,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlobBlack)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
                ) {
                    Text("닫기", fontFamily = BlackHanSans, fontSize = 17.sp)
                }
            }
            MovingHazardTape(Modifier.fillMaxWidth().height(30.dp), reverse = true)
        }
    }
}
