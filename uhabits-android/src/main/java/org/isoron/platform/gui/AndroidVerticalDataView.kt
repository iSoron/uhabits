/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.platform.gui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Scroller
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import kotlin.math.abs
import kotlin.math.max

/**
 * An AndroidView that implements vertical scrolling.
 */
class AndroidVerticalDataView(
    context: Context,
    attrs: AttributeSet? = null
) : AndroidView<DataView>(context, attrs),
    GestureDetector.OnGestureListener,
    ValueAnimator.AnimatorUpdateListener {

    private val detector = GestureDetector(context, this)
    private val scroller = Scroller(context, null, true)
    private val scrollAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener(this@AndroidVerticalDataView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == ACTION_DOWN || event.actionMasked == ACTION_MOVE) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return detector.onTouchEvent(event)
    }
    override fun onDown(e: MotionEvent) = true
    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return handleClick(e, true)
    }

    override fun onLongPress(e: MotionEvent) {
        handleClick(e)
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        dx: Float,
        dy: Float
    ): Boolean {
        if (abs(dy) > abs(dx)) {
            val parent = parent
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        scroller.startScroll(
            scroller.currX,
            scroller.currY,
            0,
            dy.toInt(),
            0
        )
        scroller.computeScrollOffset()
        updateDataOffset()
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        scroller.fling(
            scroller.currX,
            scroller.currY,
            0,
            -velocityY.toInt() / 2,
            0,
            0,
            0,
            Integer.MAX_VALUE
        )
        invalidate()
        scrollAnimator.duration = scroller.duration.toLong()
        scrollAnimator.start()
        return false
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (!scroller.isFinished) {
            scroller.computeScrollOffset()
            updateDataOffset()
        } else {
            scrollAnimator.cancel()
        }
    }

    fun resetDataOffset() {
        scroller.finalY = 0
        scroller.computeScrollOffset()
        updateDataOffset()
    }

    private fun updateDataOffset() {
        view?.let { v ->
            var newDataOffset: Int =
                scroller.currY / (v.dataColumnWidth * canvas.innerDensity).toInt()
            newDataOffset = max(0, newDataOffset)
            if (newDataOffset != v.dataOffset) {
                v.dataOffset = newDataOffset
                postInvalidate()
            }
        }
    }

    private fun handleClick(e: MotionEvent, isSingleTap: Boolean = false): Boolean {
        val x: Float
        val y: Float
        try {
            val pointerId = e.getPointerId(0)
            x = e.getX(pointerId)
            y = e.getY(pointerId)
        } catch (ex: RuntimeException) {
            return false
        }
        if (isSingleTap) {
            view?.onClick(x / canvas.innerDensity, y / canvas.innerDensity)
        } else {
            view?.onLongClick(x / canvas.innerDensity, y / canvas.innerDensity)
        }
        return true
    }
}
