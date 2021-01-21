/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.activities.common.views

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class ScrollableChart : View, GestureDetector.OnGestureListener, AnimatorUpdateListener {
    var dataOffset = 0
        private set
    private var scrollerBucketSize = 1
    private var direction = 1
    private lateinit var detector: GestureDetector
    private lateinit var scroller: Scroller
    private lateinit var scrollAnimator: ValueAnimator
    private lateinit var scrollController: ScrollController
    private var maxDataOffset = 10000

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        if (!scroller.isFinished) {
            scroller.computeScrollOffset()
            updateDataOffset()
        } else {
            scrollAnimator.cancel()
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        scroller.fling(
            scroller.currX,
            scroller.currY,
            direction * velocityX.toInt() / 2,
            0,
            0,
            maxX,
            0,
            0
        )
        invalidate()
        scrollAnimator.duration = scroller.duration.toLong()
        scrollAnimator.start()
        return false
    }

    private val maxX: Int
        get() = maxDataOffset * scrollerBucketSize

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is BundleSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val x = state.bundle!!.getInt("x")
        val y = state.bundle.getInt("y")
        direction = state.bundle.getInt("direction")
        dataOffset = state.bundle.getInt("dataOffset")
        maxDataOffset = state.bundle.getInt("maxDataOffset")
        scroller.startScroll(0, 0, x, y, 0)
        scroller.computeScrollOffset()
        super.onRestoreInstanceState(state.superState)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putInt("x", scroller.currX)
            putInt("y", scroller.currY)
            putInt("dataOffset", dataOffset)
            putInt("direction", direction)
            putInt("maxDataOffset", maxDataOffset)
        }
        return BundleSavedState(superState, bundle)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, dx: Float, dy: Float): Boolean {
        var dx = dx
        if (scrollerBucketSize == 0) return false
        if (abs(dx) > abs(dy)) {
            val parent = parent
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        dx *= -direction
        dx = min(dx, (maxX - scroller.currX).toFloat())
        scroller.startScroll(
            scroller.currX,
            scroller.currY,
            dx.toInt(),
            dy.toInt(),
            0
        )
        scroller.computeScrollOffset()
        updateDataOffset()
        return true
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }

    fun setScrollDirection(direction: Int) {
        require(!(direction != 1 && direction != -1))
        this.direction = direction
    }

    override fun onLongPress(e: MotionEvent) {}
    fun setMaxDataOffset(maxDataOffset: Int) {
        this.maxDataOffset = maxDataOffset
        dataOffset = min(dataOffset, maxDataOffset)
        scrollController.onDataOffsetChanged(dataOffset)
        postInvalidate()
    }

    fun setScrollController(scrollController: ScrollController) {
        this.scrollController = scrollController
    }

    fun setScrollerBucketSize(scrollerBucketSize: Int) {
        this.scrollerBucketSize = scrollerBucketSize
    }

    private fun init(context: Context?) {
        detector = GestureDetector(context, this)
        scroller = Scroller(context, null, true)
        val newScrollAnimator = ValueAnimator.ofFloat(0f, 1f)
        newScrollAnimator.addUpdateListener(this)
        scrollAnimator = newScrollAnimator
        scrollController = object : ScrollController {}
    }

    fun reset() {
        scroller.finalX = 0
        scroller.computeScrollOffset()
        updateDataOffset()
    }

    private fun updateDataOffset() {
        var newDataOffset = scroller.currX / scrollerBucketSize
        newDataOffset = max(0, newDataOffset)
        newDataOffset = min(maxDataOffset, newDataOffset)
        if (newDataOffset != dataOffset) {
            dataOffset = newDataOffset
            scrollController.onDataOffsetChanged(dataOffset)
            postInvalidate()
        }
    }

    interface ScrollController {
        fun onDataOffsetChanged(newDataOffset: Int) {}
    }
}
