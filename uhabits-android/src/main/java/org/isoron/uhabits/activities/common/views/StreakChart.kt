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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Streak
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.StyledResources
import java.text.DateFormat
import java.util.LinkedList
import java.util.Random
import java.util.TimeZone
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class StreakChart : View {
    private var paint: Paint? = null
    private var minLength: Long = 0
    private var maxLength: Long = 0
    private lateinit var colors: IntArray
    private lateinit var textColors: IntArray
    private var rect: RectF? = null
    private var baseSize = 0
    private var primaryColor = 0
    private var streaks: List<Streak>? = null
    private var isBackgroundTransparent = false
    private var dateFormat: DateFormat? = null
    private var internalWidth = 0
    private var em = 0f
    private var maxLabelWidth = 0f
    private var textMargin = 0f
    private var shouldShowLabels = false
    private val textColor = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * Returns the maximum number of streaks this view is able to show, given
     * its current size.
     *
     * @return max number of visible streaks
     */
    val maxStreakCount: Int
        get() = floor((measuredHeight / baseSize).toDouble()).toInt()

    fun populateWithRandomData() {
        var start: Timestamp = getToday()
        val streaks: MutableList<Streak> = LinkedList()
        for (i in 0..9) {
            val length = Random().nextInt(100)
            val end = start.plus(length)
            streaks.add(Streak(start, end))
            start = end.plus(1)
        }
        setStreaks(streaks)
    }

    fun setColor(color: Int) {
        primaryColor = color
        postInvalidate()
    }

    fun setIsBackgroundTransparent(isBackgroundTransparent: Boolean) {
        this.isBackgroundTransparent = isBackgroundTransparent
        initColors()
    }

    fun setStreaks(streaks: List<Streak>?) {
        this.streaks = streaks
        initColors()
        updateMaxMinLengths()
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (streaks!!.isEmpty()) return
        rect!![0f, 0f, internalWidth.toFloat()] = baseSize.toFloat()
        for (s in streaks!!) {
            drawRow(canvas, s, rect)
            rect!!.offset(0f, baseSize.toFloat())
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthSpec = widthSpec
        var heightSpec = heightSpec
        val params = layoutParams
        if (params != null && params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            val width = MeasureSpec.getSize(widthSpec)
            val height = streaks!!.size * baseSize
            heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        }
        setMeasuredDimension(widthSpec, heightSpec)
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        this.internalWidth = width
        val context = context
        val minTextSize = getDimension(context, R.dimen.tinyTextSize)
        val maxTextSize = getDimension(context, R.dimen.regularTextSize)
        val textSize = baseSize * 0.5f
        paint!!.textSize = max(min(textSize, maxTextSize), minTextSize)
        em = paint!!.fontSpacing
        textMargin = 0.5f * em
        updateMaxMinLengths()
    }

    private fun drawRow(canvas: Canvas, streak: Streak, rect: RectF?) {
        if (maxLength == 0L) return
        val percentage = streak.length.toFloat() / maxLength
        var availableWidth = internalWidth - 2 * maxLabelWidth
        if (shouldShowLabels) availableWidth -= 2 * textMargin
        var barWidth = percentage * availableWidth
        val minBarWidth = paint!!.measureText(streak.length.toLong().toString()) + em
        barWidth = max(barWidth, minBarWidth)
        val gap = (internalWidth - barWidth) / 2
        val paddingTopBottom = baseSize * 0.05f
        paint!!.color = percentageToColor(percentage)
        val round = dpToPixels(context, 2f)
        canvas.drawRoundRect(
            rect!!.left + gap,
            rect.top + paddingTopBottom,
            rect.right - gap,
            rect.bottom - paddingTopBottom,
            round,
            round,
            paint!!
        )
        val yOffset = rect.centerY() + 0.3f * em
        paint!!.color = percentageToTextColor(percentage)
        paint!!.textAlign = Paint.Align.CENTER
        canvas.drawText(
            streak.length.toLong().toString(),
            rect.centerX(),
            yOffset,
            paint!!
        )
        if (shouldShowLabels) {
            val startLabel = dateFormat!!.format(streak.start.toJavaDate())
            val endLabel = dateFormat!!.format(streak.end.toJavaDate())
            paint!!.color = textColors[1]
            paint!!.textAlign = Paint.Align.RIGHT
            canvas.drawText(startLabel, gap - textMargin, yOffset, paint!!)
            paint!!.textAlign = Paint.Align.LEFT
            canvas.drawText(endLabel, internalWidth - gap + textMargin, yOffset, paint!!)
        }
    }

    private fun init() {
        initPaints()
        initColors()
        streaks = emptyList()
        val newDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        if (!isInEditMode) newDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        dateFormat = newDateFormat
        rect = RectF()
        baseSize = resources.getDimensionPixelSize(R.dimen.baseSize)
    }

    private fun initColors() {
        val red = Color.red(primaryColor)
        val green = Color.green(primaryColor)
        val blue = Color.blue(primaryColor)
        val res = StyledResources(context)
        colors = IntArray(4)
        colors[3] = primaryColor
        colors[2] = Color.argb(192, red, green, blue)
        colors[1] = Color.argb(96, red, green, blue)
        colors[0] = res.getColor(R.attr.contrast20)
        textColors = IntArray(3)
        textColors[2] = res.getColor(R.attr.contrast0)
        textColors[1] = res.getColor(R.attr.contrast60)
        textColors[0] = res.getColor(R.attr.contrast80)
    }

    private fun initPaints() {
        paint = Paint()
        paint!!.textAlign = Paint.Align.CENTER
        paint!!.isAntiAlias = true
    }

    private fun percentageToColor(percentage: Float): Int {
        if (percentage >= 1.0f) return colors[3]
        if (percentage >= 0.8f) return colors[2]
        return if (percentage >= 0.5f) colors[1] else colors[0]
    }

    private fun percentageToTextColor(percentage: Float): Int {
        return if (percentage >= 0.5f) textColors[2] else textColors[1]
    }

    private fun updateMaxMinLengths() {
        maxLength = 0
        minLength = Long.MAX_VALUE
        shouldShowLabels = true
        for (s in streaks!!) {
            maxLength = max(maxLength, s.length.toLong())
            minLength = min(minLength, s.length.toLong())
            val lw1 = paint!!.measureText(dateFormat!!.format(s.start.toJavaDate()))
            val lw2 = paint!!.measureText(dateFormat!!.format(s.end.toJavaDate()))
            maxLabelWidth = max(maxLabelWidth, max(lw1, lw2))
        }
        if (internalWidth - 2 * maxLabelWidth < internalWidth * 0.25f) {
            maxLabelWidth = 0f
            shouldShowLabels = false
        }
    }
}
