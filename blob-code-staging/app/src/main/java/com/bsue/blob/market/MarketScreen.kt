package com.bsue.blob.market

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.formatWon
import com.bsue.blob.dispatch.DispatchOverlay
import com.bsue.blob.dispatch.DispatchRequest
import com.bsue.blob.dispatch.DispatchResult
import com.bsue.blob.ui.theme.GothicA1
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

// ── 지름마켓 전용 색: 블롭 브랜드(오렌지·크림)와 일부러 다르게 '보통 쇼핑앱'처럼 ──
private val MarketBlue = Color(0xFF2E5BFF)
private val MarketRed = Color(0xFFF0323C)
private val MarketBg = Color(0xFFF4F5F7)
private val MarketInk = Color(0xFF1B1D22)
private val MarketSub = Color(0xFF8A8F99)
private val MarketLine = Color(0xFFE6E8EC)
private val MarketOrangeSoft = Color(0xFFFFF1E6)
private val MarketOrange = Color(0xFFE8711C)

private const val DEMO_NOTICE = "체험용 가상 쇼핑몰이에요. 실제로 결제되지 않아요."

private enum class MarketStep { LIST, DETAIL, CHECKOUT, DONE }

// 관람객 체험용 가짜 쇼핑 화면 '지름마켓'
// 상품 고르기 → 상세 → 주문서 → [결제하기] → 블롭 출동 (실제 출동과 같은 화면)
@Composable
fun MarketScreen(
    onExit: () -> Unit,
    onHeld: (DispatchRequest) -> Unit,              // [참았다!] → 영수증 저장
    onSendFakePayment: (amount: Int?, merchant: String?) -> Unit, // 가짜 카드 승인 알림
) {
    var step by rememberSaveable { mutableStateOf(MarketStep.LIST) }
    var productId by rememberSaveable { mutableStateOf<String?>(null) }
    var dispatching by remember { mutableStateOf<DispatchRequest?>(null) }
    val product = MarketProducts.all.firstOrNull { it.id == productId }

    // 뒤로가기: 출동 중엔 무시(버튼으로만 닫힘), 아니면 한 단계 뒤로
    BackHandler {
        if (dispatching != null) return@BackHandler
        step = when (step) {
            MarketStep.LIST -> { onExit(); MarketStep.LIST }
            MarketStep.DETAIL -> MarketStep.LIST
            MarketStep.CHECKOUT -> MarketStep.DETAIL
            MarketStep.DONE -> MarketStep.LIST
        }
    }

    Box(Modifier.fillMaxSize().background(MarketBg)) {
        when {
            step == MarketStep.LIST || product == null -> MarketHome(
                onExit = onExit,
                onOpen = { productId = it.id; step = MarketStep.DETAIL },
                onSendFakePayment = { onSendFakePayment(null, null) },
            )
            step == MarketStep.DETAIL -> ProductDetail(
                product = product,
                onBack = { step = MarketStep.LIST },
                onBuy = { step = MarketStep.CHECKOUT },
            )
            step == MarketStep.CHECKOUT -> Checkout(
                product = product,
                onBack = { step = MarketStep.DETAIL },
                onPay = {
                    Guardians.byId(product.guardianId)?.let { guardian ->
                        val request = DispatchRequest(guardian, product.total, "지름마켓 · ${product.store}")
                        dispatching = request
                    }
                },
            )
            else -> OrderDone(
                product = product,
                onSendFakePayment = { onSendFakePayment(product.total, product.store) },
                onContinue = { step = MarketStep.LIST },
            )
        }

        // 블롭 출동 (실제 쇼핑앱에서 뜨는 것과 같은 화면)
        dispatching?.let { request ->
            DispatchOverlay(request) { result ->
                dispatching = null
                when (result) {
                    // 참았다 → 영수증 저장 + 뒤로가기처럼 주문서에서 나가기
                    DispatchResult.HELD -> {
                        onHeld(request)
                        step = MarketStep.DETAIL
                    }
                    // 그래도 살래 → 가짜 주문 완료
                    DispatchResult.BOUGHT -> step = MarketStep.DONE
                }
            }
        }
    }
}

// ── 홈: 검색창 · 특가 배너 · 카테고리 · 상품 목록 ─────────────────

@Composable
private fun MarketHome(
    onExit: () -> Unit,
    onOpen: (MarketProduct) -> Unit,
    onSendFakePayment: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                // 상단: 나가기 · 로고 · 알림 체험
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "‹", fontSize = 30.sp, color = MarketInk,
                        modifier = Modifier.clickable(onClick = onExit).padding(end = 10.dp),
                    )
                    Text("지름", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MarketBlue)
                    Text("마켓", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MarketInk)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "🔔 결제 알림 체험",
                        fontFamily = GothicA1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MarketBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, MarketBlue, RoundedCornerShape(50))
                            .clickable(onClick = onSendFakePayment)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                // 가짜 검색창
                Text(
                    text = "🔍  오늘 뭐 살까?",
                    fontFamily = GothicA1,
                    fontSize = 14.sp,
                    color = MarketSub,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MarketLine, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                )
                Spacer(Modifier.height(12.dp))
                DealBanner()
                Spacer(Modifier.height(12.dp))
                CategoryChips()
                Spacer(Modifier.height(8.dp))
                Text(DEMO_NOTICE, fontFamily = GothicA1, fontSize = 11.sp, color = MarketSub)
                Spacer(Modifier.height(4.dp))
            }
        }
        items(MarketProducts.all, key = { it.id }) { product ->
            ProductCard(product, onClick = { onOpen(product) })
        }
    }
}

// "⚡ 오늘만 특가 · 03:12:45 남음" (자정까지 실제로 줄어듦)
@Composable
private fun DealBanner() {
    var left by remember { mutableStateOf(timeToMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            left = timeToMidnight()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(MarketBlue, Color(0xFF7B5BFF))))
            .padding(16.dp),
    ) {
        Text("⚡ 오늘만 특가", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text("$left 남음 · 최대 70% 할인", fontFamily = GothicA1, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
    }
}

private fun timeToMidnight(): String {
    val now = LocalDateTime.now()
    val seconds = Duration.between(now.toLocalTime(), LocalTime.MAX).seconds
    return "%02d:%02d:%02d".format(seconds / 3600, seconds % 3600 / 60, seconds % 60)
}

@Composable
private fun CategoryChips() {
    val categories = listOf("전체", "편의점", "패션", "배달", "전자", "장보기", "카페")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEachIndexed { i, name ->
            val selected = i == 0
            Text(
                text = name,
                fontFamily = GothicA1,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) Color.White else MarketInk,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) MarketInk else Color.White)
                    .border(1.dp, if (selected) MarketInk else MarketLine, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun ProductCard(product: MarketProduct, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .clickable(onClick = onClick),
    ) {
        Box {
            ProductTile(product, Modifier.fillMaxWidth().aspectRatio(1f), emojiSize = 56)
            product.badge?.let { RedBadge(it, Modifier.padding(8.dp)) }
        }
        Column(Modifier.padding(10.dp)) {
            Text(product.store, fontFamily = GothicA1, fontSize = 11.sp, color = MarketSub)
            Text(
                product.name, fontFamily = GothicA1, fontSize = 13.sp, color = MarketInk,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp,
            )
            Spacer(Modifier.height(4.dp))
            PriceRow(product, big = false)
            Text(
                "★ ${product.rating} (${"%,d".format(product.reviews)})",
                fontFamily = GothicA1, fontSize = 11.sp, color = MarketSub,
            )
        }
    }
}

// ── 상품 상세 ────────────────────────────────────────

@Composable
private fun ProductDetail(product: MarketProduct, onBack: () -> Unit, onBuy: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        MarketTopBar("상품 상세", onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color.White),
        ) {
            Box {
                ProductTile(product, Modifier.fillMaxWidth().aspectRatio(1.2f), emojiSize = 110)
                product.badge?.let { RedBadge(it, Modifier.padding(14.dp)) }
            }
            Column(Modifier.padding(18.dp)) {
                Text(product.store, fontFamily = GothicA1, fontSize = 13.sp, color = MarketSub)
                Spacer(Modifier.height(4.dp))
                Text(product.name, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MarketInk)
                Spacer(Modifier.height(6.dp))
                Text(
                    "★ ${product.rating}  리뷰 ${"%,d".format(product.reviews)}개",
                    fontFamily = GothicA1, fontSize = 13.sp, color = MarketSub,
                )
                Spacer(Modifier.height(12.dp))
                PriceRow(product, big = true)
                Spacer(Modifier.height(14.dp))
                product.note?.let {
                    NoteBox(it)
                    Spacer(Modifier.height(12.dp))
                }
                if (product.cartItems.isNotEmpty()) {
                    CartItems(product)
                    Spacer(Modifier.height(12.dp))
                }
                HorizontalDivider(color = MarketLine)
                Spacer(Modifier.height(12.dp))
                InfoLine("배송", product.shippingText)
                Spacer(Modifier.height(80.dp))
            }
        }
        // 하단 고정 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MarketBlue),
            ) {
                Text("장바구니", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = MarketBlue)
            }
            Button(
                onClick = onBuy,
                modifier = Modifier.weight(1.4f).height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarketBlue, contentColor = Color.White),
            ) {
                Text("구매하기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun CartItems(product: MarketProduct) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MarketBg)
            .padding(14.dp),
    ) {
        product.cartItems.forEach { (name, price) ->
            InfoLine(name, formatWon(price))
            Spacer(Modifier.height(6.dp))
        }
        product.freeShippingLeft?.let { left ->
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatWon(left)} 더 담으면 무료배송까지!",
                fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MarketBlue,
            )
            Spacer(Modifier.height(6.dp))
            // 무료배송 진행 막대
            val progress = product.price.toFloat() / (product.price + left)
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(MarketLine)) {
                Box(Modifier.fillMaxWidth(progress).height(8.dp).clip(RoundedCornerShape(50)).background(MarketBlue))
            }
        }
    }
}

// ── 주문서 ──────────────────────────────────────────

@Composable
private fun Checkout(product: MarketProduct, onBack: () -> Unit, onPay: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        MarketTopBar("주문/결제", onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Section("배송지") {
                Text("블롭 탐정사무소 (체험관 1층)", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MarketInk)
                Text("010-0000-0000", fontFamily = GothicA1, fontSize = 13.sp, color = MarketSub)
            }
            Section("주문 상품") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProductTile(product, Modifier.size(56.dp), emojiSize = 28)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(product.store, fontFamily = GothicA1, fontSize = 12.sp, color = MarketSub)
                        Text(product.name, fontFamily = GothicA1, fontSize = 14.sp, color = MarketInk, maxLines = 2)
                    }
                }
            }
            Section("결제수단") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(18.dp).clip(CircleShape).border(5.dp, MarketBlue, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text("비상카드 (****-0328) · 일시불", fontFamily = GothicA1, fontSize = 14.sp, color = MarketInk)
                }
            }
            Section("결제 금액") {
                InfoLine("상품 금액", formatWon(product.originalPrice ?: product.price))
                product.originalPrice?.let { InfoLine("할인 금액", "-" + formatWon(it - product.price), MarketRed) }
                InfoLine("배송비", if (product.shippingFee == 0) "무료" else formatWon(product.shippingFee))
                product.freeShippingLeft?.let {
                    Text("무료배송까지 ${formatWon(it)} 남았어요", fontFamily = GothicA1, fontSize = 12.sp, color = MarketBlue)
                }
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MarketLine)
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("총 결제금액", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MarketInk)
                    Spacer(Modifier.weight(1f))
                    Text(formatWon(product.total), fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MarketRed)
                }
            }
            Text(DEMO_NOTICE, fontFamily = GothicA1, fontSize = 11.sp, color = MarketSub, modifier = Modifier.padding(horizontal = 4.dp))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(12.dp),
        ) {
            Button(
                onClick = onPay,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarketBlue, contentColor = Color.White),
            ) {
                Text(
                    "${formatWon(product.total).removePrefix("₩")}원 결제하기",
                    fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                )
            }
        }
    }
}

// ── 주문 완료 (그래도 살래를 골랐을 때) ─────────────────────

@Composable
private fun OrderDone(product: MarketProduct, onSendFakePayment: () -> Unit, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MarketBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", fontSize = 36.sp, color = Color.White)
        }
        Spacer(Modifier.height(18.dp))
        Text("주문 완료!", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MarketInk)
        Spacer(Modifier.height(6.dp))
        Text(
            "${product.store} · ${formatWon(product.total)}",
            fontFamily = GothicA1, fontSize = 15.sp, color = MarketSub,
        )
        Text("(체험이라 실제로 결제되지는 않았어요)", fontFamily = GothicA1, fontSize = 12.sp, color = MarketSub)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onSendFakePayment,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarketBlue, contentColor = Color.White),
        ) {
            Text("🔔 카드 승인 알림 받아보기", fontFamily = GothicA1, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MarketLine),
        ) {
            Text("쇼핑 계속하기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = MarketInk)
        }
    }
}

// ── 작은 조각들 ─────────────────────────────────────

@Composable
private fun MarketTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .height(52.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("‹", fontSize = 30.sp, color = MarketInk, modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
        Text(title, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MarketInk)
    }
}

@Composable
private fun ProductTile(product: MarketProduct, modifier: Modifier, emojiSize: Int) {
    Box(modifier.background(product.tileColor), contentAlignment = Alignment.Center) {
        Text(product.emoji, fontSize = emojiSize.sp)
    }
}

@Composable
private fun RedBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontFamily = GothicA1,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MarketRed)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
private fun PriceRow(product: MarketProduct, big: Boolean) {
    Column {
        product.originalPrice?.let {
            Text(
                formatWon(it), fontFamily = GothicA1, fontSize = if (big) 14.sp else 11.sp, color = MarketSub,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            product.discountPercent?.let {
                Text(
                    "$it% ", fontFamily = GothicA1, fontWeight = FontWeight.Bold,
                    fontSize = if (big) 22.sp else 15.sp, color = MarketRed,
                )
            }
            Text(
                formatWon(product.price), fontFamily = GothicA1, fontWeight = FontWeight.Bold,
                fontSize = if (big) 22.sp else 15.sp, color = MarketInk,
            )
        }
    }
}

@Composable
private fun NoteBox(text: String) {
    Text(
        text = text,
        fontFamily = GothicA1,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MarketOrange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MarketOrangeSoft)
            .padding(12.dp),
    )
}

@Composable
private fun InfoLine(label: String, value: String, valueColor: Color = MarketInk) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontFamily = GothicA1, fontSize = 14.sp, color = MarketSub)
        Spacer(Modifier.weight(1f))
        Text(value, fontFamily = GothicA1, fontSize = 14.sp, color = valueColor)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MarketInk)
        Spacer(Modifier.height(2.dp))
        content()
    }
}
