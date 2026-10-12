package com.bsue.blob.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// 폰 안에만 저장되는 작은 저장소 (서버 없음)
private val Context.blobDataStore: DataStore<Preferences> by preferencesDataStore(name = "blob")

// 화면들이 보는 현재 상태
data class BlobState(
    val unlockedIds: Set<String> = emptySet(), // 해금된 수호신 id
    val savedThisMonth: Int = 0,               // 이번 달 지킨 돈(원)
    val sortiesToday: Int = 0,                 // 오늘 블롭 출동 횟수
    val dailyLimit: Int = DEFAULT_DAILY_LIMIT, // 하루 출동 횟수 제한
    val restingToday: Boolean = false,         // "오늘은 쉬어" 켜짐
    val flippedToday: Boolean = false,         // 오늘의 미션카드 뒤집었는지
    val onboardingDone: Boolean = false,       // 첫 실행 안내를 다 봤는지
    val receipts: List<Receipt> = emptyList(), // 안 지른 영수증 (오래된 것부터)
    val cardPackages: Set<String> = CardApps.defaults.keys, // 결제 알림을 읽을 카드사 앱
    val subscriptions: List<Subscription> = emptyList(),     // 등록한 구독
    val payments: List<PaymentRecord> = emptyList(),         // 결제 알림 기록 (오래된 것부터)
    val dismissedAutoPay: Set<String> = emptySet(),          // "알고 있었어" 누른 잠복수사 후보
    val pendingReveal: List<String> = emptyList(),           // 새로 해금됐는데 아직 연출을 안 본 수호신
    val missionResultToday: String? = null,                  // 오늘 미션: "done"(해냈다) / "later"(내일 다시) / null
    val photocardShown: Set<String> = emptySet(),            // 포토카드 화면을 이미 띄운 세트 (IMPULSE / LEAK)
    val receiptsSeenAt: Long = 0L,                           // 영수증 탭에서 마지막으로 '출력'을 본 시각
    val exhibitionMode: Boolean = false,                     // 전시 모드 (가이드 · 60초 무조작 리셋 · 출동 제한 해제)
) {
    // 이 수호신으로 [참았다!]를 몇 번 했는지 (3번이면 해금)
    fun holdsFor(guardianId: String): Int = receipts.count { it.guardianId == guardianId }

    // 월 총 구독료
    val monthlySubscriptionTotal: Int get() = subscriptions.sumOf { it.monthlyAmount }

    companion object {
        const val DEFAULT_DAILY_LIMIT = 5
        const val MIN_DAILY_LIMIT = 1
        const val MAX_DAILY_LIMIT = 10
        const val HOLDS_TO_UNLOCK = 3
        const val MISSION_DONE = "done"
        const val MISSION_LATER = "later"
    }
}

class BlobRepository(context: Context) {

    private val store = context.applicationContext.blobDataStore

    private object Keys {
        val UNLOCKED = stringSetPreferencesKey("unlocked_ids")
        val SAVED_AMOUNT = intPreferencesKey("saved_amount")
        val SAVED_MONTH = stringPreferencesKey("saved_month")
        val SORTIE_COUNT = intPreferencesKey("sortie_count")
        val SORTIE_DATE = stringPreferencesKey("sortie_date")
        val DAILY_LIMIT = intPreferencesKey("daily_limit")
        val REST_DATE = stringPreferencesKey("rest_date")
        val FLIP_DATE = stringPreferencesKey("flip_date")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val RECEIPTS = stringPreferencesKey("receipts")
        val CARD_PACKAGES = stringSetPreferencesKey("card_packages")
        val SUBSCRIPTIONS = stringPreferencesKey("subscriptions")
        val PAYMENTS = stringPreferencesKey("payments")
        val DISMISSED_AUTOPAY = stringSetPreferencesKey("dismissed_autopay")
        const val MAX_PAYMENTS = 300
        val PENDING_REVEAL = stringPreferencesKey("pending_reveal") // "sale,taxi" 처럼 순서대로
        val MISSION_DATE = stringPreferencesKey("mission_date")
        val MISSION_RESULT = stringPreferencesKey("mission_result")
        val PHOTOCARD_SHOWN = stringSetPreferencesKey("photocard_shown")
        val RECEIPTS_SEEN_AT = longPreferencesKey("receipts_seen_at")
        val EXHIBITION_MODE = booleanPreferencesKey("exhibition_mode")
        const val MAX_RECEIPTS = 500
    }

    private fun today(): String = LocalDate.now().toString()
    private fun thisMonth(): String = YearMonth.now().toString()

    // 날짜·달이 바뀌면 오늘 기록/이번 달 기록은 자동으로 0부터 시작
    val state: Flow<BlobState> = store.data.map { p ->
        val today = today()
        BlobState(
            unlockedIds = p[Keys.UNLOCKED] ?: emptySet(),
            savedThisMonth = if (p[Keys.SAVED_MONTH] == thisMonth()) p[Keys.SAVED_AMOUNT] ?: 0 else 0,
            sortiesToday = if (p[Keys.SORTIE_DATE] == today) p[Keys.SORTIE_COUNT] ?: 0 else 0,
            dailyLimit = p[Keys.DAILY_LIMIT] ?: BlobState.DEFAULT_DAILY_LIMIT,
            restingToday = p[Keys.REST_DATE] == today,
            flippedToday = p[Keys.FLIP_DATE] == today,
            onboardingDone = p[Keys.ONBOARDING_DONE] ?: false,
            receipts = Receipt.decodeList(p[Keys.RECEIPTS]),
            cardPackages = p[Keys.CARD_PACKAGES] ?: CardApps.defaults.keys,
            subscriptions = Subscription.decodeList(p[Keys.SUBSCRIPTIONS]),
            payments = PaymentRecord.decodeList(p[Keys.PAYMENTS]),
            dismissedAutoPay = p[Keys.DISMISSED_AUTOPAY] ?: emptySet(),
            pendingReveal = p[Keys.PENDING_REVEAL]?.split(',')?.filter { it.isNotBlank() } ?: emptyList(),
            missionResultToday = if (p[Keys.MISSION_DATE] == today) p[Keys.MISSION_RESULT] else null,
            photocardShown = p[Keys.PHOTOCARD_SHOWN] ?: emptySet(),
            receiptsSeenAt = p[Keys.RECEIPTS_SEEN_AT] ?: 0L,
            exhibitionMode = p[Keys.EXHIBITION_MODE] ?: false,
        )
    }

    // ── 도감 해금 규칙 ──
    // ① 오늘의 미션 [해냈다]  ② 그 수호신으로 [참았다!] 3번  ③ 실물 카드 QR 스캔

    // 오늘의 미션카드 뒤집기 (뒤집기만으로는 해금 안 됨)
    suspend fun flipTodayCard() {
        store.edit { p -> p[Keys.FLIP_DATE] = today() }
    }

    // 바로 해금 (QR 스캔). 처음 해금이면 연출 대기열에 넣음
    suspend fun unlock(guardianId: String) {
        store.edit { p -> p.unlockWithReveal(guardianId) }
    }

    private fun MutablePreferences.unlockWithReveal(guardianId: String) {
        val unlocked = this[Keys.UNLOCKED] ?: emptySet()
        if (guardianId in unlocked) return
        this[Keys.UNLOCKED] = unlocked + guardianId
        val pending = this[Keys.PENDING_REVEAL]?.split(',')?.filter { it.isNotBlank() } ?: emptyList()
        this[Keys.PENDING_REVEAL] = (pending + guardianId).joinToString(",")
    }

    // 해금 연출을 다 봤으면 대기열에서 빼기
    suspend fun markRevealed(guardianId: String) {
        store.edit { p ->
            val pending = p[Keys.PENDING_REVEAL]?.split(',')?.filter { it.isNotBlank() && it != guardianId } ?: emptyList()
            p[Keys.PENDING_REVEAL] = pending.joinToString(",")
        }
    }

    // 오늘의 미션 결과: [해냈다] → 해금 / [내일 다시] → 기록만
    suspend fun setMissionResult(done: Boolean) {
        store.edit { p ->
            p[Keys.MISSION_DATE] = today()
            p[Keys.MISSION_RESULT] = if (done) BlobState.MISSION_DONE else BlobState.MISSION_LATER
            if (done) p.unlockWithReveal(Guardians.todays().id)
        }
    }

    suspend fun markPhotocardShown(set: String) {
        store.edit { p -> p[Keys.PHOTOCARD_SHOWN] = (p[Keys.PHOTOCARD_SHOWN] ?: emptySet()) + set }
    }

    // [참았다!] → 영수증 한 장 저장 + 이번 달 지킨 돈에 더하기
    suspend fun saveHeldReceipt(receipt: Receipt) {
        store.edit { p ->
            val list = Receipt.decodeList(p[Keys.RECEIPTS]) + receipt
            p[Keys.RECEIPTS] = Receipt.encodeList(list.takeLast(Keys.MAX_RECEIPTS))
            if (receipt.amount != null) p.addSavedAmount(receipt.amount)
            // 같은 수호신으로 3번 참았으면 해금
            if (list.count { it.guardianId == receipt.guardianId } >= BlobState.HOLDS_TO_UNLOCK) {
                p.unlockWithReveal(receipt.guardianId)
            }
        }
    }

    // 영수증 탭에서 새 항목 '출력' 애니메이션을 다 봤으면 그 시각까지 기록
    suspend fun markReceiptsSeen(until: Long) {
        store.edit { p -> p[Keys.RECEIPTS_SEEN_AT] = maxOf(until, p[Keys.RECEIPTS_SEEN_AT] ?: 0L) }
    }

    private fun MutablePreferences.addSavedAmount(amount: Int) {
        val month = thisMonth()
        val current = if (this[Keys.SAVED_MONTH] == month) this[Keys.SAVED_AMOUNT] ?: 0 else 0
        this[Keys.SAVED_MONTH] = month
        this[Keys.SAVED_AMOUNT] = current + amount
    }

    // 블롭 출동 시도: "오늘은 쉬어"가 꺼져 있고 하루 제한이 남았으면
    // 출동 횟수 +1 후 true. 아니면 아무것도 안 바꾸고 false.
    // 전시 모드에선 '오늘은 쉬어'·하루 제한 없이 항상 출동
    suspend fun tryDispatch(): Boolean {
        var dispatched = false
        store.edit { p ->
            val today = today()
            val exhibition = p[Keys.EXHIBITION_MODE] == true
            if (!exhibition && p[Keys.REST_DATE] == today) return@edit
            val count = if (p[Keys.SORTIE_DATE] == today) p[Keys.SORTIE_COUNT] ?: 0 else 0
            val limit = p[Keys.DAILY_LIMIT] ?: BlobState.DEFAULT_DAILY_LIMIT
            if (!exhibition && count >= limit) return@edit
            p[Keys.SORTIE_DATE] = today
            p[Keys.SORTIE_COUNT] = count + 1
            dispatched = true
        }
        return dispatched
    }

    suspend fun setDailyLimit(limit: Int) {
        store.edit { p ->
            p[Keys.DAILY_LIMIT] = limit.coerceIn(BlobState.MIN_DAILY_LIMIT, BlobState.MAX_DAILY_LIMIT)
        }
    }

    // "오늘은 쉬어" — 오늘 날짜로 저장하니까 내일이 되면 자동으로 꺼짐
    suspend fun setRestingToday(on: Boolean) {
        store.edit { p ->
            if (on) p[Keys.REST_DATE] = today() else p.remove(Keys.REST_DATE)
        }
    }

    // 결제 알림을 읽을 카드사 앱 더하기 / 빼기
    suspend fun addCardPackage(packageName: String) {
        val clean = packageName.trim()
        if (clean.isEmpty()) return
        store.edit { p -> p[Keys.CARD_PACKAGES] = (p[Keys.CARD_PACKAGES] ?: CardApps.defaults.keys) + clean }
    }

    suspend fun removeCardPackage(packageName: String) {
        store.edit { p -> p[Keys.CARD_PACKAGES] = (p[Keys.CARD_PACKAGES] ?: CardApps.defaults.keys) - packageName }
    }

    // ── 누수 점검: 구독 ──

    // 같은 id가 있으면 고치고, 없으면 새로 추가
    suspend fun saveSubscription(sub: Subscription) {
        store.edit { p ->
            val list = Subscription.decodeList(p[Keys.SUBSCRIPTIONS])
            val updated = if (list.any { it.id == sub.id }) list.map { if (it.id == sub.id) sub else it } else list + sub
            p[Keys.SUBSCRIPTIONS] = Subscription.encodeList(updated)
        }
    }

    suspend fun deleteSubscription(id: String) {
        store.edit { p ->
            p[Keys.SUBSCRIPTIONS] = Subscription.encodeList(Subscription.decodeList(p[Keys.SUBSCRIPTIONS]).filter { it.id != id })
        }
    }

    // [계속 쓸래] → 마지막 사용일을 오늘로
    suspend fun markSubscriptionUsedToday(id: String) {
        store.edit { p ->
            val today = LocalDate.now()
            p[Keys.SUBSCRIPTIONS] = Subscription.encodeList(
                Subscription.decodeList(p[Keys.SUBSCRIPTIONS]).map { if (it.id == id) it.copy(lastUsed = today) else it }
            )
        }
    }

    // ── 누수 점검: 결제 알림 기록 · 잠복수사 ──

    suspend fun recordPayment(record: PaymentRecord) {
        store.edit { p ->
            val list = PaymentRecord.decodeList(p[Keys.PAYMENTS]) + record
            p[Keys.PAYMENTS] = PaymentRecord.encodeList(list.takeLast(Keys.MAX_PAYMENTS))
        }
    }

    suspend fun clearPayments() {
        store.edit { p -> p.remove(Keys.PAYMENTS) }
    }

    suspend fun dismissAutoPay(key: String) {
        store.edit { p -> p[Keys.DISMISSED_AUTOPAY] = (p[Keys.DISMISSED_AUTOPAY] ?: emptySet()) + key }
    }

    suspend fun setExhibitionMode(on: Boolean) {
        store.edit { p -> p[Keys.EXHIBITION_MODE] = on }
    }

    // 전시용 데모 데이터: 샘플 구독 3개 + 잠복수사용 결제 기록 + 이번 달 영수증 4장
    // 넷플릭스는 '내일 결제·47일째 안 봄'이라 오늘 저녁 8시에 유령 구독 알림이 와요
    suspend fun loadDemoData() {
        val today = LocalDate.now()
        val demoSubs = listOf(
            Subscription(
                id = "demo_netflix", name = "넷플릭스", shortName = "넷플", monthlyAmount = 17_000,
                billingDay = today.plusDays(1).dayOfMonth, lastUsed = today.minusDays(47),
                usedVerb = "본", manageUrl = "https://www.netflix.com/cancelplan",
            ),
            Subscription(
                id = "demo_youtube", name = "유튜브 프리미엄", shortName = "유튜브 프리미엄", monthlyAmount = 14_900,
                billingDay = today.plusDays(12).dayOfMonth, lastUsed = today,
                usedVerb = "본", manageUrl = "https://www.youtube.com/paid_memberships",
            ),
            Subscription(
                id = "demo_coupang", name = "쿠팡 와우", shortName = "쿠팡 와우", monthlyAmount = 7_890,
                billingDay = today.plusDays(20).dayOfMonth, lastUsed = today.minusDays(3),
                usedVerb = "쓴", manageUrl = "https://www.coupang.com",
            ),
        )
        // 멜론이 석 달째 5일쯤 빠져나가는 중 (등록 안 된 구독 → 잠복수사에 걸림)
        fun at(monthsAgo: Long, day: Int): Long {
            val month = today.minusMonths(monthsAgo)
            val date = month.withDayOfMonth(day.coerceAtMost(month.lengthOfMonth()))
            return date.atTime(9, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val demoPayments = listOf(
            PaymentRecord(at(3, 5), 10_900, "멜론", "비상카드"),
            PaymentRecord(at(2, 6), 10_900, "멜론", "비상카드"),
            PaymentRecord(at(1, 5), 10_900, "멜론", "비상카드"),
            PaymentRecord(at(1, 12), 23_000, "치킨나라", "비상카드"),
            PaymentRecord(at(1, 20), 4_800, "달빛커피", "비상카드"),
        )
        // 이번 달 영수증 (서로 다른 수호신 → 데모만으로 해금되진 않음). 영수증 탭을 열면 드르륵 출력돼요
        val nowMillis = System.currentTimeMillis()
        val hour = 60 * 60 * 1000L
        val demoReceipts = listOf(
            Receipt(nowMillis - 50 * hour, 23_000, "delivery_seal", "배달의민족 · 치킨나라"),
            Receipt(nowMillis - 26 * hour, 38_700, "sale", "무신사 · 런웨이슈즈"),
            Receipt(nowMillis - 5 * hour, 12_900, "free_shipping", "쿠팡 · 꽉찬마트"),
            Receipt(nowMillis - 1 * hour, 4_800, "coffee_law", "달빛커피"),
        )
        store.edit { p ->
            val subs = Subscription.decodeList(p[Keys.SUBSCRIPTIONS]).filter { s -> demoSubs.none { it.id == s.id } }
            p[Keys.SUBSCRIPTIONS] = Subscription.encodeList(subs + demoSubs)
            val payments = (PaymentRecord.decodeList(p[Keys.PAYMENTS]) + demoPayments).sortedBy { it.createdAt }
            p[Keys.PAYMENTS] = PaymentRecord.encodeList(payments.takeLast(Keys.MAX_PAYMENTS))
            p.remove(Keys.DISMISSED_AUTOPAY)
            val receipts = (Receipt.decodeList(p[Keys.RECEIPTS]) + demoReceipts).sortedBy { it.createdAt }
            p[Keys.RECEIPTS] = Receipt.encodeList(receipts.takeLast(Keys.MAX_RECEIPTS))
            demoReceipts.forEach { r -> r.amount?.let { p.addSavedAmount(it) } }
        }
    }

    // 첫 실행 안내를 다 봤으면 true, 전시용 "안내 다시 보기"는 false
    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { p -> p[Keys.ONBOARDING_DONE] = done }
    }

    // ── 전시용 ──
    suspend fun unlockAll() {
        store.edit { p ->
            p[Keys.UNLOCKED] = Guardians.all.map { it.id }.toSet()
            p.remove(Keys.PENDING_REVEAL) // 12장 연출이 줄줄이 뜨지 않게
        }
    }

    // 관람객 교체용: 기록만 지우고 설정(출동 제한)은 그대로 둠
    suspend fun resetRecords() {
        store.edit { p ->
            p.remove(Keys.UNLOCKED)
            p.remove(Keys.SAVED_AMOUNT)
            p.remove(Keys.SAVED_MONTH)
            p.remove(Keys.SORTIE_COUNT)
            p.remove(Keys.SORTIE_DATE)
            p.remove(Keys.FLIP_DATE)
            p.remove(Keys.REST_DATE)
            p.remove(Keys.RECEIPTS)
            p.remove(Keys.SUBSCRIPTIONS)
            p.remove(Keys.PAYMENTS)
            p.remove(Keys.DISMISSED_AUTOPAY)
            p.remove(Keys.PENDING_REVEAL)
            p.remove(Keys.MISSION_DATE)
            p.remove(Keys.MISSION_RESULT)
            p.remove(Keys.PHOTOCARD_SHOWN)
            p.remove(Keys.RECEIPTS_SEEN_AT)
        }
    }
}
