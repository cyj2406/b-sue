package com.bsue.blob.ui.blob

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 캐릭터 테스트: 그림 18장 × 움직임 7개를 골라서 보기 + 지금 앱에서 어디에 무엇을 쓰는지
// (관리자 메뉴 ▸ 캐릭터 테스트)
@Composable
fun BlobPoseLab(onClose: () -> Unit) {
    var image by remember { mutableIntStateOf(BlobPose.IDLE.image) }
    var motion by remember { mutableStateOf(BlobMotion.BREATHE) }
    var mirrored by remember { mutableStateOf(false) }
    var darkBackground by remember { mutableStateOf(false) }
    var replay by remember { mutableIntStateOf(0) } // 한 번짜리 움직임 다시 재생
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobCream)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("캐릭터 테스트", fontFamily = BlackHanSans, fontSize = 26.sp, color = BlobBlack, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("닫기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack) }
        }

        // 미리보기 (바닥선 표시: 발이 이 선 위에 고정되는지 확인)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (darkBackground) BlobBlack else BlobCream)
                .border(2.dp, BlobBlack, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            BlobCharacter(
                look = BlobLook(image, motion, mirrored),
                motionKey = replay,
                modifier = Modifier.padding(bottom = 24.dp).width(240.dp).aspectRatio(BlobLooks.ASPECT),
            )
            Box(Modifier.padding(bottom = 24.dp).fillMaxWidth().height(1.dp).background(BlobOrange))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${BlobLooks.imageName(image)} · ${motion.label}" + if (mirrored) " · 좌우 반전" else "",
            fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlobBlack,
        )

        // 움직임 7개
        LabTitle("움직임")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BlobMotion.entries.forEach { m ->
                Chip(m.label, selected = m == motion) {
                    motion = m
                    replay++
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { replay++ },
                colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
            ) { Text("다시 재생 / 숫자 넘기기", fontFamily = GothicA1, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.weight(1f))
            Text("반전", fontFamily = GothicA1, fontSize = 13.sp, color = BlobBlack)
            Switch(mirrored, { mirrored = it }, colors = SwitchDefaults.colors(checkedTrackColor = BlobOrange))
            Spacer(Modifier.width(6.dp))
            Text("어둡게", fontFamily = GothicA1, fontSize = 13.sp, color = BlobBlack)
            Switch(darkBackground, { darkBackground = it }, colors = SwitchDefaults.colors(checkedTrackColor = BlobOrange))
        }

        // 그림 18장
        LabTitle("그림 18장")
        BlobLooks.allImages.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                row.forEach { (name, res) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (res == image) BlobOrange.copy(alpha = 0.2f) else BlobCream)
                            .border(if (res == image) 2.dp else 1.dp, if (res == image) BlobOrange else BlobBlack.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .clickable {
                                image = res
                                replay++
                            }
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painterResource(res), contentDescription = name,
                            contentScale = ContentScale.Fit, alignment = Alignment.BottomCenter,
                            modifier = Modifier.fillMaxWidth().aspectRatio(BlobLooks.ASPECT),
                        )
                        Text(name.removePrefix("blob_"), fontFamily = GothicA1, fontSize = 10.sp, color = BlobBlack, maxLines = 1)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }

        // 지금 앱이 쓰는 매핑 (BlobPose.kt 의 표를 그대로 보여줌) — 누르면 미리보기에 적용
        LabTitle("앱에서 쓰는 곳 (BlobPose.kt)")
        BlobScene.entries.forEach { scene ->
            val look = BlobLooks.of(scene, fromRight = scene == BlobScene.PEEK)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        image = look.image
                        motion = look.motion
                        mirrored = look.mirrored
                        replay++
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painterResource(look.image), contentDescription = null,
                    contentScale = ContentScale.Fit, modifier = Modifier.size(36.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(scene.label, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlobBlack)
                    Text(
                        "${BlobLooks.imageName(look.image)} · ${look.motion.label}" + if (scene == BlobScene.PEEK) " · 오른쪽 벽이면 반전" else "",
                        fontFamily = GothicA1, fontSize = 11.sp, color = BlobBlack.copy(alpha = 0.65f),
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LabTitle(text: String) {
    Spacer(Modifier.height(18.dp))
    Text(text, fontFamily = BlackHanSans, fontSize = 17.sp, color = BlobOrange)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontFamily = GothicA1,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = if (selected) BlobCream else BlobBlack,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) BlobBlack else BlobCream)
            .border(1.dp, BlobBlack, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
