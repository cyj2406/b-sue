package com.bsue.blob.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.Guardian
import com.bsue.blob.ui.blob.BlobLook
import com.bsue.blob.ui.blob.BlobLooks
import com.bsue.blob.ui.blob.BlobMotion
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.SilhouetteGray
import com.bsue.blob.ui.theme.SilhouetteInk
import com.bsue.blob.ui.theme.contentColorOn

private val CardShape = RoundedCornerShape(14.dp)

// 카드 공통 틀: 둥근 모서리 + 바탕색 + 검정 테두리 (테두리가 바탕 위에 그려지도록 순서 중요)
private fun Modifier.cardFrame(color: Color) = this
    .clip(CardShape)
    .background(color)
    .border(2.dp, BlobBlack, CardShape)

// 해금된 수호신 카드 앞면. compact = 도감 그리드용 작은 크기
@Composable
fun GuardianCardFace(
    guardian: Guardian,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val ink = contentColorOn(guardian.color)
    Column(
        modifier = modifier
            .cardFrame(guardian.color)
            .padding(if (compact) 12.dp else 20.dp),
    ) {
        Text(
            text = "No.%02d · %s".format(guardian.number, guardian.type.label),
            fontFamily = GothicA1,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 13.sp,
            color = ink.copy(alpha = 0.75f),
        )
        // 수호신 미션 포즈 그림 (ui/blob/BlobPose.kt 의 missionArt 표)
        Image(
            painter = painterResource(BlobLooks.missionArt(guardian.id)),
            contentDescription = guardian.name,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxWidth().height(if (compact) 76.dp else 150.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = guardian.name,
            fontFamily = BlackHanSans,
            fontSize = if (compact) 17.sp else 28.sp,
            lineHeight = if (compact) 20.sp else 32.sp,
            color = ink,
        )
        Spacer(Modifier.height(if (compact) 6.dp else 12.dp))

        // 블롭의 멈칫 질문 (크림 말풍선)
        Text(
            text = "“${guardian.question}”",
            fontFamily = GothicA1,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 12.sp else 17.sp,
            color = BlobBlack,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BlobCream)
                .padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 10.dp),
        )
        Spacer(Modifier.height(if (compact) 6.dp else 14.dp))

        // 작은 카드는 그림이 들어가서 'MISSION' 제목 없이 2줄만
        if (!compact) {
            Text(
                text = "MISSION",
                fontFamily = BlackHanSans,
                fontSize = 13.sp,
                color = ink.copy(alpha = 0.75f),
            )
        }
        Text(
            text = guardian.mission,
            fontFamily = GothicA1,
            fontSize = if (compact) 11.sp else 15.sp,
            lineHeight = if (compact) 15.sp else 21.sp,
            color = ink,
            maxLines = if (compact) 2 else 4,
            overflow = TextOverflow.Ellipsis,
        )

        if (!compact) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "출동 순간 · ${guardian.trigger}",
                fontFamily = GothicA1,
                fontSize = 12.sp,
                color = ink.copy(alpha = 0.75f),
            )
        }
    }
}

// 아직 잠긴 카드: 회색 실루엣
@Composable
fun LockedGuardianCard(
    guardian: Guardian,
    modifier: Modifier = Modifier,
    hint: String = "아직 못 만난 수호신", // 예: "참기 1/3"
) {
    Column(
        modifier = modifier
            .cardFrame(SilhouetteGray)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No.%02d".format(guardian.number),
            fontFamily = GothicA1,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = SilhouetteInk,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // 그 수호신의 미션 포즈를 한 색 실루엣으로
            BlobCharacter(
                look = BlobLook(BlobLooks.missionArt(guardian.id), BlobMotion.NONE),
                modifier = Modifier.fillMaxSize().padding(vertical = 6.dp),
                silhouette = SilhouetteInk,
            )
        }
        Text(
            text = "???",
            fontFamily = BlackHanSans,
            fontSize = 20.sp,
            color = SilhouetteInk,
        )
        Text(
            text = hint,
            fontFamily = GothicA1,
            fontSize = 11.sp,
            color = SilhouetteInk,
        )
    }
}

// 뒤집기 전 미션카드 뒷면: 검정 + 경고테이프
@Composable
fun MissionCardBack(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .cardFrame(BlobBlack),
    ) {
        HazardTape(Modifier.fillMaxWidth().height(18.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "오늘의\n미션카드",
                fontFamily = BlackHanSans,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                color = BlobOrange,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "탭해서 뒤집기",
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BlobCream,
            )
        }
        HazardTape(Modifier.fillMaxWidth().height(18.dp))
    }
}

// 카드 뒤집기 애니메이션 (Y축 회전). 한 번 뒤집으면 그날은 앞면 고정.
@Composable
fun FlipCard(
    flipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    hidden: @Composable () -> Unit,
    revealed: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "flip",
    )
    val density = LocalDensity.current.density

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !flipped, onClickLabel = "미션카드 뒤집기") { onFlip() },
    ) {
        if (rotation <= 90f) {
            hidden()
        } else {
            // 뒤쪽 면은 거울처럼 보이지 않게 한 번 더 뒤집어 줌
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                revealed()
            }
        }
    }
}

// 숫자 하나 + 라벨 상자 (홈 카운터용)
@Composable
fun StatBox(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .cardFrame(BlobCream)
            .padding(14.dp),
    ) {
        Text(label, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BlobBlack)
        Spacer(Modifier.height(6.dp))
        Text(value, fontFamily = BlackHanSans, fontSize = 26.sp, color = BlobOrange, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(caption, fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f))
    }
}
