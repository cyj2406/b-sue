package com.bsue.blob.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.notify.PaymentNotice
import com.bsue.blob.overlay.farewellLine
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.TapeYellow

// 앱 화면 안에서 보이는 '배웅' — 블롭(괜찮아 포즈)이 천천히 기울고, 동전(₩)이 날아가요
@Composable
fun FarewellBanner(notice: PaymentNotice, modifier: Modifier = Modifier) {
    val coin = remember { Animatable(0f) }
    LaunchedEffect(notice) {
        coin.snapTo(0f)
        coin.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BlobBlack)
            .border(3.dp, TapeYellow, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            BlobCharacter(BlobScene.FAREWELL, Modifier.size(64.dp))
            // 떠나는 동전
            Text(
                text = "₩",
                fontFamily = BlackHanSans,
                fontSize = 18.sp,
                color = BlobBlack,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = 40.dp.toPx() + coin.value * 220.dp.toPx()
                        translationY = 4.dp.toPx() - coin.value * 30.dp.toPx()
                        rotationZ = coin.value * 360f
                        alpha = 1f - coin.value
                    }
                    .clip(RoundedCornerShape(50))
                    .background(TapeYellow)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("배웅", fontFamily = BlackHanSans, fontSize = 13.sp, color = TapeYellow)
            Spacer(Modifier.height(2.dp))
            Text(
                text = farewellLine(notice.amount, notice.merchant),
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = BlobCream,
            )
            Text(
                text = notice.sourceLabel,
                fontFamily = GothicA1,
                fontSize = 11.sp,
                color = BlobCream.copy(alpha = 0.55f),
            )
        }
    }
}
