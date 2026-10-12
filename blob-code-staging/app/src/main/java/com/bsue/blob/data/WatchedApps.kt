package com.bsue.blob.data

// 블롭이 지켜보는 앱: 패키지명 → 담당 수호신
// 패키지명 = 앱의 고유 주소. 구글 플레이 웹페이지 주소의 "id=" 뒤에 있는 글자예요.
// (예: play.google.com/store/apps/details?id=com.sampleapp → 배달의민족)
// 앱을 추가하려면 아래 목록에 한 줄만 더 넣으면 돼요.
data class WatchedApp(
    val packageName: String,
    val label: String,       // 사람이 읽는 앱 이름 (설정 화면 표시용)
    val guardianId: String,  // Guardian.kt 의 id
)

object WatchedApps {

    val all: List<WatchedApp> = listOf(
        // 쇼핑
        WatchedApp("com.coupang.mobile", "쿠팡", "cart_cleanup"),
        // 배달
        WatchedApp("com.sampleapp", "배달의민족", "delivery_seal"),
        WatchedApp("com.coupang.mobile.eats", "쿠팡이츠", "delivery_seal"),
        WatchedApp("com.fineapp.yogiyo", "요기요", "delivery_seal"),
        // 패션
        WatchedApp("com.musinsa.store", "무신사", "closet"),
        WatchedApp("com.croquis.zigzag", "지그재그", "closet"),
        WatchedApp("com.the29cm.app29cm", "29CM", "closet"),
        // 장보기
        WatchedApp("com.dbs.kurly.m2", "컬리", "fridge"),
        // 택시
        WatchedApp("com.kakao.taxi", "카카오 T", "taxi"),
    )

    fun find(packageName: String): WatchedApp? = all.firstOrNull { it.packageName == packageName }
}
