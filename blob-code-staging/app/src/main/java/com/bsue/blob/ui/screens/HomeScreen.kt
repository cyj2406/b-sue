package com.bsue.blob.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.Guardian
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.coffeeCups
import com.bsue.blob.data.formatWon
import com.bsue.blob.leak.SubscriptionTotalCard
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.FlipCard
import com.bsue.blob.ui.components.GuardianCardFace
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.components.MissionCardBack
import com.bsue.blob.ui.components.StatBox
import com.bsue.blob.ui.components.WarningBadge
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 홈: 블롭이 사는 '탐정 사무실'
@Composable
fun HomeScreen(
    state: BlobState,
    onFlipTodayCard: () -> Unit,
    onMissionResult: (done: Boolean) -> Unit,
    onOpenMarket: () -> Unit,
    onOpenLeakCheck: () -> Unit,
    onSecretTap: () -> Unit, // '탐정 사무실' 글자 5번 연속 탭 → 관리자 메뉴
) {
    val todays = remember { Guardians.todays() }
    // 5번 연속 탭 세기 (탭 사이가 1초 넘게 벌어지면 처음부터)
    val taps = remember { mutableListOf<Long>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // 간판
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "탐정 사무실", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobBlack,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // 눌러도 티 안 나게
                ) {
                    val now = SystemClock.uptimeMillis()
                    if (taps.isNotEmpty() && now - taps.last() > 1000) taps.clear()
                    taps.add(now)
                    if (taps.size >= 5) {
                        taps.clear()
                        onSecretTap()
                    }
                },
            )
            Spacer(Modifier.weight(1f))
            WarningBadge("지출주의")
        }
        Spacer(Modifier.height(10.dp))
        HazardTape(Modifier.fillMaxWidth().height(10.dp))
        Spacer(Modifier.height(16.dp))

        // 블롭 + 말풍선
        Row(verticalAlignment = Alignment.CenterVertically) {
            BlobCharacter(BlobScene.HOME, Modifier.size(120.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                text = blobLine(state, todays),
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = BlobCream,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BlobBlack)
                    .padding(14.dp),
            )
        }
        Spacer(Modifier.height(16.dp))

        // 카운터 2개
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(
                label = "이번 달 지킨 돈",
                value = formatWon(state.savedThisMonth),
                caption = "커피 ${coffeeCups(state.savedThisMonth)}잔",
                modifier = Modifier.weight(1f),
            )
            StatBox(
                label = "오늘 출동",
                value = "${state.sortiesToday}/${state.dailyLimit}",
                caption = if (state.restingToday) "오늘은 쉬는 중" else "하루 최대 ${state.dailyLimit}번",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))

        // 월 총 구독료 (커피 ○잔) → 누수 점검
        SubscriptionTotalCard(
            total = state.monthlySubscriptionTotal,
            count = state.subscriptions.size,
            onClick = onOpenLeakCheck,
        )
        Spacer(Modifier.height(12.dp))

        // 전시 체험: 가짜 쇼핑 '지름마켓'
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BlobOrange)
                .clickable(onClick = onOpenMarket)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("지름마켓 체험하기", fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobCream)
                Spacer(Modifier.height(2.dp))
                Text(
                    "가짜 쇼핑에서 [결제하기]를 눌러봐. 내가 출동할게!",
                    fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlobCream,
                )
            }
            Text("›", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobCream)
        }
        Spacer(Modifier.height(24.dp))

        // 오늘의 미션카드
        Text("오늘의 미션카드", fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobBlack)
        Spacer(Modifier.height(12.dp))
        FlipCard(
            flipped = state.flippedToday,
            onFlip = onFlipTodayCard,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .aspectRatio(0.7f)
                .align(Alignment.CenterHorizontally),
            hidden = { MissionCardBack(Modifier.fillMaxSize()) },
            revealed = { GuardianCardFace(todays, Modifier.fillMaxSize()) },
        )
        Spacer(Modifier.height(14.dp))
        MissionResult(state, todays, onMissionResult)
        Spacer(Modifier.height(24.dp))
    }
}

// 미션 결과: [해냈다] → 해금 / [내일 다시]. 밤 10시 알림에서도 고를 수 있어요
@Composable
private fun MissionResult(state: BlobState, todays: Guardian, onMissionResult: (Boolean) -> Unit) {
    val unlocked = todays.id in state.unlockedIds
    val message = when {
        !state.flippedToday -> "미션을 해내면 이 수호신이 도감에 들어와!"
        state.missionResultToday == BlobState.MISSION_DONE -> "해냈다! 오늘 정말 멋졌어."
        state.missionResultToday == BlobState.MISSION_LATER -> "괜찮아, 내일 다시 해보자!"
        else -> "오늘 미션 어땠어?" + if (unlocked) "" else "\n해내면 도감에 들어와!"
    }
    Text(
        message,
        fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 21.sp,
        color = BlobBlack, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    if (state.flippedToday && state.missionResultToday == null) {
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onMissionResult(true) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
            ) {
                Text("해냈다", fontFamily = BlackHanSans, fontSize = 18.sp)
            }
            OutlinedButton(
                onClick = { onMissionResult(false) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, BlobBlack),
            ) {
                Text("내일 다시", fontFamily = BlackHanSans, fontSize = 18.sp, color = BlobBlack)
            }
        }
    }
}

// 블롭 말풍선 대사 (톤: 친근한 반말, 질문형)
private fun blobLine(state: BlobState, todays: Guardian): String = when {
    state.restingToday -> "오늘은 쉬는 날이야.\n푹 쉬어, 나도 쉴게!"
    !state.flippedToday -> "오늘 나랑 같이할 수호신,\n누군지 볼래?"
    else -> "오늘의 질문!\n${todays.question}"
}
