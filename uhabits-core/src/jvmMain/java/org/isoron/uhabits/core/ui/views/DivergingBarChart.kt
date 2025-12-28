/*
 * Copyright (C) 2016-2025 A?linson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Canvas
import org.isoron.platform.gui.Color
import org.isoron.platform.gui.DataView
import org.isoron.platform.gui.TextAlign
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.LocalDateFormatter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

class DivergingBarChart(
    var theme: Theme,
    var dateFormatter: LocalDateFormatter
) : DataView {

    // Data
    var series = mutableListOf<Double>()
    var axis = listOf<LocalDate>()
    var positiveColor = Color.GREEN
    var negativeColor = Color.RED
    override var dataOffset = 0

    // Style
    var paddingTop = 20.0
    var paddingLeft = 0.0
    var paddingRight = 0.0
    var footerHeight = 40.0
    var barGroupMargin = 4.0
    var barMargin = 3.0
    var barWidth = 18.0
    var nGridlines = 5
    var valuePrecision = 5
    var minValueRange = 0.00001

    override val dataColumnWidth: Double
        get() = barWidth + barMargin * 2

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val safeWidth = width - paddingLeft - paddingRight
        val barGroupWidth = 2 * barGroupMargin + (barWidth + 2 * barMargin)
        val visibleColumns = if (barGroupWidth <= 0) 1 else floor(safeWidth / barGroupWidth).toInt()
        val nColumns = max(1, visibleColumns)
        val marginLeft = (safeWidth - nColumns * barGroupWidth) / 2
        val maxBarHeight = height - footerHeight - paddingTop

        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        if (maxBarHeight <= 0) return

        val maxPositive = series.filter { it > 0 }.maxOrNull() ?: 0.0
        val maxNegative = series.filter { it < 0 }.minOrNull()?.let { abs(it) } ?: 0.0
        val hasPositive = maxPositive > 0.0
        val hasNegative = maxNegative > 0.0

        val posRange = paddedRange(maxPositive)
        val negRange = paddedRange(maxNegative)

        val posHeight: Double
        val negHeight: Double
        val zeroY: Double
        when {
            hasPositive && hasNegative -> {
                posHeight = maxBarHeight / 2
                negHeight = maxBarHeight / 2
                zeroY = paddingTop + posHeight
            }
            hasPositive -> {
                posHeight = maxBarHeight
                negHeight = 0.0
                zeroY = paddingTop + posHeight
            }
            hasNegative -> {
                posHeight = 0.0
                negHeight = maxBarHeight
                zeroY = paddingTop
            }
            else -> {
                drawAxis(canvas, nColumns, barGroupWidth, marginLeft, paddingTop + maxBarHeight)
                return
            }
        }

        drawGrid(canvas, zeroY, posHeight, negHeight)

        fun barGroupOffset(c: Int) = marginLeft + paddingLeft + (c) * barGroupWidth
        fun barOffset(c: Int) = barGroupOffset(c) + barGroupMargin + barMargin

        for (c in 0 until nColumns) {
            val dataColumn = nColumns - c - 1 + dataOffset
            if (dataColumn < 0 || dataColumn >= series.size) continue
            val value = series[dataColumn]
            if (value == 0.0) continue

            val absValue = abs(value)
            val barHeight = when {
                value > 0 && posHeight > 0 -> posHeight * (absValue / posRange)
                value < 0 && negHeight > 0 -> negHeight * (absValue / negRange)
                else -> 0.0
            }
            if (barHeight <= 0.0) continue

            val x = barOffset(c)
            if (value > 0) {
                val y = zeroY - barHeight
                drawRoundedBar(canvas, x, y, barHeight, positiveColor, true)
                drawValueLabel(canvas, x, y, barHeight, value)
            } else {
                val y = zeroY
                drawRoundedBar(canvas, x, y, barHeight, negativeColor, false)
                drawValueLabel(canvas, x, y, barHeight, value)
            }
        }

        drawAxis(canvas, nColumns, barGroupWidth, marginLeft, paddingTop + maxBarHeight)
    }

    private fun paddedRange(value: Double): Double {
        val absValue = abs(value)
        if (absValue <= 0.0) return minValueRange
        val padding = max(absValue * 0.05, minValueRange)
        return absValue + padding
    }

    private fun drawGrid(canvas: Canvas, zeroY: Double, posHeight: Double, negHeight: Double) {
        canvas.setStrokeWidth(0.5)
        canvas.setColor(theme.lowContrastTextColor)
        if (posHeight > 0 && nGridlines > 1) {
            for (k in 1 until nGridlines) {
                val y = zeroY - posHeight * (k.toDouble() / (nGridlines - 1))
                canvas.drawLine(0.0, y, canvas.getWidth(), y)
            }
        }
        if (negHeight > 0 && nGridlines > 1) {
            for (k in 1 until nGridlines) {
                val y = zeroY + negHeight * (k.toDouble() / (nGridlines - 1))
                canvas.drawLine(0.0, y, canvas.getWidth(), y)
            }
        }
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setStrokeWidth(1.0)
        canvas.drawLine(0.0, zeroY, canvas.getWidth(), zeroY)
    }

    private fun drawRoundedBar(
        canvas: Canvas,
        x: Double,
        y: Double,
        height: Double,
        color: Color,
        isPositive: Boolean
    ) {
        canvas.setColor(color)
        val r = barWidth * 0.15
        if (height > 2 * r) {
            if (isPositive) {
                canvas.fillRect(x, y + r, barWidth, height - r)
                canvas.fillRect(x + r, y, barWidth - 2 * r, r + 1)
                canvas.fillCircle(x + r, y + r, r)
                canvas.fillCircle(x + barWidth - r, y + r, r)
            } else {
                val bottom = y + height
                canvas.fillRect(x, y, barWidth, height - r)
                canvas.fillRect(x + r, bottom - r, barWidth - 2 * r, r + 1)
                canvas.fillCircle(x + r, bottom - r, r)
                canvas.fillCircle(x + barWidth - r, bottom - r, r)
            }
        } else {
            canvas.fillRect(x, y, barWidth, height)
        }
    }

    private fun drawValueLabel(canvas: Canvas, x: Double, y: Double, height: Double, value: Double) {
        val label = formatValue(abs(value))
        canvas.setFontSize(theme.smallTextSize)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.setColor(if (value >= 0) positiveColor else negativeColor)
        val labelY = if (value >= 0) {
            y - theme.smallTextSize * 0.4
        } else {
            y + height + theme.smallTextSize * 1.0
        }
        canvas.drawText(label, x + barWidth / 2, labelY)
    }

    private fun formatValue(value: Double): String {
        val format = "%.${valuePrecision}f"
        return String.format(format, value)
    }

    private fun drawAxis(
        canvas: Canvas,
        nColumns: Int,
        barGroupWidth: Double,
        marginLeft: Double,
        axisY: Double
    ) {
        canvas.setColor(theme.lowContrastTextColor)
        canvas.drawLine(0.0, axisY, canvas.getWidth(), axisY)
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.setFontSize(theme.smallTextSize)
        var prevMonth = -1
        var prevYear = -1
        val isLargeInterval = axis.size < 2 || (axis[0].distanceTo(axis[1]) > 300)

        fun barGroupOffset(c: Int) = marginLeft + paddingLeft + (c) * barGroupWidth

        for (c in 0 until nColumns) {
            val x = barGroupOffset(c)
            val dataColumn = nColumns - c - 1 + dataOffset
            if (dataColumn < 0 || dataColumn >= axis.size) continue
            val date = axis[dataColumn]
            if (isLargeInterval) {
                canvas.drawText(
                    date.year.toString(),
                    x + barGroupWidth / 2,
                    axisY + theme.smallTextSize * 1.0
                )
            } else {
                if (date.month != prevMonth) {
                    canvas.drawText(
                        dateFormatter.shortMonthName(date),
                        x + barGroupWidth / 2,
                        axisY + theme.smallTextSize * 1.0
                    )
                } else {
                    canvas.drawText(
                        date.day.toString(),
                        x + barGroupWidth / 2,
                        axisY + theme.smallTextSize * 1.0
                    )
                }
                if (date.year != prevYear) {
                    canvas.drawText(
                        date.year.toString(),
                        x + barGroupWidth / 2,
                        axisY + theme.smallTextSize * 2.3
                    )
                }
            }
            prevMonth = date.month
            prevYear = date.year
        }
    }
}
