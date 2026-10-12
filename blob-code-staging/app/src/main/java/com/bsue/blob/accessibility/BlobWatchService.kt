package com.bsue.blob.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.Guardians
import com.bsue.blob.data.Receipt
import com.bsue.blob.data.WatchedApp
import com.bsue.blob.data.WatchedApps
import com.bsue.blob.dispatch.CheckoutDetector
import com.bsue.blob.dispatch.DispatchOverlay
import com.bsue.blob.dispatch.DispatchRequest
import com.bsue.blob.dispatch.DispatchResult
import com.bsue.blob.overlay.BlobOverlayService
import com.bsue.blob.overlay.OverlayBus
import com.bsue.blob.overlay.OverlayLifecycleOwner
import com.bsue.blob.ui.theme.BlobTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 접근성 서비스 — 블롭의 눈
// ① 지켜보는 앱이 열리면 → 블롭 빼꼼 (같은 앱 10분 쿨다운)
// ② 그 앱 화면에 '결제하기/주문하기/구매하기/○○원 결제'가 보이면 → 전체화면 '출동'
//    화면 글자는 폰 안에서 그 순간만 살펴보고 저장하지 않아요. 폰 밖으로 아무것도 보내지 않아요.
class BlobWatchService : AccessibilityService() {

    companion object {
        private const val PEEK_COOLDOWN_MS = 10 * 60 * 1000L   // 같은 앱 빼꼼은 10분에 한 번
        private const val BOUGHT_SNOOZE_MS = 5 * 60 * 1000L    // [그래도 살래] 후 같은 화면 5분 쉼
        private const val SCAN_DEBOUNCE_MS = 400L              // 화면이 바뀌고 0.4초 뒤에 한 번만 읽기
        private const val MAX_NODES = 1500                     // 너무 긴 화면은 앞부분만
        private const val BACK_DELAY_MS = 250L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private val repo by lazy { BlobRepository(this) }
    private val owner = OverlayLifecycleOwner()

    // 빼꼼
    private val lastPeekAt = mutableMapOf<String, Long>()
    private var lastPackage: String? = null

    // 출동
    private var currentApp: WatchedApp? = null
    private var currentScreen: String? = null                 // "패키지/화면이름"
    private val snoozeUntil = mutableMapOf<String, Long>()    // 화면별 [그래도 살래] 쉼 끝나는 시각
    private var armed = true                                  // 결제 글자가 사라졌다 다시 나타나야 다음 출동
    private var dispatchView: View? = null

    private val scanRunnable = Runnable { scanForCheckout() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        owner.onCreate()
        owner.onResume()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return // 우리 앱·우리 창은 무시

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                onAppOrScreenChanged(pkg, event.className?.toString())
                scheduleScan()
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (pkg == currentApp?.packageName) scheduleScan()
            }
        }
    }

    // ── ① 앱이 열렸을 때: 빼꼼 ─────────────────────────────

    private fun onAppOrScreenChanged(pkg: String, className: String?) {
        val app = WatchedApps.find(pkg)
        // 키보드·알림창 같은 잠깐 뜨는 창은 '화면 바뀜'으로 치지 않음
        if (app == null && isTransientSystemWindow(pkg)) return

        currentApp = app
        if (app != null && className != null) currentScreen = "$pkg/$className"

        if (pkg == lastPackage) return
        lastPackage = pkg
        OverlayBus.recordPackage(pkg) // 설정 > '최근 연 앱' (패키지명 확인용)
        if (app == null) return

        val now = SystemClock.elapsedRealtime()
        val last = lastPeekAt[pkg]
        if (last != null && now - last < PEEK_COOLDOWN_MS) return
        lastPeekAt[pkg] = now

        scope.launch {
            // 오늘은 쉬어 / 하루 출동 제한 확인 + 출동 횟수 +1
            if (repo.tryDispatch()) {
                BlobOverlayService.peek(this@BlobWatchService, app.guardianId)
            }
        }
    }

    private fun isTransientSystemWindow(pkg: String): Boolean =
        pkg == "com.android.systemui" || pkg.contains("inputmethod") || pkg.contains("honeyboard")

    // ── ② 결제 직전: 출동 ───────────────────────────────

    private fun scheduleScan() {
        handler.removeCallbacks(scanRunnable)
        handler.postDelayed(scanRunnable, SCAN_DEBOUNCE_MS)
    }

    private fun scanForCheckout() {
        val app = currentApp ?: return
        if (dispatchView != null) return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != app.packageName) return

        val texts = collectTexts(root)

        // 결제 글자가 화면에서 사라지면 다시 출동할 준비
        if (!CheckoutDetector.hasCheckoutText(texts)) {
            armed = true
            return
        }
        if (!armed) return

        val screen = currentScreen ?: app.packageName
        val snoozed = (snoozeUntil[screen] ?: 0L) > SystemClock.elapsedRealtime()
        if (snoozed) return

        val scan = CheckoutDetector.analyze(texts, app) ?: return
        val guardian = Guardians.byId(scan.guardianId) ?: return
        armed = false // 이 결제 화면에선 한 번만

        scope.launch {
            if (!repo.tryDispatch()) return@launch // 오늘은 쉬어 / 하루 제한
            val saved = repo.state.first().savedThisMonth // 결과 시트 '이번 달 지킨 돈' 표시용
            showDispatch(
                DispatchRequest(guardian = guardian, amount = scan.amount, appLabel = app.label),
                screen,
                saved,
            )
        }
    }

    // 화면에 보이는 글자 모으기 (위에서 아래 순서)
    private fun collectTexts(root: AccessibilityNodeInfo): List<String> {
        val out = mutableListOf<String>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val node = queue.removeFirst()
            visited++
            if (node.isVisibleToUser) {
                node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
                node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(out::add)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let(queue::add)
            }
        }
        return out
    }

    // ── 출동 화면 창 ───────────────────────────────────

    private fun showDispatch(request: DispatchRequest, screen: String, savedThisMonth: Int) {
        if (dispatchView != null) return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            // 접근성 전용 창: 다른 앱 위에 확실히 뜨고, 따로 권한이 필요 없음
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            // 안드로이드 12 이상: 뒤 쇼핑 화면을 흐리게 (글자 겹침 방지)
            if (Build.VERSION.SDK_INT >= 31) {
                flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                blurBehindRadius = 40
            }
        }
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                BlobTheme {
                    DispatchOverlay(request, savedThisMonth = savedThisMonth) { result ->
                        onDispatchFinished(request, result, screen)
                    }
                }
            }
        }
        try {
            windowManager.addView(view, params)
            dispatchView = view
            OverlayBus.dispatchShowing.value = true
        } catch (e: Exception) {
            armed = true
        }
    }

    private fun onDispatchFinished(request: DispatchRequest, result: DispatchResult, screen: String) {
        hideDispatch()
        when (result) {
            DispatchResult.HELD -> {
                // 영수증 저장 + 뒤로가기로 결제화면 나가기
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
                handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, BACK_DELAY_MS)
            }
            DispatchResult.BOUGHT -> {
                // 같은 화면은 5분 동안 다시 안 나옴
                snoozeUntil[screen] = SystemClock.elapsedRealtime() + BOUGHT_SNOOZE_MS
            }
        }
    }

    private fun hideDispatch() {
        val view = dispatchView ?: return
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view)
        } catch (e: Exception) {
        }
        dispatchView = null
        OverlayBus.dispatchShowing.value = false
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        hideDispatch()
        scope.cancel()
        owner.onDestroy()
        super.onDestroy()
    }
}

// 우리 앱의 접근성 서비스가 켜져 있는지 확인 (설정·안내 화면에서 사용)
fun isBlobWatchOn(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    return enabled.split(':').any { it.startsWith("${context.packageName}/") }
}
