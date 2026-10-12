package com.bsue.blob.notify

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.bsue.blob.data.BlobRepository
import com.bsue.blob.data.CardApps
import com.bsue.blob.data.PaymentRecord
import com.bsue.blob.overlay.BlobOverlayService
import com.bsue.blob.overlay.OverlayBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 알림 읽기 — 카드 승인 알림이 오면 블롭이 '배웅'
// 설정에 등록된 카드사 앱 + 지름마켓의 가짜 결제 알림만 봐요.
// 결제 알림의 금액·가게·날짜만 폰 안에 기록해요 ('자동결제 잠복수사'용). 폰 밖으로는 안 보내요.
class PaymentListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repo by lazy { BlobRepository(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        val pkg = sbn.packageName

        // 우리 앱이 보낸 가짜 결제 알림
        if (pkg == packageName) {
            if (!extras.getBoolean(FakePaymentNotifier.EXTRA_FAKE)) return
            PaymentParser.parse(title, text, "비상카드")?.let { farewell(it); record(it) }
            return
        }

        scope.launch {
            val cardPackages = repo.state.first().cardPackages
            val notice = PaymentParser.parse(title, text, CardApps.label(pkg))
            when {
                pkg in cardPackages -> notice?.let { farewell(it); record(it) }
                // 목록에 없는 앱인데 '승인' 알림처럼 생겼으면 → 설정에서 '추가할까?'로 보여줌
                notice != null && "승인" in "$title$text" -> OverlayBus.recordPaymentLikePackage(pkg)
            }
        }
    }

    // 잠복수사용 결제 기록 (가게 이름을 못 읽은 알림은 반복 여부를 알 수 없어서 건너뜀)
    private fun record(notice: PaymentNotice) {
        val amount = notice.amount ?: return
        val merchant = notice.merchant ?: return
        scope.launch {
            repo.recordPayment(PaymentRecord(System.currentTimeMillis(), amount, merchant, notice.sourceLabel))
        }
    }

    // 배웅: 앱을 보고 있으면 앱 안에서, 아니면 떠 있는 블롭이
    private fun farewell(notice: PaymentNotice) {
        if (OverlayBus.appInForeground.value) {
            OverlayBus.farewell.tryEmit(notice)
        } else {
            BlobOverlayService.farewell(this, notice)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

// 알림 읽기 권한이 켜져 있는지
fun isPaymentListenerOn(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
