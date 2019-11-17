/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.components

import org.isoron.platform.gui.*
import org.isoron.platform.time.*
import kotlin.math.*

class BarChart(var theme: Theme,
               var dateFormatter: LocalDateFormatter) : Component {

    // Data
    var series = mutableListOf<List<Double>>()
    var colors = mutableListOf<Color>()
    var axis = listOf<LocalDate>()

    // Style
    var paddingTop = 20.0
    var paddingLeft = 5.0
    var paddingRight = 5.0
    var footerHeight = 40.0
    var barGroupMargin = 4.0
    var barMargin = 4.0
    var barWidth = 20.0
    var nGridlines = 6
    var backgroundColor = theme.cardBackgroundColor

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()

        val n = series.size
        val barGroupWidth = 2 * barGroupMargin + n * (barWidth + 2 * barMargin)
        val safeWidth = width - paddingLeft - paddingRight
        val nColumns = floor((safeWidth) / barGroupWidth).toInt()
        val marginLeft = (safeWidth - nColumns * barGroupWidth) / 2
        val maxBarHeight = height - footerHeight - paddingTop
        var maxValue = series.map { it.max()!! }.max()!!
        maxValue = max(maxValue, 1.0)

        canvas.setColor(backgroundColor)
        canvas.fillRect(0.0, 0.0, width, height)

        fun barGroupOffset(c: Int) = marginLeft + paddingLeft +
                                     (c) * barGroupWidth

        fun barOffset(c: Int, s: Int) = barGroupOffset(c) +
                                        barGroupMargin +
                                        s * (barWidth + 2 * barMargin) +
                                        barMargin

        fun drawColumn(s: Int, c: Int) {
            val value = if (c < series[s].size) series[s][c] else 0.0
            val perc = value / maxValue
            val barColorPerc = if (n > 1) 1.0 else round(perc / 0.20) * 0.20
            val barColor = theme.lowContrastTextColor.blendWith(colors[s],
                                                                barColorPerc)
            val barHeight = round(maxBarHeight * perc)
            val x = barOffset(c, s)
            val y = height - footerHeight - barHeight
            canvas.setColor(barColor)
            val r = round(barWidth * 0.33)
            canvas.fillRect(x, y + r, barWidth, barHeight - r)
            canvas.fillRect(x + r, y, barWidth - 2 * r, r)
            canvas.fillCircle(x + r, y + r, r)
            canvas.fillCircle(x + barWidth - r, y + r, r)
            canvas.setFontSize(theme.smallTextSize)
            canvas.setTextAlign(TextAlign.CENTER)
            canvas.setColor(backgroundColor)
            canvas.fillRect(x - barMargin,
                            y - theme.smallTextSize * 1.25,
                            barWidth + 2 * barMargin,
                            theme.smallTextSize * 1.0)
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.drawText(value.toShortString(),
                            x + barWidth / 2,
                            y - theme.smallTextSize * 0.80)
        }

        fun drawSeries(s: Int) {
            for (c in 0 until nColumns) drawColumn(s, c)
        }

        fun drawMajorGrid() {
            canvas.setStrokeWidth(1.0)
            if (n > 1) {
                canvas.setColor(backgroundColor.blendWith(
                        theme.lowContrastTextColor,
                        0.5))
                for (c in 0 until nColumns - 1) {
                    val x = barGroupOffset(c)
                    canvas.drawLine(x, paddingTop, x, paddingTop + maxBarHeight)
                }
            }
            for (k in 1 until nGridlines) {
                val pct = 1.0 - (k.toDouble() / (nGridlines - 1))
                val y = paddingTop + maxBarHeight * pct
                canvas.setColor(theme.lowContrastTextColor)
                canvas.drawLine(0.0, y, width, y)
            }
        }

        fun drawFooter() {
            val y = paddingTop + maxBarHeight
            canvas.setColor(backgroundColor)
            canvas.fillRect(0.0, y, width, height - y)
            canvas.setColor(theme.lowContrastTextColor)
            canvas.drawLine(0.0, y, width, y)
            canvas.setColor(theme.mediumContrastTextColor)
            canvas.setTextAlign(TextAlign.CENTER)
            var prevMonth = -1
            var prevYear = -1
            val isLargeInterval = (axis[0].distanceTo(axis[1]) > 300)

            for (c in 0 until nColumns) {
                val x = barGroupOffset(c)
                val date = axis[c]
                if(isLargeInterval) {
                    canvas.drawText(date.year.toString(),
                                    x + barGroupWidth / 2,
                                    y + theme.smallTextSize * 1.0)
                } else {
                    if (date.month != prevMonth) {
                        canvas.drawText(dateFormatter.shortMonthName(date),
                                        x + barGroupWidth / 2,
                                        y + theme.smallTextSize * 1.0)
                    } else {
                        canvas.drawText(date.day.toString(),
                                        x + barGroupWidth / 2,
                                        y + theme.smallTextSize * 1.0)
                    }
                    if (date.year != prevYear) {
                        canvas.drawText(date.year.toString(),
                                        x + barGroupWidth / 2,
                                        y + theme.smallTextSize * 2.3)
                    }
                }
                prevMonth = date.month
                prevYear = date.year
            }
        }

        drawMajorGrid()
        for (k in 0 until n) drawSeries(k)
        drawFooter()
    }
}
