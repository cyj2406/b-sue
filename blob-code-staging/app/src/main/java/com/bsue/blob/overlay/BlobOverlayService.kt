package com.bsue.blob.overlay

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bsue.blob.MainActivity
import com.bsue.blob.R
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.Guardian
import com.bsue.blob.data.Guardians
import com.bsue.blob.notify.PaymentNotice
import com.bsue.blob.ui.blob.BlobLooks
import com.bsue.blob.ui.blob.BlobScene
import com.bsue.blob.ui.theme.BlobTheme
import com.bsue.blob.ui.theme.TapeYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// 다른 앱 위에 항상 떠 있는 블롭 (컴냥처럼)
// - 포그라운드 서비스: 알림창에 "출동 대기 중"이 떠 있는 동안 계속 살아 있음
// - 드래그 → 놓으면 좌우 가장자리에 말랑하게 붙음
// - 톡 → 말풍선 (말풍선 누르면 앱 열림) / 길게 누르기 → "오늘은 쉬어" 켜고 끄기
// - 빼꼼 → 접근성 서비스가 쇼핑·배달앱을 감지하면 화면 모서리에서 빼꼼 + 수호신 질문
// - 배웅 → 카드 승인 알림이 오면 흔들흔들 손 흔들며 "잘 가, ○○원…"
class BlobOverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "blob_bubble"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "com.bsue.blob.action.STOP_BUBBLE"
        private const val ACTION_PEEK = "com.bsue.blob.action.PEEK"
        private const val EXTRA_GUARDIAN_ID = "guardian_id"
        private const val ACTION_FAREWELL = "com.bsue.blob.action.FAREWELL"
        private const val EXTRA_AMOUNT = "amount"
        private const val EXTRA_MERCHANT = "merchant"
        private const val SPEECH_SHOW_MS = 4000L

        // 지금 떠 있는지 (빼꼼 요청 보낼 방법 고를 때 사용)
        @Volatile
        var isRunning = false
            private set

        // 권한이 있을 때만 출동
        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            ContextCompat.startForegroundService(context, Intent(context, BlobOverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BlobOverlayService::class.java))
        }

        // 접근성 서비스가 부름: 이 수호신으로 빼꼼! (블롭이 쉬고 있었으면 깨워서 출동)
        fun peek(context: Context, guardianId: String) {
            send(
                context,
                Intent(context, BlobOverlayService::class.java)
                    .setAction(ACTION_PEEK)
                    .putExtra(EXTRA_GUARDIAN_ID, guardianId),
            )
        }

        // 알림 읽기가 부름: 결제 알림 배웅
        fun farewell(context: Context, notice: PaymentNotice) {
            send(
                context,
                Intent(context, BlobOverlayService::class.java)
                    .setAction(ACTION_FAREWELL)
                    .putExtra(EXTRA_AMOUNT, notice.amount ?: -1)
                    .putExtra(EXTRA_MERCHANT, notice.merchant),
            )
        }

        private fun send(context: Context, intent: Intent) {
            if (!Settings.canDrawOverlays(context)) return
            try {
                if (isRunning) context.startService(intent)
                else ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                // 안드로이드가 백그라운드 시작을 막으면 이번 출동은 건너뜀
            }
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var repo: BlobRepository
    private val owner = OverlayLifecycleOwner()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ui = BubbleUiState()

    private var blobView: View? = null
    private var speechView: View? = null
    private lateinit var blobParams: WindowManager.LayoutParams
    private var posX = 0f
    private var posY = 0f
    private var snapAnimator: ValueAnimator? = null
    private var hideSpeechJob: Job? = null
    private var lineIndex = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        repo = BlobRepository(this)
        owner.onCreate()

        if (!startAsForeground() || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        addBlobWindow()
        owner.onResume()
        isRunning = blobView != null

        // "오늘은 쉬어" 상태를 앱 설정과 같이 씀
        scope.launch {
            repo.state.collect { ui.resting = it.restingToday }
        }
        // 블롭 출동 앱을 보고 있거나 전체화면 출동 중이면 숨기기
        scope.launch {
            combine(OverlayBus.appInForeground, OverlayBus.dispatchShowing) { app, dispatch -> app || dispatch }
                .collect { hidden ->
                    blobView?.visibility = if (hidden) View.GONE else View.VISIBLE
                    if (hidden) hideSpeech()
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PEEK -> {
                val guardian = intent.getStringExtra(EXTRA_GUARDIAN_ID)?.let { Guardians.byId(it) }
                if (guardian != null) peekOut(guardian)
            }
            ACTION_FAREWELL -> {
                val amount = intent.getIntExtra(EXTRA_AMOUNT, -1).takeIf { it >= 0 }
                waveGoodbye(amount, intent.getStringExtra(EXTRA_MERCHANT))
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        snapAnimator?.cancel()
        scope.cancel()
        hideSpeech()
        blobView?.let { removeWindow(it) }
        blobView = null
        owner.onDestroy()
        super.onDestroy()
    }

    // ── 알림 + 포그라운드 시작 ──────────────────────────────

    private fun startAsForeground(): Boolean {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "떠 있는 블롭", NotificationManager.IMPORTANCE_MIN).apply {
                description = "블롭이 화면 위에 떠 있는 동안 보여요"
                setShowBadge(false)
            }
        )

        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val sendHome = PendingIntent.getService(
            this, 1,
            Intent(this, BlobOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blob_notify)
            .setContentTitle("블롭 출동 대기 중")
            .setContentText("화면 위에서 지켜보고 있을게")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(openApp)
            .addAction(0, "집에 보내기", sendHome)
            .build()

        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        return try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── 블롭 창 ─────────────────────────────────────────

    private fun addBlobWindow() {
        val size = dp(BUBBLE_WINDOW_DP)
        val screen = screenSize()
        posX = (screen.x - size + edgeInset()).toFloat() // 처음엔 오른쪽 벽
        posY = screen.y * 0.55f
        ui.dockedRight = true

        blobParams = overlayParams(size, size).apply {
            gravity = Gravity.TOP or Gravity.START
            x = posX.toInt()
            y = posY.toInt()
        }

        val root = BubbleTouchLayout(this).apply {
            attachOwners(this)
            addView(composeView { FloatingBlob(ui) })
            listener = object : BubbleTouchLayout.Listener {
                override fun onDragStart() {
                    snapAnimator?.cancel()
                    hideSpeech()
                    ui.dragging = true
                }

                override fun onDragBy(dx: Float, dy: Float) {
                    val maxY = (screenSize().y - blobParams.height).toFloat()
                    posX += dx
                    posY = (posY + dy).coerceIn(0f, maxY)
                    blobParams.x = posX.toInt()
                    blobParams.y = posY.toInt()
                    updateBlobWindow()
                }

                override fun onDragEnd() {
                    ui.dragging = false
                    snapToEdge()
                }

                override fun onTap() {
                    when {
                        speechView != null -> openApp()
                        ui.resting -> showSpeech(SleepingLine)
                        else -> {
                            showSpeech(TapLines[lineIndex % TapLines.size])
                            lineIndex++
                        }
                    }
                }

                override fun onLongPress() {
                    val restNow = !ui.resting
                    scope.launch { repo.setRestingToday(restNow) }
                    showSpeech(if (restNow) GoRestLine else WakeUpLine)
                }
            }
        }

        try {
            windowManager.addView(root, blobParams)
            blobView = root
        } catch (e: Exception) {
            stopSelf() // 권한이 꺼져 있으면 조용히 퇴근
        }
    }

    // 손을 놓으면 가까운 좌우 벽으로 '띠용' 하고 붙기
    private fun snapToEdge() {
        val size = blobParams.width
        val screenW = screenSize().x
        val toRight = blobParams.x + size / 2 > screenW / 2
        val target = if (toRight) screenW - size + edgeInset() else -edgeInset()
        ui.dockedRight = toRight

        // 벽을 살짝 지나쳤다가 돌아옴 → 붙는 순간 말랑
        animateX(target, 420, OvershootInterpolator(1.4f)) { ui.squish++ }
    }

    // 빼꼼: 벽 밖으로 쏙 숨었다가 → 반쯤 고개 내밀고 멈칫 → 톡 튀어나오며 수호신 질문
    private fun peekOut(guardian: Guardian) {
        if (blobView == null || ui.resting) return
        if (OverlayBus.appInForeground.value || OverlayBus.dispatchShowing.value) return
        hideSpeech()
        val size = blobParams.width
        val screenW = screenSize().x
        val inset = edgeInset()
        val hiddenX = if (ui.dockedRight) screenW else -size
        val halfX = if (ui.dockedRight) screenW - size / 2 else -size / 2
        val dockedX = if (ui.dockedRight) screenW - size + inset else -inset

        blobParams.x = hiddenX
        posX = hiddenX.toFloat()
        updateBlobWindow()
        ui.scene = BlobScene.PEEK // 살금살금 포즈 (오른쪽 벽이면 좌우 반전)

        animateX(halfX, 260, DecelerateInterpolator()) {
            ui.peek++ // 고개 갸웃
            animateX(dockedX, 380, OvershootInterpolator(2f), startDelay = 450) {
                ui.squish++
                showSpeech(
                    text = guardian.question,
                    title = guardian.name,
                    accent = guardian.color,
                    fromPeek = true,
                    art = BlobLooks.missionPeekArt(guardian.id),
                    scene = BlobScene.PEEK,
                )
            }
        }
    }

    // 배웅: 쉬는 중이어도 손은 흔들어 줌 (눈 감은 채로)
    private fun waveGoodbye(amount: Int?, merchant: String?) {
        if (blobView == null) return
        if (OverlayBus.appInForeground.value || OverlayBus.dispatchShowing.value) return
        showSpeech(
            text = farewellLine(amount, merchant),
            title = "배웅",
            accent = TapeYellow,
            fromPeek = true,
            scene = BlobScene.FAREWELL, // '괜찮아' 포즈로 천천히 기울기
        )
    }

    // 블롭 창을 x 위치로 움직이는 애니메이션
    private fun animateX(
        target: Int,
        durationMs: Long,
        interpolator: TimeInterpolator,
        startDelay: Long = 0,
        onEnd: () -> Unit = {},
    ) {
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(blobParams.x, target).apply {
            duration = durationMs
            this.startDelay = startDelay
            this.interpolator = interpolator
            addUpdateListener {
                blobParams.x = it.animatedValue as Int
                posX = blobParams.x.toFloat()
                updateBlobWindow()
            }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }
                override fun onAnimationEnd(animation: Animator) {
                    if (!cancelled) onEnd()
                }
            })
            start()
        }
    }

    private fun updateBlobWindow() {
        val view = blobView ?: return
        try {
            windowManager.updateViewLayout(view, blobParams)
        } catch (e: Exception) {
        }
    }

    // ── 말풍선 창 ────────────────────────────────────────

    private fun showSpeech(
        text: String,
        title: String? = null,
        accent: Color? = null,
        fromPeek: Boolean = false,
        @DrawableRes art: Int? = null,
        scene: BlobScene = BlobScene.FLOATING, // 말풍선이 떠 있는 동안의 포즈
    ) {
        hideSpeech()
        ui.scene = scene
        val gap = dp(4)
        val params = overlayParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        ).apply {
            // 오른쪽 벽이면 블롭 왼쪽에, 왼쪽 벽이면 블롭 오른쪽에
            gravity = Gravity.TOP or if (ui.dockedRight) Gravity.END else Gravity.START
            x = blobParams.width - edgeInset() + gap
            y = blobParams.y + dp(10)
        }
        val view = composeView {
            SpeechBubble(
                text = text,
                title = title,
                accent = accent,
                showOfficeLink = !fromPeek,
                art = art,
                // 빼꼼 말풍선은 누르면 그냥 닫힘 (쇼핑 흐름을 막지 않기)
                onClick = { if (fromPeek) hideSpeech() else openApp() },
            )
        }
        attachOwners(view)
        try {
            windowManager.addView(view, params)
            speechView = view
        } catch (e: Exception) {
            return
        }
        hideSpeechJob = scope.launch {
            delay(SPEECH_SHOW_MS)
            hideSpeech()
        }
    }

    private fun hideSpeech() {
        ui.scene = BlobScene.FLOATING // 말풍선이 닫히면 평소 포즈로
        hideSpeechJob?.cancel()
        hideSpeechJob = null
        speechView?.let { removeWindow(it) }
        speechView = null
    }

    private fun openApp() {
        hideSpeech()
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        )
    }

    // ── 도우미 ──────────────────────────────────────────

    private fun overlayParams(width: Int, height: Int) = WindowManager.LayoutParams(
        width,
        height,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        // 키보드·뒤로가기는 아래 앱이 그대로 받고, 화면 끝을 살짝 넘어가도 됨
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    )

    private fun composeView(content: @Composable () -> Unit) = ComposeView(this).apply {
        setContent { BlobTheme { content() } }
    }

    private fun attachOwners(view: View) {
        view.setViewTreeLifecycleOwner(owner)
        view.setViewTreeSavedStateRegistryOwner(owner)
    }

    private fun removeWindow(view: View) {
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
        }
    }

    // 창 여백만큼 바깥으로 빼서 블롭 몸이 벽에 딱 붙게
    private fun edgeInset(): Int = dp((BUBBLE_WINDOW_DP - BLOB_DP) / 2)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun screenSize(): Point =
        if (Build.VERSION.SDK_INT >= 30) {
            val bounds = windowManager.currentWindowMetrics.bounds
            Point(bounds.width(), bounds.height())
        } else {
            val metrics = resources.displayMetrics
            Point(metrics.widthPixels, metrics.heightPixels)
        }
}
