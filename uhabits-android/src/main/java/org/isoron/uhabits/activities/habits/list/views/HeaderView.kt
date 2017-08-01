/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.*
import android.graphics.*
import android.os.Build.VERSION.*
import android.os.Build.VERSION_CODES.*
import android.text.*
import android.view.View.MeasureSpec.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.common.views.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.core.utils.DateUtils.*
import org.isoron.uhabits.utils.*
import java.util.*

class HeaderView(
        context: Context,
        val prefs: Preferences,
        val midnightTimer: MidnightTimer
) : ScrollableChart(context),
    Preferences.Listener,
    MidnightTimer.MidnightListener {

    private var drawer = Drawer()

    var buttonCount: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    init {
        setScrollerBucketSize(dim(R.dimen.checkmarkWidth).toInt())
        setBackgroundColor(sres.getColor(R.attr.headerBackgroundColor))
        if (SDK_INT >= LOLLIPOP) elevation = dp(2.0f)
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
        private val paint = TextPaint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = dim(R.dimen.tinyTextSize)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            color = sres.getColor(R.attr.mediumContrastTextColor)
        }

        fun draw(canvas: Canvas) {
            val day = getStartOfTodayCalendar()
            val width = dim(R.dimen.checkmarkWidth)
            val height = dim(R.dimen.checkmarkHeight)
            val isReversed = prefs.isCheckmarkSequenceReversed

            day.add(GregorianCalendar.DAY_OF_MONTH, -dataOffset)
            val em = paint.measureText("m")

            repeat(buttonCount) { index ->
                rect.set(0f, 0f, width, height)
                rect.offset(canvas.width.toFloat(), 0f)

                if (isReversed) rect.offset(-(index + 1) * width, 0f)
                else rect.offset((index - buttonCount) * width, 0f)

                if (isRTL()) rect.set(canvas.width - rect.right, rect.top,
                                      canvas.width - rect.left, rect.bottom)

                val y1 = rect.centerY() - 0.25 * em
                val y2 = rect.centerY() + 1.25 * em
                val lines = formatHeaderDate(day).toUpperCase().split("\n")
                canvas.drawText(lines[0], rect.centerX(), y1.toFloat(), paint)
                canvas.drawText(lines[1], rect.centerX(), y2.toFloat(), paint)
                day.add(GregorianCalendar.DAY_OF_MONTH, -1)
            }
        }
    }
}
