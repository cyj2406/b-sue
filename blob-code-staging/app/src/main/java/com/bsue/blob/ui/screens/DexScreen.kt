package com.bsue.blob.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.GuardianType
import com.bsue.blob.data.Guardians
import com.bsue.blob.ui.components.GuardianCardFace
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.components.LockedGuardianCard
import com.bsue.blob.ui.components.WarningBadge
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1

// 도감: 12 수호신 (충동 6 / 누수 6)
@Composable
fun DexScreen(
    state: BlobState,
    onScanQr: () -> Unit,
    onShowPhotocard: (GuardianType) -> Unit,
) {
    val unlockedCount = Guardians.all.count { it.id in state.unlockedIds }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 머리말
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("수호신 도감", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobBlack)
                    Spacer(Modifier.weight(1f))
                    WarningBadge("$unlockedCount / ${Guardians.all.size}")
                }
                Spacer(Modifier.height(10.dp))
                HazardTape(Modifier.fillMaxWidth().height(10.dp))
                Spacer(Modifier.height(12.dp))
                // 실물 카드 QR 스캔
                Button(
                    onClick = onScanQr,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
                ) {
                    Text("실물 카드 QR 스캔", fontFamily = BlackHanSans, fontSize = 17.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "해금 방법: 오늘의 미션 [해냈다] · 그 수호신으로 ${BlobState.HOLDS_TO_UNLOCK}번 참기 · 실물 카드 QR",
                    fontFamily = GothicA1, fontSize = 12.sp, lineHeight = 17.sp, color = BlobBlack.copy(alpha = 0.7f),
                )
            }
        }

        GuardianType.entries.forEach { type ->
            val group = Guardians.ofType(type)
            val groupUnlocked = group.count { it.id in state.unlockedIds }

            // 구역 제목: 충동 / 누수
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("${type.label} 수호신", fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobOrange)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "$groupUnlocked / ${group.size} 해금",
                        fontFamily = GothicA1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BlobBlack,
                    )
                }
            }

            // 6신 다 모으면 포토카드 버튼
            if (groupUnlocked == group.size) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Button(
                        onClick = { onShowPhotocard(type) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlobOrange, contentColor = BlobCream),
                    ) {
                        Text("${type.label} 6신 완성! 포토카드 받기", fontFamily = BlackHanSans, fontSize = 16.sp)
                    }
                }
            }

            items(group, key = { it.id }) { guardian ->
                val cardModifier = Modifier.fillMaxWidth().aspectRatio(0.66f)
                if (guardian.id in state.unlockedIds) {
                    GuardianCardFace(guardian, cardModifier, compact = true)
                } else {
                    val holds = state.holdsFor(guardian.id)
                    LockedGuardianCard(
                        guardian, cardModifier,
                        hint = if (holds > 0) "참기 $holds/${BlobState.HOLDS_TO_UNLOCK}" else "아직 못 만난 수호신",
                    )
                }
            }
        }
    }
}
