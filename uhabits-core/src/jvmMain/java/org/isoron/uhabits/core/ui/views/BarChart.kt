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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Canvas
import org.isoron.platform.gui.Color
import org.isoron.platform.gui.DataView
import org.isoron.platform.gui.TextAlign
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.LocalDateFormatter
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round

class BarChart(
    var theme: Theme,
    var dateFormatter: LocalDateFormatter,
) : DataView {

    // Data
    var series = mutableListOf<List<Double>>()
    var colors = mutableListOf<Color>()
    var axis = listOf<LocalDate>()
    override var dataOffset = 0

    // Style
    var paddingTop = 20.0
    var paddingLeft = 0.0
    var paddingRight = 0.0
    var footerHeight = 40.0
    var barGroupMargin = 4.0
    var barMargin = 3.0
    var barWidth = 12.0
    var nGridlines = 6

    override val dataColumnWidth: Double
        get() = barWidth + barMargin * 2

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()

        val nSeries = series.size
        val barGroupWidth = 2 * barGroupMargin + nSeries * (barWidth + 2 * barMargin)
        val safeWidth = width - paddingLeft - paddingRight
        val nColumns = floor((safeWidth) / barGroupWidth).toInt()
        val marginLeft = (safeWidth - nColumns * barGroupWidth) / 2
        val maxBarHeight = height - footerHeight - paddingTop
        var maxValue = series.map { it.maxOrNull()!! }.maxOrNull()!!
        maxValue = max(maxValue, 1.0)

        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        fun barGroupOffset(c: Int) = marginLeft + paddingLeft +
            (c) * barGroupWidth

        fun barOffset(c: Int, s: Int) = barGroupOffset(c) +
            barGroupMargin +
            s * (barWidth + 2 * barMargin) +
            barMargin

        fun drawColumn(s: Int, c: Int) {
            val dataColumn = nColumns - c - 1 + dataOffset
            val value = when {
                dataColumn < 0 || dataColumn >= series[s].size -> 0.0
                else -> series[s][dataColumn]
            }
            if (value <= 0) return
            val perc = value / maxValue
            val barHeight = round(maxBarHeight * perc)
            val x = barOffset(c, s)
            val y = height - footerHeight - barHeight
            canvas.setColor(colors[s])
            val r = round(barWidth * 0.15)
            if (2 * r < barHeight) {
                canvas.fillRect(x, y + r, barWidth, barHeight - r)
                canvas.fillRect(x + r, y, barWidth - 2 * r, r)
                canvas.fillCircle(x + r, y + r, r)
                canvas.fillCircle(x + barWidth - r, y + r, r)
            } else {
                canvas.fillRect(x, y, barWidth, barHeight)
            }
            canvas.setFontSize(theme.smallTextSize)
            canvas.setTextAlign(TextAlign.CENTER)
            canvas.setColor(colors[s])
            canvas.drawText(
                value.toShortString(),
                x + barWidth / 2,
                y - theme.smallTextSize * 0.80
            )
        }

        fun drawSeries(s: Int) {
            for (c in 0 until nColumns) drawColumn(s, c)
        }

        fun drawMajorGrid() {
            canvas.setStrokeWidth(1.0)
            if (nSeries > 1) {
                canvas.setColor(theme.lowContrastTextColor.withAlpha(0.5))
                for (c in 0 until nColumns - 1) {
                    val x = barGroupOffset(c)
                    canvas.drawLine(x, paddingTop, x, paddingTop + maxBarHeight)
                }
            }
            for (k in 1 until nGridlines) {
                val pct = 1.0 - (k.toDouble() / (nGridlines - 1))
                val y = paddingTop + maxBarHeight * pct
                canvas.setColor(theme.lowContrastTextColor)
                canvas.setStrokeWidth(0.5)
                canvas.drawLine(0.0, y, width, y)
            }
        }

        fun drawAxis() {
            val y = paddingTop + maxBarHeight
            canvas.setColor(theme.lowContrastTextColor)
            canvas.drawLine(0.0, y, width, y)
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.setTextAlign(TextAlign.CENTER)
            canvas.setFontSize(theme.smallTextSize)
            var prevMonth = -1
            var prevYear = -1
            val isLargeInterval = axis.size < 2 || (axis[0].distanceTo(axis[1]) > 300)

            for (c in 0 until nColumns) {
                val x = barGroupOffset(c)
                val dataColumn = nColumns - c - 1 + dataOffset
                if (dataColumn < 0 || dataColumn >= axis.size) continue
                val date = axis[dataColumn]
                if (isLargeInterval) {
                    canvas.drawText(
                        date.year.toString(),
                        x + barGroupWidth / 2,
                        y + theme.smallTextSize * 1.0
                    )
                } else {
                    if (date.month != prevMonth) {
                        canvas.drawText(
                            dateFormatter.shortMonthName(date),
                            x + barGroupWidth / 2,
                            y + theme.smallTextSize * 1.0
                        )
                    } else {
                        canvas.drawText(
                            date.day.toString(),
                            x + barGroupWidth / 2,
                            y + theme.smallTextSize * 1.0
                        )
                    }
                    if (date.year != prevYear) {
                        canvas.drawText(
                            date.year.toString(),
                            x + barGroupWidth / 2,
                            y + theme.smallTextSize * 2.3
                        )
                    }
                }
                prevMonth = date.month
                prevYear = date.year
            }
        }

        drawMajorGrid()
        for (k in 0 until nSeries) drawSeries(k)
        drawAxis()
    }
}
