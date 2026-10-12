package com.bsue.blob.ui.blob

import androidx.annotation.DrawableRes
import com.bsue.blob.R

// ════════════════════════════════════════════════════════════════
//  블롭 그림 ↔ 앱 상태, 한 곳에서 관리하는 파일
//  그림을 바꿀 땐 res/drawable-nodpi 의 같은 이름 파일만 교체하면 되고,
//  "어느 화면에 어느 그림"을 바꿀 땐 아래 BlobLooks.of() 표만 고치면 돼요.
// ════════════════════════════════════════════════════════════════

// 감정 포즈 5종 (res/drawable-nodpi/pose_*.webp)
enum class BlobPose(@DrawableRes val image: Int, val label: String) {
    IDLE(R.drawable.pose_idle, "기본"),
    PEEK(R.drawable.pose_peek, "빼꼼"),   // 그림은 '왼쪽에서 오른쪽으로' 엿보는 방향
    STOP(R.drawable.pose_stop, "잠깐!"),
    CHEER(R.drawable.pose_cheer, "환호"),
    PAT(R.drawable.pose_pat, "괜찮아"),
}

// 움직임 7종 (BlobCharacter 가 그려요)
enum class BlobMotion(val label: String) {
    NONE("정지"),
    BREATHE("숨쉬기 + 가끔 통통"),
    SNEAK("살금살금 (빼꼼)"),
    STOP_ENTER("등장 squash & stretch"),
    COUNT_PRESS("숫자마다 눌렸다 펴짐"),
    CHEER_JUMP("점프 2번 + 반짝"),
    PAT_SWAY("좌우로 천천히 기울기"),
}

// 화면에 보여줄 블롭 한 컷 = 그림 + 움직임 + 좌우 반전
data class BlobLook(
    @DrawableRes val image: Int,
    val motion: BlobMotion,
    val mirrored: Boolean = false,
)

// 블롭이 나오는 자리
enum class BlobScene(val label: String) {
    FLOATING("플로팅 버블 (평소)"),
    FLOATING_RESTING("플로팅 버블 (오늘은 쉬어)"),
    PEEK("빼꼼 (쇼핑·배달앱 열 때)"),
    FAREWELL("배웅 (결제 알림)"),
    HOME("홈 탐정 사무실"),
    ONBOARDING("첫 안내 · 전시 가이드"),
    DISPATCH("출동 등장 + 3초 카운트"),
    HELD("[참았다!]"),
    BOUGHT("[그래도 살래] 못 참아도 괜찮아"),
    UNLOCK("수호신 해금 · 블롭 소환 · 포토카드"),
    RECEIPT("영수증 프린터 (대기)"),
    RECEIPT_PRINTING("영수증 프린터 (출력 중)"),
}

object BlobLooks {

    // ★ 상태 → 그림·움직임 표 (여기만 고치면 앱 전체가 바뀌어요)
    fun of(scene: BlobScene, fromRight: Boolean = false): BlobLook = when (scene) {
        BlobScene.FLOATING -> BlobLook(BlobPose.IDLE.image, BlobMotion.BREATHE)
        BlobScene.FLOATING_RESTING -> BlobLook(BlobPose.IDLE.image, BlobMotion.NONE)
        // 왼쪽 벽에서 나오면 그대로, 오른쪽 벽에서 나오면 좌우 반전
        BlobScene.PEEK -> BlobLook(BlobPose.PEEK.image, BlobMotion.SNEAK, mirrored = fromRight)
        BlobScene.FAREWELL -> BlobLook(BlobPose.PAT.image, BlobMotion.PAT_SWAY)
        BlobScene.HOME -> BlobLook(BlobPose.IDLE.image, BlobMotion.BREATHE)
        BlobScene.ONBOARDING -> BlobLook(BlobPose.IDLE.image, BlobMotion.BREATHE)
        BlobScene.DISPATCH -> BlobLook(BlobPose.STOP.image, BlobMotion.COUNT_PRESS)
        BlobScene.HELD -> BlobLook(BlobPose.CHEER.image, BlobMotion.CHEER_JUMP)
        BlobScene.BOUGHT -> BlobLook(BlobPose.PAT.image, BlobMotion.PAT_SWAY)
        BlobScene.UNLOCK -> BlobLook(BlobPose.CHEER.image, BlobMotion.CHEER_JUMP)
        BlobScene.RECEIPT -> BlobLook(BlobPose.IDLE.image, BlobMotion.BREATHE)
        BlobScene.RECEIPT_PRINTING -> BlobLook(BlobPose.CHEER.image, BlobMotion.CHEER_JUMP)
    }

    // 수호신 → 미션 포즈 (도감 카드 · 출동 화면 작은 카드 · 미션 알림)
    @DrawableRes
    fun missionArt(guardianId: String): Int = when (guardianId) {
        "zero_day" -> R.drawable.blob_zero_day
        "two_plus_one" -> R.drawable.blob_two_plus_one
        "sale" -> R.drawable.blob_sale
        "cart_cleanup" -> R.drawable.blob_cart
        "delivery_seal" -> R.drawable.blob_delivery_lock
        "coffee_law" -> R.drawable.blob_coffee
        "ghost_sub" -> R.drawable.blob_ghost_sub
        "auto_pay" -> R.drawable.blob_autopay
        "free_shipping" -> R.drawable.blob_free_shipping
        "closet" -> R.drawable.blob_closet
        "fridge" -> R.drawable.blob_fridge
        "taxi" -> R.drawable.blob_walk
        else -> BlobPose.IDLE.image
    }

    // 빼꼼 말풍선 속 작은 수호신 그림 (배달앱 봉인만 빼꼼 전용 그림이 따로 있어요)
    @DrawableRes
    fun missionPeekArt(guardianId: String): Int =
        if (guardianId == "delivery_seal") R.drawable.blob_delivery_lock_peek else missionArt(guardianId)

    // 테스트 화면용: 18장 전부 (이름 → 그림)
    val allImages: List<Pair<String, Int>> = listOf(
        "pose_idle" to R.drawable.pose_idle,
        "pose_peek" to R.drawable.pose_peek,
        "pose_stop" to R.drawable.pose_stop,
        "pose_cheer" to R.drawable.pose_cheer,
        "pose_pat" to R.drawable.pose_pat,
        "blob_zero_day" to R.drawable.blob_zero_day,
        "blob_two_plus_one" to R.drawable.blob_two_plus_one,
        "blob_sale" to R.drawable.blob_sale,
        "blob_cart" to R.drawable.blob_cart,
        "blob_delivery_lock" to R.drawable.blob_delivery_lock,
        "blob_delivery_lock_peek" to R.drawable.blob_delivery_lock_peek,
        "blob_coffee" to R.drawable.blob_coffee,
        "blob_ghost_sub" to R.drawable.blob_ghost_sub,
        "blob_autopay" to R.drawable.blob_autopay,
        "blob_free_shipping" to R.drawable.blob_free_shipping,
        "blob_closet" to R.drawable.blob_closet,
        "blob_fridge" to R.drawable.blob_fridge,
        "blob_walk" to R.drawable.blob_walk,
    )

    fun imageName(@DrawableRes image: Int): String = allImages.firstOrNull { it.second == image }?.first ?: "?"

    // 그림 비율 (1100 x 1020) — 칸 크기 정할 때 사용
    const val ASPECT = 1100f / 1020f
}
