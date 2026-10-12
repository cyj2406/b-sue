package com.bsue.blob.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bsue.blob.R

// ════════════════════════════════════════════════
//  비상수단 글자 — DESIGN.md 3번
//  기본: Pretendard / 브랜드: Black Han Sans (출동 질문 · 영수증 헤더 · 로고에만)
// ════════════════════════════════════════════════

// res/font/pretendard_*.ttf (orioncactus/pretendard v1.3.9)
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),     // 400
    Font(R.font.pretendard_medium, FontWeight.Medium),      // 500
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),  // 600
    Font(R.font.pretendard_bold, FontWeight.Bold),          // 700
)

// res/font/black_han_sans_regular.ttf — 출동 화면 · 영수증 · 로고 전용
val BlackHanSans = FontFamily(Font(R.font.black_han_sans_regular))

private val Tight = (-0.02).em // 제목·숫자 글자 간격

// 글자 크기 표 (크기 / 줄높이 / 굵기)
object BsueType {
    // 지킨 돈 금액, 결과 화면 금액
    val Display = TextStyle(fontFamily = Pretendard, fontSize = 34.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = Tight)
    // 온보딩·결과 화면 큰 제목
    val Title1 = TextStyle(fontFamily = Pretendard, fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = Tight)
    // 화면 제목 (탭 상단)
    val Title2 = TextStyle(fontFamily = Pretendard, fontSize = 22.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold, letterSpacing = Tight)
    // 카드 제목
    val Title3 = TextStyle(fontFamily = Pretendard, fontSize = 19.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = Tight)
    // 리스트 제목
    val Body1 = TextStyle(fontFamily = Pretendard, fontSize = 17.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium)
    // 버튼 글씨 (Body1 크기, SemiBold)
    val Button = Body1.copy(fontWeight = FontWeight.SemiBold)
    // 본문, 말풍선
    val Body2 = TextStyle(fontFamily = Pretendard, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    // 날짜, 환산("커피 10잔"), 메타
    val Caption = TextStyle(fontFamily = Pretendard, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    // 탭 라벨, 배지
    val Small = TextStyle(fontFamily = Pretendard, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)

    // ── 브랜드 (Black Han Sans) ──
    // 출동 질문 "정말 긁을 거야?"
    val BrandXL = TextStyle(fontFamily = BlackHanSans, fontSize = 34.sp, lineHeight = 42.sp)
    // 로고 워드마크, 영수증 헤더
    val BrandL = TextStyle(fontFamily = BlackHanSans, fontSize = 22.sp, lineHeight = 28.sp)

    // 출동 3·2·1 숫자
    val Count = TextStyle(fontFamily = Pretendard, fontSize = 96.sp, lineHeight = 96.sp, fontWeight = FontWeight.Bold, letterSpacing = Tight)
}

// Material 기본 글자(버튼·입력창 등)도 Pretendard로
val BsueTypography = Typography(
    displayLarge = BsueType.Display,
    displayMedium = BsueType.Display,
    displaySmall = BsueType.Display,
    headlineLarge = BsueType.Title1,
    headlineMedium = BsueType.Title1,
    headlineSmall = BsueType.Title2,
    titleLarge = BsueType.Title2,
    titleMedium = BsueType.Title3,
    titleSmall = BsueType.Body1,
    bodyLarge = BsueType.Body1,
    bodyMedium = BsueType.Body2,
    bodySmall = BsueType.Caption,
    labelLarge = BsueType.Button,
    labelMedium = BsueType.Caption,
    labelSmall = BsueType.Small,
)
