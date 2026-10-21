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

package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View.MeasureSpec.EXACTLY
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.ScrollableChart
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.MidnightTimer
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.isRTL
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec
import java.util.Locale

data class HeaderState(
    val buttonCount: Int,
    val maxDataOffset: Int
)

class HeaderView(
    context: Context,
    val prefs: Preferences,
    val midnightTimer: MidnightTimer
) : ScrollableChart(context),
    Preferences.Listener,
    MidnightTimer.MidnightListener {

    private var drawer = Drawer()

    private var state: HeaderState? = null

    fun setState(newState: HeaderState) {
        state = newState
        setMaxDataOffset(newState.maxDataOffset)
        requestLayout()
    }

    init {
        setScrollerBucketSize(dim(R.dimen.checkmarkWidth).toInt())
        setBackgroundColor(sres.getColor(R.attr.headerBackgroundColor))
        elevation = dp(2.0f)
    }

    override fun atMidnight() {
        post { invalidate() }
    }

    override fun onCheckmarkSequenceChanged() {
        updateScrollDirection()
        postInvalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateScrollDirection()
        prefs.addListener(this)
        midnightTimer.addListener(this)
    }

    override fun onDetachedFromWindow() {
        midnightTimer.removeListener(this)
        prefs.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = dim(R.dimen.checkmarkHeight)
        setMeasuredDimension(widthMeasureSpec, height.toMeasureSpec(EXACTLY))
    }

    private fun updateScrollDirection() {
        var direction = -1
        if (prefs.isCheckmarkSequenceReversed) direction *= -1
        if (isRTL()) direction *= -1
        setScrollDirection(direction)
    }

    private inner class Drawer {
        private val rect = RectF()
        private val dateFormatter = JavaLocalDateFormatter(Locale.getDefault())
        private val paint = TextPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = dim(R.dimen.tinyTextSize)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            color = sres.getColor(R.attr.contrast60)
        }

        fun draw(canvas: Canvas) {
            val today = getToday()
            val width = dim(R.dimen.checkmarkWidth)
            val height = dim(R.dimen.checkmarkHeight)
            val isReversed = prefs.isCheckmarkSequenceReversed

            val em = paint.measureText("m")

            repeat(state?.buttonCount ?: 0) { index ->
                rect.set(0f, 0f, width, height)
                rect.offset(canvas.width.toFloat() - dp(3.0f), 0f)

                if (isReversed) {
                    rect.offset(-(index + 1) * width, 0f)
                } else {
                    rect.offset((index - (state?.buttonCount ?: 0)) * width, 0f)
                }

                if (isRTL()) {
                    rect.set(
                        canvas.width - rect.right,
                        rect.top,
                        canvas.width - rect.left,
                        rect.bottom
                    )
                }

                val date = today.minus(index + dataOffset)
                val dayOfWeek = dateFormatter.shortWeekdayName(date).uppercase()
                val dayOfMonth = date.day.toString()
                val y1 = rect.centerY() - 0.25 * em
                val y2 = rect.centerY() + 1.25 * em
                canvas.drawText(dayOfWeek, rect.centerX(), y1.toFloat(), paint)
                canvas.drawText(dayOfMonth, rect.centerX(), y2.toFloat(), paint)
            }
        }
    }
}
