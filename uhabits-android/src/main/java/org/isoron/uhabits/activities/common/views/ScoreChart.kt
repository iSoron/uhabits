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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getStartOfTodayCalendarWithOffset
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.isoron.uhabits.utils.InterfaceUtils.dpToPixels
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.StyledResources
import org.isoron.uhabits.utils.toSimpleDataFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.LinkedList
import java.util.Locale
import java.util.Random
import kotlin.math.max
import kotlin.math.min

class ScoreChart : ScrollableChart {
    private var pGrid: Paint? = null
    private var em = 0f
    private var dfMonth: SimpleDateFormat? = null
    private var dfDay: SimpleDateFormat? = null
    private var dfYear: SimpleDateFormat? = null
    private var pText: Paint? = null
    private var pGraph: Paint? = null
    private var rect: RectF? = null
    private var prevRect: RectF? = null
    private var baseSize = 0
    private var internalPaddingTop = 0
    private var columnWidth = 0f
    private var columnHeight = 0
    private var nColumns = 0
    private var textColor = 0
    private var gridColor = 0
    private var scores: List<Score>? = null
    private var primaryColor = 0

    @Deprecated("")
    private var bucketSize = 7
    private var internalBackgroundColor = 0
    private var internalDrawingCache: Bitmap? = null
    private var cacheCanvas: Canvas? = null
    private var isTransparencyEnabled = false
    private var skipYear = 0
    private var previousYearText: String? = null
    private var previousMonthText: String? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun populateWithRandomData() {
        val random = Random()
        val newScores = LinkedList<Score>()
        var previous = 0.5
        val timestamp: Timestamp = getToday()
        for (i in 1..99) {
            val step = 0.1
            var current = previous + random.nextDouble() * step * 2 - step
            current = max(0.0, min(1.0, current))
            newScores.add(Score(timestamp.minus(i), current))
            previous = current
        }
        scores = newScores
    }

    fun setBucketSize(bucketSize: Int) {
        this.bucketSize = bucketSize
        postInvalidate()
    }

    fun setIsTransparencyEnabled(enabled: Boolean) {
        isTransparencyEnabled = enabled
        postInvalidate()
    }

    fun setColor(primaryColor: Int) {
        this.primaryColor = primaryColor
        postInvalidate()
    }

    fun setScores(scores: List<Score>) {
        this.scores = scores
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val activeCanvas: Canvas?
        if (isTransparencyEnabled) {
            if (internalDrawingCache == null) initCache(width, height)
            activeCanvas = cacheCanvas
            internalDrawingCache!!.eraseColor(Color.TRANSPARENT)
        } else {
            activeCanvas = canvas
        }
        if (scores == null) return
        rect!![0f, 0f, nColumns * columnWidth] = columnHeight.toFloat()
        rect!!.offset(0f, internalPaddingTop.toFloat())
        drawGrid(activeCanvas, rect)
        pText!!.color = textColor
        pGraph!!.color = primaryColor
        prevRect!!.setEmpty()
        previousMonthText = ""
        previousYearText = ""
        skipYear = 0
        for (k in 0 until nColumns) {
            val offset = nColumns - k - 1 + dataOffset
            if (offset >= scores!!.size) continue
            val score = scores!![offset].value
            val timestamp = scores!![offset].timestamp
            val height = (columnHeight * score).toInt()
            rect!![0f, 0f, baseSize.toFloat()] = baseSize.toFloat()
            rect!!.offset(
                k * columnWidth + (columnWidth - baseSize) / 2,
                (
                    internalPaddingTop + columnHeight - height - baseSize / 2
                    ).toFloat()
            )
            if (!prevRect!!.isEmpty) {
                drawLine(activeCanvas, prevRect, rect)
                drawMarker(activeCanvas, prevRect)
            }
            if (k == nColumns - 1) drawMarker(activeCanvas, rect)
            prevRect!!.set(rect!!)
            rect!![0f, 0f, columnWidth] = columnHeight.toFloat()
            rect!!.offset(k * columnWidth, internalPaddingTop.toFloat())
            drawFooter(activeCanvas, rect, timestamp)
        }
        if (activeCanvas !== canvas) canvas.drawBitmap(internalDrawingCache!!, 0f, 0f, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        var height = height
        if (height < 9) height = 200
        val maxTextSize = getDimension(context, R.dimen.tinyTextSize)
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
        val minStrokeWidth = dpToPixels(context, 1f)
        pGraph!!.textSize = baseSize * 0.5f
        pGraph!!.strokeWidth = baseSize * 0.1f
        pGrid!!.strokeWidth = min(minStrokeWidth, baseSize * 0.05f)
        if (isTransparencyEnabled) initCache(width, height)
    }

    private fun drawFooter(canvas: Canvas?, rect: RectF?, currentDate: Timestamp) {
        val yearText = dfYear!!.format(currentDate.toJavaDate())
        val monthText = dfMonth!!.format(currentDate.toJavaDate())
        val dayText = dfDay!!.format(currentDate.toJavaDate())
        val calendar = currentDate.toCalendar()
        val text: String
        val year = calendar[Calendar.YEAR]
        var shouldPrintYear = true
        if (yearText == previousYearText) shouldPrintYear = false
        if (bucketSize >= 365 && year % 2 != 0) shouldPrintYear = false
        if (skipYear > 0) {
            skipYear--
            shouldPrintYear = false
        }
        if (shouldPrintYear) {
            previousYearText = yearText
            previousMonthText = ""
            pText!!.textAlign = Paint.Align.CENTER
            canvas!!.drawText(
                yearText,
                rect!!.centerX(),
                rect.bottom + em * 2.2f,
                pText!!
            )
            skipYear = 1
        }
        if (bucketSize < 365) {
            if (monthText != previousMonthText) {
                previousMonthText = monthText
                text = monthText
            } else {
                text = dayText
            }
            pText!!.textAlign = Paint.Align.CENTER
            canvas!!.drawText(
                text,
                rect!!.centerX(),
                rect.bottom + em * 1.2f,
                pText!!
            )
        }
    }

    private fun drawGrid(canvas: Canvas?, rGrid: RectF?) {
        val nRows = 5
        val rowHeight = rGrid!!.height() / nRows
        pText!!.textAlign = Paint.Align.LEFT
        pText!!.color = textColor
        pGrid!!.color = gridColor
        for (i in 0 until nRows) {
            canvas!!.drawText(
                String.format("%d%%", 100 - i * 100 / nRows),
                rGrid.left + 0.5f * em,
                rGrid.top + 1f * em,
                pText!!
            )
            canvas.drawLine(
                rGrid.left,
                rGrid.top,
                rGrid.right,
                rGrid.top,
                pGrid!!
            )
            rGrid.offset(0f, rowHeight)
        }
        canvas!!.drawLine(rGrid.left, rGrid.top, rGrid.right, rGrid.top, pGrid!!)
    }

    private fun drawLine(canvas: Canvas?, rectFrom: RectF?, rectTo: RectF?) {
        pGraph!!.color = primaryColor
        canvas!!.drawLine(
            rectFrom!!.centerX(),
            rectFrom.centerY(),
            rectTo!!.centerX(),
            rectTo.centerY(),
            pGraph!!
        )
    }

    private fun drawMarker(canvas: Canvas?, rect: RectF?) {
        rect!!.inset(baseSize * 0.225f, baseSize * 0.225f)
        setModeOrColor(pGraph, XFERMODE_CLEAR, internalBackgroundColor)
        canvas!!.drawOval(rect, pGraph!!)
        rect.inset(baseSize * 0.1f, baseSize * 0.1f)
        setModeOrColor(pGraph, XFERMODE_SRC, primaryColor)
        canvas.drawOval(rect, pGraph!!)

//        rect.inset(baseSize * 0.1f, baseSize * 0.1f);
//        setModeOrColor(pGraph, XFERMODE_CLEAR, backgroundColor);
//        canvas.drawOval(rect, pGraph);
        if (isTransparencyEnabled) pGraph!!.xfermode = XFERMODE_SRC
    }

    private val maxDayWidth: Float
        private get() {
            var maxDayWidth = 0f
            val day: GregorianCalendar =
                getStartOfTodayCalendarWithOffset()
            for (i in 0..27) {
                day[Calendar.DAY_OF_MONTH] = i
                val monthWidth = pText!!.measureText(dfMonth!!.format(day.time))
                maxDayWidth = max(maxDayWidth, monthWidth)
            }
            return maxDayWidth
        }
    private val maxMonthWidth: Float
        private get() {
            var maxMonthWidth = 0f
            val day: GregorianCalendar =
                getStartOfTodayCalendarWithOffset()
            for (i in 0..11) {
                day[Calendar.MONTH] = i
                val monthWidth = pText!!.measureText(dfMonth!!.format(day.time))
                maxMonthWidth = max(maxMonthWidth, monthWidth)
            }
            return maxMonthWidth
        }

    private fun init() {
        initPaints()
        initColors()
        initDateFormats()
        initRects()
    }

    private fun initCache(width: Int, height: Int) {
        if (internalDrawingCache != null) internalDrawingCache!!.recycle()
        val newDrawingCache = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        internalDrawingCache = newDrawingCache
        cacheCanvas = Canvas(newDrawingCache)
    }

    private fun initColors() {
        val res = StyledResources(context)
        primaryColor = Color.BLACK
        textColor = res.getColor(R.attr.contrast60)
        gridColor = res.getColor(R.attr.contrast20)
        internalBackgroundColor = res.getColor(R.attr.cardBgColor)
    }

    private fun initDateFormats() {
        if (isInEditMode) {
            dfMonth = SimpleDateFormat("MMM", Locale.getDefault())
            dfYear = SimpleDateFormat("yyyy", Locale.getDefault())
            dfDay = SimpleDateFormat("d", Locale.getDefault())
        } else {
            dfMonth = "MMM".toSimpleDataFormat()
            dfYear = "yyyy".toSimpleDataFormat()
            dfDay = "d".toSimpleDataFormat()
        }
    }

    private fun initPaints() {
        pText = Paint()
        pText!!.isAntiAlias = true
        pGraph = Paint()
        pGraph!!.textAlign = Paint.Align.CENTER
        pGraph!!.isAntiAlias = true
        pGrid = Paint()
        pGrid!!.isAntiAlias = true
    }

    private fun initRects() {
        rect = RectF()
        prevRect = RectF()
    }

    private fun setModeOrColor(p: Paint?, mode: PorterDuffXfermode, color: Int) {
        if (isTransparencyEnabled) p!!.xfermode = mode else p!!.color = color
    }

    companion object {
        private val XFERMODE_CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        private val XFERMODE_SRC = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }
}
