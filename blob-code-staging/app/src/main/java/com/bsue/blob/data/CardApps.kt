package com.bsue.blob.data

// 결제 알림을 읽을 카드사·간편결제 앱 (처음 기본값). 설정 화면에서 더하거나 뺄 수 있어요.
// 패키지명은 구글 플레이 주소의 "id=" 뒤 글자 (2026-10 확인)
object CardApps {

    val defaults: Map<String, String> = linkedMapOf(
        "com.shcard.smartpay" to "신한 SOL페이",
        "com.kbcard.cxh.appcard" to "KB Pay",
        "com.hyundaicard.appcard" to "현대카드",
        "com.lcacApp" to "롯데카드 디지로카",
        "com.wooricard.smartapp" to "우리WON카드",
        "com.hanaskcard.paycla" to "하나Pay",
        "nh.smart.nhallonepay" to "NH pay",
        "com.kakaobank.channel" to "카카오뱅크",
        "viva.republica.toss" to "토스",
        "com.kakaopay.app" to "카카오페이",
    )

    fun label(packageName: String): String = defaults[packageName] ?: packageName
}
