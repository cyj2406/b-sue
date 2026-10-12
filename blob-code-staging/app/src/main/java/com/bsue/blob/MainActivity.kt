package com.bsue.blob

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bsue.blob.leak.GhostSubAlarm
import com.bsue.blob.mission.MissionAlarm
import com.bsue.blob.overlay.BlobOverlayService
import com.bsue.blob.overlay.OverlayBus
import com.bsue.blob.ui.BlobApp
import com.bsue.blob.ui.theme.BlobTheme

class MainActivity : ComponentActivity() {

    // 앱으로 돌아올 때마다 +1 → 설정·안내 화면이 권한 상태를 다시 확인
    private var resumeTick by mutableIntStateOf(0)

    // 기본 카메라로 카드 QR(bsue://card/03)을 찍으면 이 앱이 열리면서 주소가 들어옴
    private var deepLink by mutableStateOf<String?>(null)

    // 저장된 값을 다 읽을 때까지 스플래시(오렌지 + 블롭)를 계속 보여줌
    private var appReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !appReady }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // 위쪽(크림 배경): 어두운 아이콘 / 아래쪽(검정 탭바): 밝은 아이콘
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // 유령 구독 색출: 매일 저녁 8시 / 오늘의 미션: 아침 9시·밤 10시 예약
        GhostSubAlarm.scheduleNext(this)
        MissionAlarm.scheduleAll(this)
        deepLink = intent?.dataString
        setContent {
            BlobTheme {
                BlobApp(
                    resumeTick = resumeTick,
                    deepLink = deepLink,
                    onDeepLinkHandled = { deepLink = null },
                    onReady = { appReady = true },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.dataString?.let { deepLink = it }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
        // 앱을 보는 동안엔 떠 있는 블롭은 숨김
        OverlayBus.appInForeground.value = true
        // '다른 앱 위에 표시' 권한이 있으면 블롭 출동 (이미 떠 있으면 그대로)
        BlobOverlayService.start(this)
    }

    override fun onPause() {
        super.onPause()
        OverlayBus.appInForeground.value = false
    }
}
