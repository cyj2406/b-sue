package com.bsue.blob.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

// 수호신 종류
enum class GuardianType(val label: String) {
    IMPULSE("충동"),
    LEAK("누수"),
}

// 12 수호신 카드 한 장의 정보. 앱 전체(홈·도감·출동 화면)가 이것 하나를 같이 씀.
data class Guardian(
    val id: String,         // 저장용 고유 이름 — 한 번 정하면 바꾸지 마세요 (바꾸면 해금 기록이 끊겨요)
    val number: Int,        // 도감 번호 1~12
    val type: GuardianType,
    val name: String,       // 카드 이름
    val question: String,   // 블롭의 멈칫 질문
    val trigger: String,    // 앱에서 나오는 순간
    val mission: String,    // 카드에 적힌 미션 (초안 — 자유롭게 고쳐도 돼요)
    val color: Color,       // 카드 고유 색
)

object Guardians {

    // ⚠️ 카드 색은 임시값이에요. 실제 HEX를 받으면 Color(0xFF______)의 밑줄 자리 6글자만 바꾸면 돼요.
    val all: List<Guardian> = listOf(
        // ── 충동 ──
        Guardian(
            id = "zero_day", number = 1, type = GuardianType.IMPULSE,
            name = "하루 0원 챌린지",
            question = "오늘 꼭 써야 돼?",
            trigger = "아침 데일리 미션",
            mission = "오늘 하루 0원으로 버텨보기. 못 해도 괜찮아, 내일 또 하면 돼!",
            color = Color(0xFFE23B2E), // 빨강 (임시)
        ),
        Guardian(
            id = "two_plus_one", number = 2, type = GuardianType.IMPULSE,
            name = "2+1의 함정",
            question = "하나 더 진짜 필요해?",
            trigger = "수량 2개 이상 · 편의점 결제",
            mission = "묶음 할인이 보이면 딱 3초, 진짜 쓸 개수만 담아보기",
            color = Color(0xFFF2B705), // 노랑 (임시)
        ),
        Guardian(
            id = "sale", number = 3, type = GuardianType.IMPULSE,
            name = "세일이라서 샀다",
            question = "세일 아니어도 살 거야?",
            trigger = "결제화면에 할인 · 세일 · %",
            mission = "'세일' 글자를 손으로 가리고, 원래 값이어도 살지 생각해보기",
            color = Color(0xFFF27405), // 주황 (임시)
        ),
        Guardian(
            id = "cart_cleanup", number = 4, type = GuardianType.IMPULSE,
            name = "장바구니 대청소",
            question = "담아만 둔 지 며칠째야?",
            trigger = "장바구니 화면",
            mission = "장바구니에서 일주일 넘게 담아둔 것 하나만 빼보기",
            color = Color(0xFF7B4BC4), // 보라 (임시)
        ),
        Guardian(
            id = "delivery_seal", number = 5, type = GuardianType.IMPULSE,
            name = "배달앱 봉인",
            question = "오늘도 시켜먹을래?",
            trigger = "배민 · 쿠팡이츠 · 요기요 실행",
            mission = "오늘 한 끼는 배달 말고 다른 방법으로 해결해보기",
            color = Color(0xFF2F6FDE), // 파랑 (임시)
        ),
        Guardian(
            id = "coffee_law", number = 6, type = GuardianType.IMPULSE,
            name = "커피 몇 잔의 법칙",
            question = "이거 커피 몇 잔이야?",
            trigger = "모든 결제 직전 금액 환산",
            mission = "결제 전에 금액을 커피 잔 수로 바꿔서 말해보기",
            color = Color(0xFF8B5A2B), // 갈색 (임시)
        ),
        // ── 누수 ──
        Guardian(
            id = "ghost_sub", number = 7, type = GuardianType.LEAK,
            name = "유령 구독 색출",
            question = "마지막에 쓴 게 언제야?",
            trigger = "구독 결제일 전날",
            mission = "구독 목록을 열고 한 달 넘게 안 쓴 것 찾아보기",
            color = Color(0xFF9B8FD8), // 연보라 (임시)
        ),
        Guardian(
            id = "auto_pay", number = 8, type = GuardianType.LEAK,
            name = "자동결제 잠복수사",
            question = "나도 모르게 빠져나가는 중",
            trigger = "같은 결제처 매달 반복",
            mission = "이번 달 카드 내역에서 매달 나가는 것에 표시해보기",
            color = Color(0xFF1F9E5A), // 초록 (임시)
        ),
        Guardian(
            id = "free_shipping", number = 9, type = GuardianType.LEAK,
            name = "무료배송의 배신",
            question = "배송비 아끼려 더 샀지?",
            trigger = "'무료배송까지' 문구",
            mission = "무료배송 맞추려고 담은 것, 빼도 괜찮은지 다시 보기",
            color = Color(0xFF12A7A0), // 청록 (임시)
        ),
        Guardian(
            id = "closet", number = 10, type = GuardianType.LEAK,
            name = "옷장 대방출",
            question = "작년에 산 거 입긴 했어?",
            trigger = "무신사 · 지그재그 · 29CM 실행",
            mission = "옷장에서 1년 동안 안 입은 옷 하나 꺼내보기",
            color = Color(0xFFE2558C), // 분홍 (임시)
        ),
        Guardian(
            id = "fridge", number = 11, type = GuardianType.LEAK,
            name = "냉장고 소탕",
            question = "냉장고에 또 사뒀지?",
            trigger = "컬리 등 장보기앱 · 저녁 배달시간",
            mission = "장보기 전에 냉장고 안을 사진 한 장 찍어보기",
            color = Color(0xFF3BA7E0), // 하늘 (임시)
        ),
        Guardian(
            id = "taxi", number = 12, type = GuardianType.LEAK,
            name = "만원짜리 5분",
            question = "택시비 5분 아끼는 값이야?",
            trigger = "카카오T 등 택시앱 실행",
            mission = "택시 부르기 전에 대중교통 도착 시간 한 번 비교해보기",
            color = Color(0xFFC9A400), // 겨자 (임시)
        ),
    )

    fun byId(id: String): Guardian? = all.firstOrNull { it.id == id }

    fun ofType(type: GuardianType): List<Guardian> = all.filter { it.type == type }

    // 오늘의 미션카드: 날짜마다 정해진 한 장 (하루 동안은 같은 카드)
    fun todays(date: LocalDate = LocalDate.now()): Guardian =
        all[(date.toEpochDay() % all.size).toInt()]
}
