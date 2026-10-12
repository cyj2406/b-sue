package com.bsue.blob.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

// 떠 있는 블롭의 손가락 처리: 드래그 / 톡 / 길게 누르기를 구분해요.
// 창 자체가 손가락을 따라 움직이기 때문에, 화면 전체 기준 좌표(rawX, rawY)로 계산해요.
class BubbleTouchLayout(context: Context) : FrameLayout(context) {

    interface Listener {
        fun onDragStart()
        fun onDragBy(dx: Float, dy: Float)
        fun onDragEnd()
        fun onTap()
        fun onLongPress()
    }

    var listener: Listener? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false
    private var longPressed = false

    private val longPressRunnable = Runnable {
        if (!dragging) {
            longPressed = true
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            listener?.onLongPress()
        }
    }

    // 안쪽 Compose 화면 대신 여기서 모든 터치를 받음
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
                lastX = ev.rawX
                lastY = ev.rawY
                dragging = false
                longPressed = false
                postDelayed(longPressRunnable, longPressTimeout)
            }

            MotionEvent.ACTION_MOVE -> {
                val movedFar = abs(ev.rawX - downX) > touchSlop || abs(ev.rawY - downY) > touchSlop
                if (!dragging && !longPressed && movedFar) {
                    dragging = true
                    removeCallbacks(longPressRunnable)
                    listener?.onDragStart()
                }
                if (dragging) {
                    listener?.onDragBy(ev.rawX - lastX, ev.rawY - lastY)
                    lastX = ev.rawX
                    lastY = ev.rawY
                }
            }

            MotionEvent.ACTION_UP -> {
                removeCallbacks(longPressRunnable)
                when {
                    dragging -> listener?.onDragEnd()
                    !longPressed -> {
                        performClick()
                        listener?.onTap()
                    }
                }
                dragging = false
            }

            MotionEvent.ACTION_CANCEL -> {
                removeCallbacks(longPressRunnable)
                if (dragging) listener?.onDragEnd()
                dragging = false
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
