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
import kotlin.math.min

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
    var paddingTop = 12.0
    var paddingBottom = 12.0
    var paddingLeft = 6.0
    var paddingRight = 6.0
    var barMargin = 4.0
    var barHeight = 10.0
    var labelPadding = 6.0
    var valuePadding = 6.0
    var nGridlines = 4
    var valuePrecision = 5
    var minValueRange = 0.00001
    var minLabelWidth = 18.0

    override val dataColumnWidth: Double
        get() = barHeight + barMargin * 2

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val rowHeight = barHeight + barMargin * 2
        val availableHeight = height - paddingTop - paddingBottom
        val visibleRows = max(1, floor(availableHeight / rowHeight).toInt())

        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        if (availableHeight <= 0 || series.isEmpty() || axis.isEmpty()) return

        val visibleEnd = min(series.size, dataOffset + visibleRows)
        val visibleValues = if (dataOffset < visibleEnd) {
            series.subList(dataOffset, visibleEnd)
        } else {
            emptyList()
        }

        val maxPositive = visibleValues.filter { it > 0 }.maxOrNull() ?: 0.0
        val maxNegative = visibleValues.filter { it < 0 }.minOrNull()?.let { abs(it) } ?: 0.0
        val hasPositive = maxPositive > 0.0
        val hasNegative = maxNegative > 0.0

        val posRange = paddedRange(maxPositive)
        val negRange = paddedRange(maxNegative)

        val isLargeInterval = axis.size < 2 || (axis[0].distanceTo(axis[1]) > 300)
        val labels = mutableListOf<String>()
        var labelWidth = minLabelWidth
        var positiveLabelWidth = 0.0
        var negativeLabelWidth = 0.0
        var prevMonth = -1
        for (row in 0 until visibleRows) {
            val dataIndex = row + dataOffset
            if (dataIndex >= axis.size || dataIndex >= series.size) break
            val date = axis[dataIndex]
            val label = if (isLargeInterval) {
                date.year.toString()
            } else if (date.month != prevMonth) {
                dateFormatter.shortMonthName(date)
            } else {
                date.day.toString()
            }
            labels.add(label)
            labelWidth = max(labelWidth, canvas.measureText(label))
            val value = series[dataIndex]
            val valueWidth = canvas.measureText(formatValue(abs(value)))
            if (value > 0) {
                positiveLabelWidth = max(positiveLabelWidth, valueWidth)
            } else if (value < 0) {
                negativeLabelWidth = max(negativeLabelWidth, valueWidth)
            }
            prevMonth = date.month
        }

        val negativeGutter = if (hasNegative) negativeLabelWidth + valuePadding else 0.0
        val positiveGutter = if (hasPositive) positiveLabelWidth + valuePadding else 0.0
        val chartLeft = paddingLeft + labelWidth + labelPadding + negativeGutter
        val chartRight = width - paddingRight - positiveGutter
        if (chartRight <= chartLeft) return
        val chartWidth = chartRight - chartLeft

        val posWidth: Double
        val negWidth: Double
        val zeroX: Double
        when {
            hasPositive && hasNegative -> {
                val total = posRange + negRange
                negWidth = chartWidth * (negRange / total)
                posWidth = chartWidth - negWidth
                zeroX = chartLeft + negWidth
            }
            hasPositive -> {
                posWidth = chartWidth
                negWidth = 0.0
                zeroX = chartLeft
            }
            hasNegative -> {
                posWidth = 0.0
                negWidth = chartWidth
                zeroX = chartRight
            }
            else -> {
                return
            }
        }

        drawGrid(canvas, chartLeft, chartRight, zeroX, paddingTop, availableHeight, posWidth, negWidth)

        canvas.setFontSize(theme.smallTextSize)
        for (row in 0 until visibleRows) {
            val dataIndex = row + dataOffset
            if (dataIndex >= series.size || dataIndex >= axis.size || row >= labels.size) continue

            val value = series[dataIndex]
            val y = paddingTop + row * rowHeight + barMargin
            val yCenter = y + barHeight / 2

            canvas.setTextAlign(TextAlign.RIGHT)
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.drawText(
                labels[row],
                paddingLeft + labelWidth,
                yCenter + theme.smallTextSize * 0.35
            )

            if (value == 0.0) continue

            val absValue = abs(value)
            val barLength = when {
                value > 0 && posWidth > 0 -> posWidth * (absValue / posRange)
                value < 0 && negWidth > 0 -> negWidth * (absValue / negRange)
                else -> 0.0
            }

            if (barLength > 0.0) {
                val barX = if (value > 0) zeroX else zeroX - barLength
                drawRoundedBar(canvas, barX, y, barLength, barHeight, if (value > 0) positiveColor else negativeColor)
            }

            val labelText = formatValue(absValue)
            if (value > 0) {
                canvas.setTextAlign(TextAlign.LEFT)
                canvas.setColor(positiveColor)
                canvas.drawText(
                    labelText,
                    chartRight + valuePadding,
                    yCenter + theme.smallTextSize * 0.35
                )
            } else {
                canvas.setTextAlign(TextAlign.RIGHT)
                canvas.setColor(negativeColor)
                canvas.drawText(
                    labelText,
                    chartLeft - valuePadding,
                    yCenter + theme.smallTextSize * 0.35
                )
            }
        }
    }

    private fun paddedRange(value: Double): Double {
        val absValue = abs(value)
        if (absValue <= 0.0) return minValueRange
        val padding = max(absValue * 0.05, minValueRange)
        return absValue + padding
    }

    private fun drawGrid(
        canvas: Canvas,
        chartLeft: Double,
        chartRight: Double,
        zeroX: Double,
        top: Double,
        height: Double,
        posWidth: Double,
        negWidth: Double
    ) {
        canvas.setStrokeWidth(0.5)
        canvas.setColor(theme.lowContrastTextColor)
        if (posWidth > 0 && nGridlines > 1) {
            for (k in 1 until nGridlines) {
                val x = zeroX + posWidth * (k.toDouble() / (nGridlines - 1))
                canvas.drawLine(x, top, x, top + height)
            }
        }
        if (negWidth > 0 && nGridlines > 1) {
            for (k in 1 until nGridlines) {
                val x = zeroX - negWidth * (k.toDouble() / (nGridlines - 1))
                canvas.drawLine(x, top, x, top + height)
            }
        }
        canvas.setColor(theme.mediumContrastTextColor)
        canvas.setStrokeWidth(1.0)
        canvas.drawLine(zeroX, top, zeroX, top + height)
        canvas.setColor(theme.lowContrastTextColor)
        canvas.drawLine(chartLeft, top, chartRight, top)
        canvas.drawLine(chartLeft, top + height, chartRight, top + height)
    }

    private fun drawRoundedBar(
        canvas: Canvas,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        color: Color
    ) {
        val r = height * 0.3
        canvas.setColor(color)
        if (width > 0.0) {
            canvas.fillRoundRect(x, y, width, height, r)
        }
    }

    private fun formatValue(value: Double): String {
        val format = "%.${valuePrecision}f"
        return String.format(format, value)
    }
}
