package com.bsue.blob.ui.exhibition

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.MovingHazardTape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

const val GUIDE_SECONDS = 30

// 전시 모드 시작 가이드: "지름마켓에서 아무거나 결제해봐" (30초 뒤 저절로 지름마켓으로)
@Composable
fun ExhibitionGuide(
    onStart: () -> Unit,   // [지름마켓 가기] 또는 30초 끝
    onSkip: () -> Unit,    // [그냥 둘러볼래]
) {
    val timer = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        timer.animateTo(0f, tween(GUIDE_SECONDS * 1000, easing = LinearEasing))
        onStart()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobCream)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .systemBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            MovingHazardTape(Modifier.fillMaxWidth().height(24.dp))
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BlobCharacter(BlobScene.ONBOARDING, Modifier.size(150.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    "안녕! 나는 탐정 블롭이야",
                    fontFamily = BlackHanSans, fontSize = 26.sp, color = BlobBlack, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "지름마켓에서 아무거나\n결제해봐!",
                    fontFamily = BlackHanSans, fontSize = 34.sp, lineHeight = 40.sp,
                    color = BlobOrange, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(22.dp))
                GuideStep(1, "마음에 드는 상품 고르기")
                GuideStep(2, "[결제하기] 누르기 (진짜 결제 아니야)")
                GuideStep(3, "내가 출동하면 3초만 멈칫!")
                GuideStep(4, "[참았다!] 또는 [그래도 살래] 고르기")
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
                ) {
                    Text("지름마켓 가기", fontFamily = BlackHanSans, fontSize = 20.sp)
                }
                TextButton(onClick = onSkip) {
                    Text("그냥 둘러볼래", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
                }
                Spacer(Modifier.height(8.dp))
                // 30초 남은 시간 막대
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BlobBlack.copy(alpha = 0.12f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(timer.value)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(BlobOrange),
                    )
                }
            }
            MovingHazardTape(Modifier.fillMaxWidth().height(24.dp), reverse = true)
        }
    }
}

@Composable
private fun GuideStep(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(BlobBlack),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", fontFamily = BlackHanSans, fontSize = 15.sp, color = BlobOrange)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlobBlack)
    }
}
