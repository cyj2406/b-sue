package com.bsue.blob.ui.theme

import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════
//  색 역할표 — DESIGN.md 의 색 '이름'을 지금 앱 팔레트(Color.kt)에 연결
//  새 색은 없어요. 원래 색 + 투명도만 써요. (흰색은 Compose 기본 Color.White)
// ════════════════════════════════════════════════
object Role {
    // 바탕
    val ScreenBg = BlobCream                              // grey100 역할: 홈·탭 화면 배경
    val Surface = Color.White                             // white 역할: 카드, 시트
    val Pressed = BlobBlack.copy(alpha = 0.04f)           // grey50 역할: 리스트 눌림

    // 글자
    val TextPrimary = BlobBlack                           // grey900 역할: 제목·숫자·주요 글씨
    val TextSecondary = BlobGray                          // grey600 역할: 캡션, 날짜, 메타
    val TextDisabled = SilhouetteInk                      // grey400 역할: 비활성 탭, placeholder

    // 선
    val Divider = SilhouetteGray                          // grey200 역할: 구분선, 시트 손잡이

    // 포인트 (행동과 성과에만)
    val Point = BlobOrange                                // orange500 역할: 주 버튼, 진행률, 선택 점
    val PointWeakBg = BlobOrange.copy(alpha = 0.10f)      // orange50 역할: 약한 버튼, 선택 칩, 배지 바탕
    val PointTrack = BlobOrange.copy(alpha = 0.18f)       // orange100 역할: 진행률 트랙

    // 보조 버튼 · 칩
    val SecondaryBg = SilhouetteGray.copy(alpha = 0.55f)  // grey100 역할 (흰 카드·시트 위)
    val SecondaryText = BlobBlack.copy(alpha = 0.75f)     // grey700 역할
    val AvatarBg = BlobCream                              // grey100 역할: 아바타 원

    // 어두운 곳 (출동 화면·모달)
    val Dim = BlobBlack.copy(alpha = 0.6f)                // dim 역할: 시트 뒤
    val DarkSecondaryBg = Color.White.copy(alpha = 0.12f) // 출동 화면 보조 버튼
    val DarkSecondaryText = Color.White.copy(alpha = 0.85f)
}
