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
package org.isoron.uhabits.activities.habits.overview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.ScrollableChart
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.StyledResources
import org.isoron.uhabits.utils.toSimpleDataFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

class AggregateScoreChart : ScrollableChart {
    private var pGrid: Paint? = null
    private var pText: Paint? = null
    private var pGraph: Paint? = null
    private var em = 0f
    private var baseSize = 0
    private var columnWidth = 0f
    private var columnHeight = 0
    private var nColumns = 0
    private var textColor = 0
    private var gridColor = 0
    private var primaryColor = 0
    private var scores: List<Score>? = null
    private var dfMonth: SimpleDateFormat? = null
    private var dfDay: SimpleDateFormat? = null
    private var dfYear: SimpleDateFormat? = null
    private var internalPaddingTop = 0
    private var previousYearText: String? = null
    private var previousMonthText: String? = null
    private var skipYear = 0

    // Dynamic Y-axis scaling
    private var minScore = 0.0
    private var maxScore = 1.0
    private val yAxisBuffer = 0.0025 // 0.25% buffer

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun setScores(scores: List<Score>) {
        this.scores = scores
        calculateYAxisRange()
        postInvalidate()
    }

    private fun calculateYAxisRange() {
        if (scores == null || scores!!.isEmpty()) {
            minScore = 0.0
            maxScore = 1.0
            return
        }

        var min = Double.MAX_VALUE
        var max = Double.MIN_VALUE

        for (score in scores!!) {
            if (score.value < min) min = score.value
            if (score.value > max) max = score.value
        }

        // Add buffer
        val range = max - min
        if (range < 0.01) {
            // If range is very small, use fixed buffer
            minScore = max(0.0, min - yAxisBuffer)
            maxScore = min(1.0, max + yAxisBuffer)
        } else {
            minScore = max(0.0, min - range * 0.1)
            maxScore = min(1.0, max + range * 0.1)
        }

        // Ensure min != max
        if (minScore == maxScore) {
            minScore = max(0.0, minScore - 0.05)
            maxScore = min(1.0, maxScore + 0.05)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (scores == null || scores!!.isEmpty()) return

        val rect = RectF(0f, 0f, nColumns * columnWidth, columnHeight.toFloat())
        rect.offset(0f, internalPaddingTop.toFloat())

        drawGrid(canvas, rect)
        drawLineGraph(canvas, rect)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        var height = height
        if (height < 9) height = 200

        val maxTextSize = InterfaceUtils.getDimension(context, R.dimen.tinyTextSize)
        val textSize = height * 0.06f
        pText!!.textSize = min(textSize, maxTextSize)
        em = pText!!.fontSpacing

        val footerHeight = (3 * em).toInt()
        internalPaddingTop = em.toInt()
        baseSize = (height - footerHeight - internalPaddingTop) / 8
        columnWidth = baseSize.toFloat()
        columnWidth = max(columnWidth, maxDayWidth * 1.5f)
        columnWidth = max(columnWidth, maxMonthWidth * 1.2f)
        nColumns = (width / columnWidth).toInt()
        columnWidth = width.toFloat() / nColumns
        setScrollerBucketSize(columnWidth.toInt())
        columnHeight = 8 * baseSize

        val minStrokeWidth = InterfaceUtils.dpToPixels(context, 1f)
        pGraph!!.strokeWidth = max(minStrokeWidth, baseSize * 0.1f)
        pGrid!!.strokeWidth = min(minStrokeWidth, baseSize * 0.05f)
    }

    private fun drawGrid(canvas: Canvas, rGrid: RectF) {
        val nRows = 5
        val rowHeight = rGrid.height() / nRows

        pText!!.textAlign = Paint.Align.LEFT
        pText!!.color = textColor
        pGrid!!.color = gridColor

        val tempRect = RectF(rGrid)
        for (i in 0 until nRows) {
            val percentage = maxScore - i * (maxScore - minScore) / nRows
            canvas.drawText(
                String.format("%.1f%%", percentage * 100),
                tempRect.left + 0.5f * em,
                tempRect.top + 1f * em,
                pText!!
            )
            canvas.drawLine(
                tempRect.left,
                tempRect.top,
                tempRect.right,
                tempRect.top,
                pGrid!!
            )
            tempRect.offset(0f, rowHeight)
        }
        canvas.drawLine(tempRect.left, tempRect.top, tempRect.right, tempRect.top, pGrid!!)
    }

    private fun drawLineGraph(canvas: Canvas, rect: RectF) {
        if (scores == null || scores!!.size < 2) return

        pGraph!!.color = primaryColor
        pGraph!!.style = Paint.Style.STROKE

        previousMonthText = ""
        previousYearText = ""
        skipYear = 0

        var prevX = 0f
        var prevY = 0f
        var isFirst = true

        for (k in 0 until min(nColumns, scores!!.size)) {
            val offset = nColumns - k - 1 + dataOffset
            if (offset >= scores!!.size) continue

            val score = scores!![offset]
            val timestamp = score.timestamp

            // Normalize score value to Y position
            val normalizedValue = if (maxScore > minScore) {
                (score.value - minScore) / (maxScore - minScore)
            } else {
                0.5
            }
            val y = rect.top + rect.height() * (1 - normalizedValue.toFloat())
            val x = rect.left + k * columnWidth + columnWidth / 2

            // Draw line segment
            if (!isFirst) {
                canvas.drawLine(prevX, prevY, x, y, pGraph!!)
            }

            // Draw marker point
            val markerRadius = baseSize * 0.15f
            pGraph!!.style = Paint.Style.FILL
            canvas.drawCircle(x, y, markerRadius, pGraph!!)
            pGraph!!.style = Paint.Style.STROKE

            // Draw footer
            val footerRect = RectF(
                k * columnWidth,
                rect.bottom,
                (k + 1) * columnWidth,
                rect.bottom + 3 * em
            )
            drawFooter(canvas, footerRect, timestamp)

            prevX = x
            prevY = y
            isFirst = false
        }
    }

    private fun drawFooter(canvas: Canvas, rect: RectF, currentDate: Timestamp) {
        val yearText = dfYear!!.format(currentDate.toJavaDate())
        val monthText = dfMonth!!.format(currentDate.toJavaDate())
        val dayText = dfDay!!.format(currentDate.toJavaDate())
        val calendar = currentDate.toCalendar()
        val year = calendar[Calendar.YEAR]

        var shouldPrintYear = true
        if (yearText == previousYearText) shouldPrintYear = false
        if (year % 2 != 0) shouldPrintYear = false
        if (skipYear > 0) {
            skipYear--
            shouldPrintYear = false
        }

        if (shouldPrintYear) {
            previousYearText = yearText
            previousMonthText = ""
            pText!!.textAlign = Paint.Align.CENTER
            canvas.drawText(
                yearText,
                rect.centerX(),
                rect.top + em * 2.2f,
                pText!!
            )
            skipYear = 1
        }

        val text = if (monthText != previousMonthText) {
            previousMonthText = monthText
            monthText
        } else {
            dayText
        }

        pText!!.textAlign = Paint.Align.CENTER
        canvas.drawText(
            text,
            rect.centerX(),
            rect.top + em * 1.2f,
            pText!!
        )
    }

    private val maxDayWidth: Float
        get() {
            var maxDayWidth = 0f
            val day = DateUtils.getStartOfTodayCalendarWithOffset()
            for (i in 0..27) {
                day[Calendar.DAY_OF_MONTH] = i
                val width = pText!!.measureText(dfDay!!.format(day.time))
                maxDayWidth = max(maxDayWidth, width)
            }
            return maxDayWidth
        }

    private val maxMonthWidth: Float
        get() {
            var maxMonthWidth = 0f
            val day = DateUtils.getStartOfTodayCalendarWithOffset()
            for (i in 0..11) {
                day[Calendar.MONTH] = i
                val width = pText!!.measureText(dfMonth!!.format(day.time))
                maxMonthWidth = max(maxMonthWidth, width)
            }
            return maxMonthWidth
        }

    private fun init() {
        initPaints()
        initColors()
        initDateFormats()
    }

    private fun initColors() {
        val res = StyledResources(context)
        primaryColor = res.getColor(R.attr.aboutScreenColor)
        textColor = res.getColor(R.attr.contrast60)
        gridColor = res.getColor(R.attr.contrast20)
    }

    private fun initDateFormats() {
        dfMonth = "MMM".toSimpleDataFormat()
        dfYear = "yyyy".toSimpleDataFormat()
        dfDay = "d".toSimpleDataFormat()
    }

    private fun initPaints() {
        pText = Paint()
        pText!!.isAntiAlias = true
        pGraph = Paint()
        pGraph!!.isAntiAlias = true
        pGrid = Paint()
        pGrid!!.isAntiAlias = true
    }
}
