package com.bsue.blob.overlay

import com.bsue.blob.notify.PaymentNotice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

// 앱 화면 ↔ 떠 있는 블롭 ↔ 접근성 서비스 사이의 연락통
object OverlayBus {
    // 블롭 출동 앱을 보고 있는 동안엔 떠 있는 블롭을 숨김 (홈에 블롭이 이미 있으니까)
    val appInForeground = MutableStateFlow(false)

    // 전체화면 '출동' 중엔 떠 있는 블롭을 숨김 (출동 화면에 블롭이 나오니까)
    val dispatchShowing = MutableStateFlow(false)

    // 최근에 열린 앱 패키지명 (최신이 맨 앞, 8개까지) — 설정 화면에서 패키지명 확인용
    val recentPackages = MutableStateFlow<List<String>>(emptyList())

    fun recordPackage(packageName: String) {
        recentPackages.value = (listOf(packageName) + recentPackages.value.filter { it != packageName }).take(8)
    }

    // 결제 알림 → 앱 화면 안에서 배웅 (앱을 보고 있을 때)
    val farewell = MutableSharedFlow<PaymentNotice>(extraBufferCapacity = 4)

    // 카드사 목록엔 없지만 '승인' 알림을 보낸 앱 — 설정에서 '추가할까?' 후보
    val paymentLikePackages = MutableStateFlow<List<String>>(emptyList())

    fun recordPaymentLikePackage(packageName: String) {
        paymentLikePackages.value = (listOf(packageName) + paymentLikePackages.value.filter { it != packageName }).take(5)
    }
}
