package com.bsue.blob.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.Receipt
import com.bsue.blob.data.coffeeCupsLabel
import com.bsue.blob.data.formatWon
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.components.BlobCharacter
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.receipt.DashedLine
import com.bsue.blob.ui.receipt.FakeBarcode
import com.bsue.blob.ui.receipt.InkColor
import com.bsue.blob.ui.receipt.InkFaded
import com.bsue.blob.ui.receipt.PRINT_ROW_MS
import com.bsue.blob.ui.receipt.PaperColor
import com.bsue.blob.ui.receipt.PrintReveal
import com.bsue.blob.ui.receipt.ReceiptImage
import com.bsue.blob.ui.receipt.TornBottomShape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.ReceiptMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val FIRST_PRINT_DELAY_MS = 500L
private val PrinterBody = Color(0xFF2B2B2B)
private val ReadyGreen = Color(0xFF3FA34D)

// 영수증: 안 지른 것들의 계산서 (감열지 영수증 느낌)
@Composable
fun ReceiptScreen(
    state: BlobState,
    onMarkSeen: (Long) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val thisMonth = remember { YearMonth.now() }

    // 월별 보기: 영수증이 있는 달 + 이번 달
    val months = remember(state.receipts) {
        (state.receipts.map { YearMonth.from(it.dateTime()) } + thisMonth).distinct().sortedDescending()
    }
    var monthText by rememberSaveable { mutableStateOf(thisMonth.toString()) }
    val month = YearMonth.parse(monthText)
    val monthIndex = months.indexOf(month).coerceAtLeast(0)
    val items = state.receipts
        .filter { YearMonth.from(it.dateTime()) == month }
        .sortedByDescending { it.createdAt }

    // 새로 들어온 항목 → 드르륵 출력 (이번 달만). 오래된 새 항목부터 차례로
    var seenAt by remember { mutableLongStateOf(state.receiptsSeenAt) }
    val newOnes = if (month == thisMonth) items.filter { it.createdAt > seenAt } else emptyList()
    val printing = newOnes.isNotEmpty()
    LaunchedEffect(newOnes.maxOfOrNull { it.createdAt }) {
        val latest = newOnes.maxOfOrNull { it.createdAt } ?: return@LaunchedEffect
        delay(FIRST_PRINT_DELAY_MS + PRINT_ROW_MS * newOnes.size)
        seenAt = latest
        onMarkSeen(latest)
    }

    // 이미지 저장·공유용: 영수증 부분만 그림으로 떠두기
    val captureLayer = rememberGraphicsLayer()
    val fileName = "blob-receipt-$month"
    fun withBitmap(action: (Bitmap) -> Unit) {
        scope.launch {
            val bitmap = captureLayer.toImageBitmap().asAndroidBitmap()
            action(bitmap)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobBlack)
            .verticalScroll(rememberScrollState()),
    ) {
        // 제목 + 저장/공유
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("안 지른 영수증", fontFamily = BlackHanSans, fontSize = 28.sp, color = BlobCream, modifier = Modifier.weight(1f))
            SmallAction("저장", enabled = !printing) {
                withBitmap { bmp ->
                    val ok = ReceiptImage.saveToGallery(context, bmp, fileName)
                    Toast.makeText(context, if (ok) "갤러리 '블롭 출동' 앨범에 저장했어!" else "저장을 못 했어. 다시 해볼래?", Toast.LENGTH_SHORT).show()
                }
            }
            Spacer(Modifier.width(8.dp))
            SmallAction("공유", enabled = !printing) {
                withBitmap { bmp -> ReceiptImage.share(context, bmp, fileName) }
            }
        }

        // 월 고르기
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val hasOlder = monthIndex < months.lastIndex
            val hasNewer = monthIndex > 0
            MonthArrow("‹", hasOlder) { monthText = months[monthIndex + 1].toString() }
            Text(
                "${month.year}년 ${month.monthValue}월",
                fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobCream,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            MonthArrow("›", hasNewer) { monthText = months[monthIndex - 1].toString() }
        }

        // 프린터
        ReceiptPrinter(printing = printing, modifier = Modifier.padding(horizontal = 12.dp))

        // 종이 (이 Box가 그대로 저장·공유 이미지가 돼요)
        Box(
            modifier = Modifier
                .drawWithContent {
                    captureLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(captureLayer)
                }
                .background(BlobBlack)
                .padding(start = 26.dp, end = 26.dp, bottom = 28.dp),
        ) {
            ReceiptPaper(
                month = month,
                isThisMonth = month == thisMonth,
                items = items,
                isNew = { it.createdAt > seenAt },
                printDelay = { receipt ->
                    // 새 항목 중 오래된 것부터 차례로 출력
                    val order = newOnes.sortedBy { it.createdAt }.indexOf(receipt)
                    FIRST_PRINT_DELAY_MS + PRINT_ROW_MS * order.coerceAtLeast(0)
                },
                modifier = Modifier.offset(y = (-6).dp), // 프린터 입구에서 막 나온 느낌
            )
        }
    }
}

// 프린터 몸통: 검정 상자 + 출력구 + 경고테이프 + 작동 램프
@Composable
private fun ReceiptPrinter(printing: Boolean, modifier: Modifier = Modifier) {
    val blink = rememberInfiniteTransition(label = "printer")
    val lamp by blink.animateFloat(0.3f, 1f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "lamp")
    val shake by blink.animateFloat(-1f, 1f, infiniteRepeatable(tween(60), RepeatMode.Reverse), label = "shake")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = if (printing) shake * 1.5f else 0f }
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
            .background(PrinterBody),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 프린터 담당 블롭: 대기 중엔 기본, 출력 중엔 환호 (ui/blob/BlobPose.kt)
            BlobCharacter(
                scene = if (printing) BlobScene.RECEIPT_PRINTING else BlobScene.RECEIPT,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("BLOB PRINTER", fontFamily = ReceiptMono, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BlobCream.copy(alpha = 0.6f))
            Spacer(Modifier.weight(1f))
            Text(if (printing) "출력 중" else "대기", fontFamily = GothicA1, fontSize = 11.sp, color = BlobCream.copy(alpha = 0.6f))
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (printing) BlobOrange.copy(alpha = lamp) else ReadyGreen),
            )
        }
        HazardTape(Modifier.fillMaxWidth().height(8.dp))
        // 출력구 (종이가 나오는 틈)
        Box(
            Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(BlobBlack),
        )
    }
}

// 영수증 종이 본문
@Composable
private fun ReceiptPaper(
    month: YearMonth,
    isThisMonth: Boolean,
    items: List<Receipt>,
    isNew: (Receipt) -> Boolean,
    printDelay: (Receipt) -> Long,
    modifier: Modifier = Modifier,
) {
    val total = items.sumOf { it.amount ?: 0 }
    val now = remember { LocalDateTime.now() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, TornBottomShape())
            .background(PaperColor, TornBottomShape())
            .padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 30.dp),
    ) {
        // 머리
        Text("비상수단", fontFamily = BlackHanSans, fontSize = 34.sp, color = InkColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Mono("안 지른 것들의 계산서", size = 15, bold = true, align = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Mono("블롭 탐정사무소 · 지출주의 구역", size = 11, color = InkFaded, align = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        DashedLine()
        Spacer(Modifier.height(8.dp))
        KeyValue("기간", "%d.%02d".format(month.year, month.monthValue))
        KeyValue("발행", now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")))
        KeyValue("담당", "탐정 블롭")
        Spacer(Modifier.height(8.dp))
        DashedLine()
        Spacer(Modifier.height(6.dp))
        Row {
            Mono("날짜/내용", size = 12, bold = true, modifier = Modifier.weight(1f))
            Mono("금액", size = 12, bold = true)
        }
        Spacer(Modifier.height(6.dp))
        DashedLine(color = InkFaded)

        // 항목 (최근 것이 위)
        if (items.isEmpty()) {
            Spacer(Modifier.height(18.dp))
            Mono("(아직 비어 있어)", size = 13, align = TextAlign.Center, color = InkFaded)
            Mono("참은 순간이 여기 한 줄씩 찍힐 거야", size = 13, align = TextAlign.Center, color = InkFaded)
            Spacer(Modifier.height(18.dp))
        }
        items.forEach { receipt ->
            key(receipt.createdAt) {
                PrintReveal(animate = isNew(receipt), delayMillis = printDelay(receipt)) {
                    ReceiptLine(receipt)
                }
            }
        }

        // 합계
        Spacer(Modifier.height(4.dp))
        DashedLine(double = true)
        Spacer(Modifier.height(8.dp))
        KeyValue("합계", "${items.size}건")
        if (items.any { it.amount == null }) KeyValue("금액 모름", "${items.count { it.amount == null }}건")
        Spacer(Modifier.height(8.dp))
        Mono(
            if (isThisMonth) "이번 달 지킨 돈" else "${month.year}년 ${month.monthValue}월에 지킨 돈",
            size = 14, bold = true,
        )
        Text(
            formatWon(total), fontFamily = BlackHanSans, fontSize = 34.sp, color = InkColor,
            textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(),
        )
        Mono("= 커피 ${coffeeCupsLabel(total)}잔", size = 16, bold = true, align = TextAlign.End)
        Spacer(Modifier.height(10.dp))
        DashedLine()
        Spacer(Modifier.height(14.dp))
        FakeBarcode(seed = month.hashCode() + items.size)
        Spacer(Modifier.height(4.dp))
        Mono("BLOB-%d%02d-%04d".format(month.year, month.monthValue, items.size), size = 11, align = TextAlign.Center, color = InkFaded)
        Spacer(Modifier.height(12.dp))
        Mono("참아줘서 고마워!", size = 14, bold = true, align = TextAlign.Center)
        Mono("다음에도 딱 3초만 같이 멈칫하자", size = 12, align = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Mono("* 이 영수증은 결제 영수증이 아닙니다 *", size = 10, align = TextAlign.Center, color = InkFaded)
    }
}

// 항목 한 줄: 날짜 · 내용 · 금액 · 막은 수호신
@Composable
private fun ReceiptLine(receipt: Receipt) {
    val guardian = Guardians.byId(receipt.guardianId)
    Column(Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Mono(receipt.dateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")), size = 12, color = InkFaded, modifier = Modifier.weight(1f))
            Mono(receipt.amount?.let { formatWon(it) } ?: "금액 모름", size = 15, bold = true)
        }
        Mono(receipt.appLabel.ifBlank { "어딘가의 결제" }, size = 14)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Mono("└ 막은 수호신 ", size = 12, color = InkFaded)
            Box(Modifier.size(9.dp).background(guardian?.color ?: InkFaded))
            Mono(" ${guardian?.name ?: "-"}", size = 12, bold = true)
        }
        Spacer(Modifier.height(8.dp))
        DashedLine(color = InkFaded.copy(alpha = 0.5f))
    }
}

@Composable
private fun Mono(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    color: Color = InkColor,
    align: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        fontFamily = ReceiptMono,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = (size + 6).sp,
        color = color,
        textAlign = align,
        modifier = if (align == TextAlign.Start) modifier else modifier.fillMaxWidth(),
    )
}

@Composable
private fun KeyValue(key: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Mono(key, size = 13, modifier = Modifier.weight(1f))
        Mono(value, size = 13, bold = true)
    }
}

@Composable
private fun SmallAction(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.5.dp, if (enabled) BlobOrange else BlobCream.copy(alpha = 0.3f)),
    ) {
        Text(label, fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = if (enabled) BlobOrange else BlobCream.copy(alpha = 0.3f))
    }
}

@Composable
private fun MonthArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        symbol,
        fontSize = 30.sp,
        color = if (enabled) BlobCream else BlobCream.copy(alpha = 0.2f),
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
    )
}

private fun Receipt.dateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault())
