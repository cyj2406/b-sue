package com.bsue.blob.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.BlobState
import com.bsue.blob.data.Guardian
import com.bsue.blob.data.GuardianType
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.Receipt
import com.bsue.blob.data.formatWon
import com.bsue.blob.dispatch.DispatchOverlay
import com.bsue.blob.dispatch.DispatchRequest
import com.bsue.blob.dispatch.DispatchResult
import com.bsue.blob.leak.LeakCheckScreen
import com.bsue.blob.market.MarketScreen
import com.bsue.blob.notify.FakePaymentNotifier
import com.bsue.blob.notify.isPaymentListenerOn
import com.bsue.blob.overlay.OverlayBus
import com.bsue.blob.qr.BlobQr
import com.bsue.blob.qr.QrScanScreen
import com.bsue.blob.ui.blob.BlobPoseLab
import com.bsue.blob.ui.components.BSUE_TRANSITION_MS
import com.bsue.blob.ui.components.BsueTab
import com.bsue.blob.ui.components.BsueTabBar
import com.bsue.blob.ui.components.SettingsIcon
import com.bsue.blob.ui.components.ToastHost
import com.bsue.blob.ui.components.rememberToastState
import com.bsue.blob.ui.exhibition.AdminMenu
import com.bsue.blob.ui.exhibition.ExhibitionGuide
import com.bsue.blob.ui.screens.DexScreen
import com.bsue.blob.ui.screens.HomeScreen
import com.bsue.blob.ui.screens.OnboardingScreen
import com.bsue.blob.ui.screens.PhotoCardScreen
import com.bsue.blob.ui.screens.ReceiptScreen
import com.bsue.blob.ui.screens.SettingsScreen
import com.bsue.blob.ui.screens.SummonCelebration
import com.bsue.blob.ui.screens.UnlockCelebration
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 전시 모드: 이 시간 동안 아무도 안 만지면 시작 화면으로
private const val IDLE_RESET_MS = 60_000L
// 탭바 높이 (토스트를 탭바 위 16에 띄울 때)
private val TAB_BAR_HEIGHT = 57.dp

// ════════════════════════════════════════════════════════════════
//  앱 구성 v2 — 네비게이션
//  첫 실행: 온보딩 → 홈
//  전시 모드: 전시 시작 화면 → 지름마켓 (실제 사용과 분리)
//  평소: 하단 탭 3개 (홈 · 수사대 · 기록), 설정은 홈 오른쪽 위 아이콘
//  플로팅 블롭은 앱 밖에서만 (MainActivity 가 OverlayBus 로 숨김)
// ════════════════════════════════════════════════════════════════
@Composable
fun BlobApp(
    resumeTick: Int,
    deepLink: String? = null,          // 기본 카메라로 QR을 찍어 bsue:// 로 열렸을 때
    onDeepLinkHandled: () -> Unit = {},
    onReady: () -> Unit = {},          // 저장된 값을 다 읽음 → 스플래시 화면 닫기
) {
    val context = LocalContext.current
    val repo = remember { BlobRepository(context) }
    // 저장된 값을 읽기 전엔 null → 화면이 깜빡이지 않게
    val state: BlobState? by repo.state.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val toast = rememberToastState()

    var tabName by rememberSaveable { mutableStateOf(BsueTab.HOME.name) }
    val tab = BsueTab.valueOf(tabName)
    var showSettings by rememberSaveable { mutableStateOf(false) } // 홈 오른쪽 위 아이콘 → 설정
    var testRequest by remember { mutableStateOf<DispatchRequest?>(null) } // 출동 테스트 중이면 값이 있음
    var testIndex by rememberSaveable { mutableIntStateOf(0) }
    var showMarket by rememberSaveable { mutableStateOf(false) } // 지름마켓 (전시 모드 안)
    var showAdmin by remember { mutableStateOf(false) }           // 숨은 관리자 메뉴 (로고 5번 탭)
    var showPoseLab by remember { mutableStateOf(false) }         // 캐릭터 테스트
    var showExhibitionStart by remember { mutableStateOf(false) } // 전시 시작 화면
    var showScanner by rememberSaveable { mutableStateOf(false) } // QR 스캔
    var showSummon by remember { mutableStateOf(false) }          // 카드홀더 QR → 블롭 소환!
    var rescanned by remember { mutableStateOf<Guardian?>(null) } // 이미 해금된 카드 QR을 또 찍었을 때
    var photocardType by remember { mutableStateOf<GuardianType?>(null) } // 수사대에서 직접 연 포토카드
    var showLeakCheck by rememberSaveable { mutableStateOf(false) } // 구독 관리
    var fakeIndex by rememberSaveable { mutableIntStateOf(0) }

    val current = state ?: return // 저장된 값 읽는 중 (아주 잠깐)
    LaunchedEffect(Unit) { onReady() }

    // ── 전시 모드: 켜져 있으면 앱 시작 때 전시 시작 화면부터 ──
    LaunchedEffect(current.exhibitionMode) {
        if (current.exhibitionMode) showExhibitionStart = true
    }
    // 전시 모드 동안 화면 안 꺼지게
    val view = LocalView.current
    DisposableEffect(current.exhibitionMode) {
        view.keepScreenOn = current.exhibitionMode
        onDispose { view.keepScreenOn = false }
    }
    // 60초 동안 아무도 안 만지면 전시 시작 화면으로
    val lastTouch = remember { longArrayOf(SystemClock.uptimeMillis()) }
    LaunchedEffect(current.exhibitionMode) {
        if (!current.exhibitionMode) return@LaunchedEffect
        while (true) {
            delay(5_000)
            val idle = SystemClock.uptimeMillis() - lastTouch[0] >= IDLE_RESET_MS
            if (idle && !showExhibitionStart && !showAdmin) {
                showMarket = false
                showLeakCheck = false
                showScanner = false
                showSettings = false
                showSummon = false
                testRequest = null
                rescanned = null
                photocardType = null
                tabName = BsueTab.HOME.name
                showExhibitionStart = true
                lastTouch[0] = SystemClock.uptimeMillis()
            }
        }
    }

    // QR 결과 처리 (앱 스캐너 · 기본 카메라 딥링크 공통)
    val handleQr: (BlobQr) -> Unit = { qr ->
        when (qr) {
            is BlobQr.Card ->
                if (qr.guardian.id in current.unlockedIds) rescanned = qr.guardian
                else scope.launch { repo.unlock(qr.guardian.id) } // → 해금 연출 대기열
            BlobQr.Holder -> showSummon = true
        }
    }
    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            BlobQr.parse(deepLink)?.let(handleQr)
            onDeepLinkHandled()
        }
    }

    // 결제 알림 → 앱을 보고 있을 땐 앱 안에서는 조용히 토스트로 배웅 (블롭은 앱 밖에서만)
    LaunchedEffect(Unit) {
        OverlayBus.farewell.collect { notice ->
            val money = notice.amount?.let { formatWon(it).removePrefix("₩") + "원" } ?: "결제"
            toast.show("잘 가, $money… " + (notice.merchant?.let { "${it}에서 잘 쓰고 와" } ?: "잘 쓰기!"))
        }
    }

    // 가짜 카드 승인 알림 보내기 (금액·가게를 안 주면 예시에서 돌아가며)
    val sendFakePayment: (Int?, String?) -> Unit = { amount, merchant ->
        val sample = if (amount != null) {
            FakePaymentNotifier.Sample(amount, merchant ?: "지름마켓")
        } else {
            FakePaymentNotifier.samples[fakeIndex % FakePaymentNotifier.samples.size].also { fakeIndex++ }
        }
        when {
            !FakePaymentNotifier.send(context, sample) -> toast.show("알림 권한이 꺼져 있어. 설정에서 켜줘")
            !isPaymentListenerOn(context) -> toast.show("알림은 보냈어. '알림 읽기'를 켜면 배웅할게")
        }
    }

    // 첫 실행: 안내 먼저
    if (!current.onboardingDone) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Role.ScreenBg)
                .systemBarsPadding(),
        ) {
            OnboardingScreen(
                resumeTick = resumeTick,
                onFinish = { scope.launch { repo.setOnboardingDone(true) } },
            )
        }
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Role.ScreenBg)
            // 화면 어디를 만지든 '마지막으로 만진 시각' 기록 (터치는 그대로 아래로 전달)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        lastTouch[0] = SystemClock.uptimeMillis()
                    }
                }
            },
    ) {
        when {
            showPoseLab -> BlobPoseLab(onClose = { showPoseLab = false })
            showAdmin -> AdminMenu(
                exhibitionMode = current.exhibitionMode,
                resumeTick = resumeTick,
                onExhibitionModeChange = { on -> scope.launch { repo.setExhibitionMode(on) } },
                onResetAll = {
                    tabName = BsueTab.HOME.name
                    scope.launch { repo.resetRecords() }
                },
                onLoadDemo = { scope.launch { repo.loadDemoData() } },
                onUnlockAll = { scope.launch { repo.unlockAll() } },
                onReplayOnboarding = {
                    showAdmin = false
                    scope.launch { repo.setOnboardingDone(false) }
                },
                onOpenPoseLab = { showPoseLab = true },
                onClose = { showAdmin = false },
            )
            showScanner -> QrScanScreen(
                onResult = { qr ->
                    showScanner = false
                    handleQr(qr)
                },
                onClose = { showScanner = false },
            )
            showMarket -> MarketScreen(
                onExit = {
                    showMarket = false
                    if (current.exhibitionMode) showExhibitionStart = true
                },
                onHeld = { request ->
                    scope.launch {
                        repo.saveHeldReceipt(
                            Receipt(
                                createdAt = System.currentTimeMillis(),
                                amount = request.amount,
                                guardianId = request.guardian.id,
                                appLabel = request.appLabel,
                            )
                        )
                    }
                },
                onSendFakePayment = sendFakePayment,
            )
            showLeakCheck -> LeakCheckScreen(
                state = current,
                onBack = { showLeakCheck = false },
                onSave = { sub -> scope.launch { repo.saveSubscription(sub) } },
                onDelete = { id -> scope.launch { repo.deleteSubscription(id) } },
                onDismissSuspect = { key -> scope.launch { repo.dismissAutoPay(key) } },
                onLoadDemo = { scope.launch { repo.loadDemoData() } },
                onClearPayments = { scope.launch { repo.clearPayments() } },
            )
            // 설정: 홈 오른쪽 위 아이콘으로 들어오는 화면
            showSettings -> SubScreen(title = "설정", onBack = { showSettings = false }) {
                SettingsScreen(
                    state = current,
                    resumeTick = resumeTick,
                    onDailyLimitChange = { limit -> scope.launch { repo.setDailyLimit(limit) } },
                    onRestingChange = { on -> scope.launch { repo.setRestingToday(on) } },
                    onUnlockAll = { scope.launch { repo.unlockAll() } },
                    onResetRecords = { scope.launch { repo.resetRecords() } },
                    onReplayOnboarding = {
                        showSettings = false
                        tabName = BsueTab.HOME.name
                        scope.launch { repo.setOnboardingDone(false) }
                    },
                    onAddCardPackage = { pkg -> scope.launch { repo.addCardPackage(pkg) } },
                    onRemoveCardPackage = { pkg -> scope.launch { repo.removeCardPackage(pkg) } },
                    onSendFakePayment = { sendFakePayment(null, null) },
                    onTestDispatch = {
                        val sample = TestSamples[testIndex % TestSamples.size]
                        testIndex++
                        Guardians.byId(sample.guardianId)?.let { guardian ->
                            testRequest = DispatchRequest(guardian, sample.amount, sample.appLabel, isTest = true)
                        }
                    },
                )
            }
            // 평소: 탭 3개
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth().statusBarsPadding()) {
                    // 탭 전환 250ms
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = { fadeIn(tween(BSUE_TRANSITION_MS)) togetherWith fadeOut(tween(BSUE_TRANSITION_MS / 2)) },
                        label = "tab",
                    ) { shown ->
                        when (shown) {
                            BsueTab.HOME -> Column(Modifier.fillMaxSize()) {
                                HomeTopBar(
                                    onSettings = { showSettings = true },
                                    onSecret = { showAdmin = true },
                                )
                                // ⚠️ 2단계에서 새 홈으로 바꿀 자리 (지금은 기존 홈)
                                Box(Modifier.weight(1f)) {
                                    HomeScreen(
                                        state = current,
                                        onFlipTodayCard = { scope.launch { repo.flipTodayCard() } },
                                        onMissionResult = { done -> scope.launch { repo.setMissionResult(done) } },
                                        onOpenMarket = { showMarket = true },
                                        onOpenLeakCheck = { showLeakCheck = true },
                                        onSecretTap = { showAdmin = true },
                                    )
                                }
                            }
                            // ⚠️ 5단계에서 '수사대'로 바꿀 자리 (지금은 기존 도감)
                            BsueTab.SQUAD -> DexScreen(
                                state = current,
                                onScanQr = { showScanner = true },
                                onShowPhotocard = { type -> photocardType = type },
                            )
                            // ⚠️ 6단계에서 새 기록으로 바꿀 자리 (지금은 기존 영수증)
                            BsueTab.RECORD -> ReceiptScreen(
                                state = current,
                                onMarkSeen = { until -> scope.launch { repo.markReceiptsSeen(until) } },
                            )
                        }
                    }
                }
                BsueTabBar(selected = tab, onSelect = { tabName = it.name })
            }
        }

        // 출동 테스트: 앱 안에서 출동 화면 그대로 띄우기 (뒤로가기 = 그냥 닫기)
        testRequest?.let { request ->
            BackHandler { testRequest = null }
            DispatchOverlay(
                request = request,
                savedThisMonth = current.savedThisMonth,
                onOpenReceipts = {
                    showSettings = false
                    tabName = BsueTab.RECORD.name
                },
            ) { result ->
                if (result == DispatchResult.HELD) {
                    scope.launch {
                        repo.saveHeldReceipt(
                            Receipt(
                                createdAt = System.currentTimeMillis(),
                                amount = request.amount,
                                guardianId = request.guardian.id,
                                appLabel = request.appLabel,
                            )
                        )
                    }
                }
                testRequest = null
            }
        }

        // ── 해금 · 소환 · 포토카드 연출 (한 번에 하나씩) ──
        val revealGuardian = current.pendingReveal.firstOrNull()?.let { Guardians.byId(it) }
        // 6신을 처음 다 모은 세트 → 포토카드 화면 자동으로 한 번
        val newlyCompleted = GuardianType.entries.firstOrNull { type ->
            type.name !in current.photocardShown &&
                Guardians.ofType(type).all { it.id in current.unlockedIds }
        }
        when {
            showAdmin || showPoseLab || showScanner || showExhibitionStart -> Unit
            showSummon -> SummonCelebration(onDone = { showSummon = false })
            rescanned != null -> UnlockCelebration(
                guardian = rescanned!!,
                title = "이미 만난 수호신이야!",
                onDone = { rescanned = null },
            )
            revealGuardian != null -> UnlockCelebration(
                guardian = revealGuardian,
                onDone = { scope.launch { repo.markRevealed(revealGuardian.id) } },
            )
            photocardType != null -> PhotoCardScreen(
                type = photocardType!!,
                guardians = Guardians.ofType(photocardType!!),
                onClose = { photocardType = null },
            )
            newlyCompleted != null -> PhotoCardScreen(
                type = newlyCompleted,
                guardians = Guardians.ofType(newlyCompleted),
                onClose = { scope.launch { repo.markPhotocardShown(newlyCompleted.name) } },
            )
        }

        // 전시 시작 화면 (전시 모드일 때 맨 위) — ⚠️ 4단계에서 새 시작 화면으로 바꿀 자리
        if (showExhibitionStart && current.exhibitionMode && !showAdmin) {
            ExhibitionGuide(
                onStart = {
                    showExhibitionStart = false
                    showMarket = true
                },
                onSkip = { showExhibitionStart = false },
            )
        }

        // 토스트 (탭바 위 16)
        ToastHost(toast, bottomPadding = if (!showSettings && !showMarket && !showLeakCheck) TAB_BAR_HEIGHT else 0.dp)
    }
}

// ── 홈 상단 바: 왼쪽 워드마크 '비상수단' · 오른쪽 설정 아이콘 ──
// 워드마크를 5번 연속 탭하면 숨은 관리자 메뉴 (전시 모드 나가기 등)
@Composable
private fun HomeTopBar(onSettings: () -> Unit, onSecret: () -> Unit) {
    val taps = remember { mutableListOf<Long>() }
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = BsueSpace.ScreenSide),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "비상수단",
            style = BsueType.BrandL,
            color = Role.TextPrimary,
            modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                val now = SystemClock.uptimeMillis()
                if (taps.isNotEmpty() && now - taps.last() > 1000) taps.clear()
                taps.add(now)
                if (taps.size >= 5) {
                    taps.clear()
                    onSecret()
                }
            },
        )
        Spacer(Modifier.weight(1f))
        Box(Modifier.clickable(onClick = onSettings).padding(BsueSpace.X8)) { SettingsIcon() }
    }
}

// ── 하위 화면 틀: 위에 ‹ 뒤로 + 제목 (설정 등) ──
@Composable
private fun SubScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        Modifier
            .fillMaxSize()
            .background(Role.ScreenBg)
            .statusBarsPadding(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = BsueSpace.X16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "‹",
                style = BsueType.Title1,
                color = Role.TextPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(horizontal = BsueSpace.X8),
            )
            Spacer(Modifier.width(BsueSpace.X4))
            Text(title, style = BsueType.Title3, color = Role.TextPrimary)
        }
        Box(Modifier.weight(1f)) { Column { content() } }
    }
}

// 출동 테스트 때 돌아가며 쓰는 예시 (수호신 / 금액 / 앱)
private data class TestSample(val guardianId: String, val amount: Int, val appLabel: String)

private val TestSamples = listOf(
    TestSample("delivery_seal", 23_800, "배달의민족"),
    TestSample("sale", 59_000, "무신사"),
    TestSample("free_shipping", 12_900, "쿠팡"),
    TestSample("taxi", 14_200, "카카오 T"),
    TestSample("fridge", 41_500, "컬리"),
)
