package com.bsue.blob.market

import androidx.compose.ui.graphics.Color

// 지름마켓 상품 (전부 가상 브랜드). 상품 그림은 임시로 이모지 — 원화가 나오면 이미지로 교체.
data class MarketProduct(
    val id: String,
    val guardianId: String,          // 결제하기 → 이 수호신 출동
    val store: String,               // 가상 가게 이름
    val name: String,
    val emoji: String,
    val tileColor: Color,            // 상품 그림 배경색
    val price: Int,                  // 판매가 (상품 금액)
    val originalPrice: Int? = null,  // 정가 (할인 표시용)
    val badge: String? = null,       // 빨간 딱지 (예: 2+1, 70%)
    val rating: Double,
    val reviews: Int,
    val shippingFee: Int = 0,
    val shippingText: String,        // 예: 무료배송, 배달 35~45분
    val note: String? = null,        // 주황 안내 상자 (예: 장바구니에 담은 지 14일째)
    val cartItems: List<Pair<String, Int>> = emptyList(), // 장바구니형 상품의 구성
    val freeShippingLeft: Int? = null,                    // '무료배송까지 ○원' 남은 금액
) {
    val total: Int get() = price + shippingFee
    val discountPercent: Int?
        get() = originalPrice?.let { ((it - price) * 100f / it).toInt() }?.takeIf { it > 0 }
}

object MarketProducts {
    val all = listOf(
        MarketProduct(
            id = "drink", guardianId = "two_plus_one",
            store = "다모아편의점", name = "톡쏘는 탄산음료 500ml 2+1 (3병)", emoji = "🥤",
            tileColor = Color(0xFFDDF3FF),
            price = 3_600, originalPrice = 5_400, badge = "2+1",
            rating = 4.7, reviews = 2_841,
            shippingText = "편의점 픽업 · 지금 바로",
            note = "하나 더 담으면 하나 더 공짜! (진짜 필요해?)",
        ),
        MarketProduct(
            id = "sneakers", guardianId = "sale",
            store = "런웨이슈즈", name = "에어러너 데일리 러닝화", emoji = "👟",
            tileColor = Color(0xFFFFE4DC),
            price = 38_700, originalPrice = 129_000, badge = "70% 세일",
            rating = 4.8, reviews = 12_093,
            shippingText = "무료배송 · 내일 도착",
            note = "⏰ 세일 종료까지 2시간!",
        ),
        MarketProduct(
            id = "chicken", guardianId = "delivery_seal",
            store = "치킨나라", name = "야식 반반치킨 (후라이드+양념)", emoji = "🍗",
            tileColor = Color(0xFFFFF0CC),
            price = 20_000, badge = "야식 1위",
            rating = 4.9, reviews = 30_412,
            shippingFee = 3_000, shippingText = "배달 35~45분 · 배달팁 3,000원",
            note = "🌙 지금 주문 폭주 중",
        ),
        MarketProduct(
            id = "headphones", guardianId = "cart_cleanup",
            store = "소리공방", name = "노이즈캔슬링 무선 헤드폰 X3", emoji = "🎧",
            tileColor = Color(0xFFE6E1FF),
            price = 189_000, originalPrice = 219_000, badge = "장바구니 14일째",
            rating = 4.6, reviews = 5_220,
            shippingText = "무료배송 · 모레 도착",
            note = "🛒 장바구니에 담은 지 14일째예요",
        ),
        MarketProduct(
            id = "cart", guardianId = "free_shipping",
            store = "꽉찬마트", name = "장바구니 상품 3개", emoji = "📦",
            tileColor = Color(0xFFDFF5E8),
            price = 12_000, badge = "무료배송까지 3,000원",
            rating = 4.5, reviews = 987,
            shippingFee = 3_000, shippingText = "배송비 3,000원 (15,000원 이상 무료배송)",
            cartItems = listOf("키친타월 6롤" to 4_900, "세제 리필 1L" to 4_600, "그래놀라 300g" to 2_500),
            freeShippingLeft = 3_000,
        ),
        MarketProduct(
            id = "coffee_goods", guardianId = "coffee_law",
            store = "달빛커피", name = "가을 시즌 한정 텀블러 473ml", emoji = "☕",
            tileColor = Color(0xFFF3E6D8),
            price = 32_000, badge = "시즌 한정",
            rating = 4.8, reviews = 7_706,
            shippingText = "무료배송 · 오늘까지 한정 판매",
            note = "✨ 이번 시즌 지나면 다시 안 나와요",
        ),
    )
}
