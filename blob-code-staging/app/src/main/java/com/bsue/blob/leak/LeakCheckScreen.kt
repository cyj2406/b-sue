package com.bsue.blob.leak

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.Subscription
import com.bsue.blob.data.SubscriptionPresets
import com.bsue.blob.data.coffeeCupsLabel
import com.bsue.blob.data.formatWon
import com.bsue.blob.ui.components.HazardTape
import com.bsue.blob.ui.theme.BlackHanSans
import com.bsue.blob.ui.theme.BlobBlack
import com.bsue.blob.ui.theme.BlobCream
import com.bsue.blob.ui.theme.BlobGray
import com.bsue.blob.ui.theme.BlobOrange
import com.bsue.blob.ui.theme.GothicA1
import com.bsue.blob.ui.theme.contentColorOn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val ghostColor get() = Guardians.byId("ghost_sub")!!.color
private val autoPayColor get() = Guardians.byId("auto_pay")!!.color

// 누수 점검: 구독 목록 · 유령 구독 · 자동결제 잠복수사
@Composable
fun LeakCheckScreen(
    state: BlobState,
    onBack: () -> Unit,
    onSave: (Subscription) -> Unit,
    onDelete: (String) -> Unit,
    onDismissSuspect: (String) -> Unit,
    onLoadDemo: () -> Unit,
    onClearPayments: () -> Unit,
) {
    val context = LocalContext.current
    // 편집 중인 구독 (새로 등록이면 id가 빈 값)
    var editing by remember { mutableStateOf<Subscription?>(null) }
    val suspects = remember(state.payments, state.subscriptions, state.dismissedAutoPay) {
        AutoPayDetector.detect(state.payments, state.subscriptions, state.dismissedAutoPay)
    }
    val subs = state.subscriptions.sortedBy { it.nextBillingDate() }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹", fontSize = 32.sp, color = BlobBlack,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp),
            )
            Text("누수 점검", fontFamily = BlackHanSans, fontSize = 30.sp, color = BlobBlack)
        }
        Spacer(Modifier.height(10.dp))
        HazardTape(Modifier.fillMaxWidth().height(10.dp))
        Spacer(Modifier.height(16.dp))

        SubscriptionTotalCard(state.monthlySubscriptionTotal, state.subscriptions.size)

        // ── 구독 목록 ──
        LeakSectionTitle("내 구독")
        if (subs.isEmpty()) {
            Hint("아직 등록한 구독이 없어. 아래에서 등록하거나 '데모 데이터'를 눌러봐!")
        }
        subs.forEach { sub ->
            SubscriptionRow(sub, onClick = { editing = sub })
            Spacer(Modifier.height(10.dp))
        }
        Button(
            onClick = { editing = newSubscription() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
        ) {
            Text("+ 구독 등록", fontFamily = BlackHanSans, fontSize = 17.sp)
        }

        // ── 자동결제 잠복수사 ──
        LeakSectionTitle("자동결제 잠복수사")
        if (suspects.isEmpty()) {
            Hint("결제 알림 기록에서 매달 반복되는 결제를 찾는 중이야. 아직 수상한 건 없어.")
        }
        suspects.forEach { suspect ->
            SuspectCard(
                suspect = suspect,
                onRegister = {
                    editing = newSubscription().copy(
                        name = suspect.merchant,
                        shortName = suspect.merchant,
                        monthlyAmount = suspect.amount,
                        billingDay = suspect.dayOfMonth,
                    )
                },
                onDismiss = { onDismissSuspect(suspect.key) },
            )
            Spacer(Modifier.height(10.dp))
        }
        Hint("결제 알림 기록 ${state.payments.size}건 · 금액·가게·날짜만 이 폰에 저장돼")
        if (state.payments.isNotEmpty()) {
            TextButton(onClick = onClearPayments) {
                Text("결제 알림 기록 지우기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobGray)
            }
        }

        // ── 전시용 ──
        LeakSectionTitle("전시용")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onLoadDemo, modifier = Modifier.weight(1f)) {
                Text("데모 데이터", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
            OutlinedButton(
                onClick = {
                    val target = subs.firstOrNull()
                    val message = when {
                        target == null -> "먼저 구독을 등록하거나 데모 데이터를 넣어줘!"
                        !GhostSubAlarm.notify(context, target, preview = true) -> "알림 권한이 꺼져 있어. 폰 설정에서 켜줘!"
                        else -> null
                    }
                    message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("알림 미리보기", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = BlobBlack)
            }
        }
        Hint("데모 데이터: 넷플릭스(내일 결제·47일째 안 봄) · 유튜브 프리미엄 · 쿠팡 와우 + 석 달째 빠져나가는 멜론 결제 기록")
        Spacer(Modifier.height(24.dp))
    }

    editing?.let { sub ->
        SubscriptionEditor(
            initial = sub,
            isNew = sub.id.isEmpty(),
            onDismiss = { editing = null },
            onSave = { saved ->
                onSave(saved)
                editing = null
            },
            onDelete = {
                onDelete(sub.id)
                editing = null
            },
        )
    }
}

private fun newSubscription() = Subscription(
    id = "", name = "", shortName = "", monthlyAmount = 0,
    billingDay = LocalDate.now().dayOfMonth, lastUsed = null,
)

// ── 월 총 구독료 카드 (홈에서도 같이 씀) ─────────────────────

@Composable
fun SubscriptionTotalCard(total: Int, count: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BlobBlack)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("한 달 구독료", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlobCream)
            Spacer(Modifier.weight(1f))
            if (onClick != null) {
                Text("누수 점검 ›", fontFamily = BlackHanSans, fontSize = 14.sp, color = BlobOrange)
            }
        }
        Spacer(Modifier.height(6.dp))
        if (count == 0) {
            Text("아직 등록한 구독이 없어", fontFamily = BlackHanSans, fontSize = 22.sp, color = BlobCream)
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatWon(total), fontFamily = BlackHanSans, fontSize = 28.sp, color = BlobOrange)
                Spacer(Modifier.width(8.dp))
                Text("= 커피 ${coffeeCupsLabel(total)}잔", fontFamily = BlackHanSans, fontSize = 20.sp, color = BlobCream)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "구독 ${count}개 · 1년이면 ${formatWon(total * 12)} (커피 ${coffeeCupsLabel(total * 12)}잔)",
                fontFamily = GothicA1, fontSize = 12.sp, color = BlobCream.copy(alpha = 0.7f),
            )
        }
    }
}

// ── 구독 한 줄 ─────────────────────────────────────

@Composable
private fun SubscriptionRow(sub: Subscription, onClick: () -> Unit) {
    val today = LocalDate.now()
    val dDay = ChronoUnit.DAYS.between(today, sub.nextBillingDate(today))
    val ghost = sub.looksGhost(today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (ghost) ghostColor.copy(alpha = 0.18f) else BlobCream)
            .border(2.dp, if (ghost) ghostColor else BlobBlack, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(sub.name, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlobBlack)
                if (ghost) {
                    Spacer(Modifier.width(6.dp))
                    Chip("유령 의심", ghostColor)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "매달 ${sub.billingDay}일 · " + if (dDay == 0L) "오늘 결제" else "D-$dDay",
                fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f),
            )
            Text(
                sub.daysSinceLastUsed(today)?.let { if (it == 0L) "오늘 ${sub.usedVerb} 걸로 기억해" else "마지막으로 ${sub.usedVerb} 지 ${it}일째" }
                    ?: "마지막 사용일 모름",
                fontFamily = GothicA1, fontSize = 12.sp, color = BlobBlack.copy(alpha = 0.7f),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatWon(sub.monthlyAmount), fontFamily = BlackHanSans, fontSize = 18.sp, color = BlobOrange)
            Text("커피 ${coffeeCupsLabel(sub.monthlyAmount)}잔", fontFamily = GothicA1, fontSize = 11.sp, color = BlobBlack.copy(alpha = 0.6f))
        }
    }
}

// ── 잠복수사 카드 ───────────────────────────────────

@Composable
private fun SuspectCard(suspect: AutoPaySuspect, onRegister: () -> Unit, onDismiss: () -> Unit) {
    val ink = contentColorOn(autoPayColor)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(autoPayColor)
            .border(2.dp, BlobBlack, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Text("자동결제 잠복수사", fontFamily = BlackHanSans, fontSize = 13.sp, color = ink.copy(alpha = 0.8f))
        Spacer(Modifier.height(4.dp))
        Text(suspect.merchant, fontFamily = BlackHanSans, fontSize = 24.sp, color = ink)
        Text(
            "매달 ${suspect.dayOfMonth}일쯤 · ${formatWon(suspect.amount)} · ${suspect.months}달째",
            fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ink,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "“이거 구독 같은데, 알고 있었어?”",
            fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BlobBlack,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BlobCream)
                .padding(10.dp),
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onRegister,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BlobBlack, contentColor = BlobCream),
            ) {
                Text("구독으로 등록", fontFamily = GothicA1, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("알고 있었어", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = ink)
            }
        }
    }
}

// ── 구독 등록 / 고치기 창 ─────────────────────────────

private enum class LastUsedChoice(val label: String, val daysAgo: Long?) {
    KEEP("그대로", null),
    UNKNOWN("모름", null),
    TODAY("오늘", 0),
    WEEK("1주 전", 7),
    MONTH("한 달 전", 30),
    THREE_MONTHS("석 달 넘게", 90),
}

@Composable
private fun SubscriptionEditor(
    initial: Subscription,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Subscription) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var shortName by remember { mutableStateOf(initial.shortName) }
    var amount by remember { mutableStateOf(if (initial.monthlyAmount > 0) initial.monthlyAmount.toString() else "") }
    var day by remember { mutableStateOf(initial.billingDay.toString()) }
    var usedVerb by remember { mutableStateOf(initial.usedVerb) }
    var manageUrl by remember { mutableStateOf(initial.manageUrl) }
    var lastUsedChoice by remember {
        mutableStateOf(if (!isNew && initial.lastUsed != null) LastUsedChoice.KEEP else LastUsedChoice.UNKNOWN)
    }

    val amountValue = amount.toIntOrNull()
    val dayValue = day.toIntOrNull()
    val valid = name.isNotBlank() && amountValue != null && amountValue > 0 && dayValue != null && dayValue in 1..31

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlobCream,
        title = { Text(if (isNew) "구독 등록" else "구독 고치기", fontFamily = BlackHanSans) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (isNew) {
                    Text("자주 쓰는 구독", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlobBlack)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SubscriptionPresets.all.forEach { preset ->
                            Text(
                                text = preset.name,
                                fontFamily = GothicA1,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (name == preset.name) BlobCream else BlobBlack,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (name == preset.name) BlobBlack else BlobCream)
                                    .border(1.dp, BlobBlack, RoundedCornerShape(50))
                                    .clickable {
                                        name = preset.name
                                        shortName = preset.shortName
                                        amount = preset.amount.toString()
                                        usedVerb = preset.usedVerb
                                        manageUrl = preset.manageUrl
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                    Text("※ 금액은 기본값이야. 내 요금제에 맞게 고쳐줘!", fontFamily = GothicA1, fontSize = 11.sp, color = BlobGray)
                    Spacer(Modifier.height(10.dp))
                }
                Field("서비스 이름", name) { name = it; if (shortName.isBlank() || shortName == initial.shortName) shortName = it }
                Field("월 금액 (원)", amount, KeyboardType.Number) { amount = it.filter(Char::isDigit) }
                Field("결제일 (매달 며칠)", day, KeyboardType.Number) { day = it.filter(Char::isDigit).take(2) }
                Spacer(Modifier.height(6.dp))
                Text("마지막으로 쓴 날 (선택)", fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BlobBlack)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LastUsedChoice.entries
                        .filter { it != LastUsedChoice.KEEP || (!isNew && initial.lastUsed != null) }
                        .forEach { choice ->
                            val selected = choice == lastUsedChoice
                            Text(
                                text = choice.label,
                                fontFamily = GothicA1,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selected) BlobCream else BlobBlack,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) BlobOrange else BlobCream)
                                    .border(1.dp, BlobBlack, RoundedCornerShape(50))
                                    .clickable { lastUsedChoice = choice }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val lastUsed = when (lastUsedChoice) {
                        LastUsedChoice.KEEP -> initial.lastUsed
                        LastUsedChoice.UNKNOWN -> null
                        else -> LocalDate.now().minusDays(lastUsedChoice.daysAgo ?: 0)
                    }
                    onSave(
                        initial.copy(
                            id = initial.id.ifEmpty { "sub_${System.currentTimeMillis()}" },
                            name = name.trim(),
                            shortName = shortName.trim().ifEmpty { name.trim() },
                            monthlyAmount = amountValue ?: 0,
                            billingDay = dayValue ?: 1,
                            lastUsed = lastUsed,
                            usedVerb = usedVerb,
                            manageUrl = manageUrl,
                        )
                    )
                },
            ) {
                Text("저장", fontFamily = GothicA1, fontWeight = FontWeight.Bold, color = if (valid) BlobOrange else BlobGray)
            }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(onClick = onDelete) {
                        Text("삭제", fontFamily = GothicA1, color = BlobGray)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("취소", fontFamily = GothicA1, color = BlobBlack)
                }
            }
        },
    )
}

// ── 작은 조각들 ─────────────────────────────────────

@Composable
private fun Field(label: String, value: String, keyboard: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = GothicA1) },
        singleLine = true,
        textStyle = TextStyle(fontFamily = GothicA1, fontSize = 15.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    )
}

@Composable
private fun LeakSectionTitle(text: String) {
    Spacer(Modifier.height(24.dp))
    Text(text, fontFamily = BlackHanSans, fontSize = 18.sp, color = BlobOrange)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Hint(text: String) {
    Text(
        text, fontFamily = GothicA1, fontSize = 12.sp, lineHeight = 17.sp,
        color = BlobBlack.copy(alpha = 0.65f), modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text, fontFamily = GothicA1, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = contentColorOn(color))
    }
}
