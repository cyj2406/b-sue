package com.bsue.blob.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ════════════════════════════════════════════════
//  비상수단 컬러 — 원래 앱 팔레트 그대로 (새 색 추가 금지)
//  필요한 색이 없으면 아래 색의 투명도(.copy(alpha = …))만 조절해서 써요.
//
//  DESIGN.md 의 색 이름은 '역할'로만 읽어요:
//    orange 계열 → BlobOrange (포인트)
//    grey 계열   → BlobGray · SilhouetteInk · SilhouetteGray · BlobCream (배경·구분선·보조 글씨)
//    grey900     → BlobBlack (기본 글씨)
// ════════════════════════════════════════════════

// ── 브랜드 ──
val BlobOrange = Color(0xFFD64518) // 포인트: 주 버튼, 지킨 돈, 진행률, 선택 상태
val BlobBlack = Color(0xFF111111)  // 기본 글씨, 출동 화면 배경
val BlobCream = Color(0xFFF4ECDD)  // 화면 배경, 영수증 종이

// ── 무채색 (배경·구분선·보조 글씨) ──
val BlobGray = Color(0xFF8A8478)       // 보조 글씨, 캡션
val SilhouetteInk = Color(0xFFB9AE9B)  // 비활성, 잠긴 카드 실루엣
val SilhouetteGray = Color(0xFFDCD3C3) // 구분선, 보조 버튼 배경, 잠긴 카드 바탕

// ── 캐릭터·장식 ──
val TapeYellow = Color(0xFFFFC90E) // 경고테이프 노랑 (토스 스타일 화면에선 안 씀)
val BlobBrown = Color(0xFF7A4A24)  // 탐정모자 (임시 그림)

// 카드 색 위에 올릴 글자색: 밝은 카드엔 검정, 어두운 카드엔 크림
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.45f) BlobBlack else BlobCream
