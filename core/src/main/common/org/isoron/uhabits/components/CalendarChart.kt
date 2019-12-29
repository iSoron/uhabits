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

class CalendarChart(var today: LocalDate,
                    var color: Color,
                    var theme: Theme,
                    var dateFormatter: LocalDateFormatter) : Component {

    var padding = 5.0
    var backgroundColor = Color(0xFFFFFF)
    var squareSpacing = 1.0
    var series = listOf<Double>()
    var scrollPosition = 0
    private var squareSize = 0.0

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        canvas.setColor(backgroundColor)
        canvas.fillRect(0.0, 0.0, width, height)
        squareSize = round((height - 2 * padding) / 8.0)
        canvas.setFontSize(height * 0.06)

        val nColumns = floor((width - 2 * padding) / squareSize).toInt() - 2
        val todayWeekday = today.dayOfWeek
        val topLeftOffset = (nColumns - 1 + scrollPosition) * 7 + todayWeekday.index
        val topLeftDate = today.minus(topLeftOffset)

        repeat(nColumns) { column ->
            val topOffset = topLeftOffset - 7 * column
            val topDate = topLeftDate.plus(7 * column)
            drawColumn(canvas, column, topDate, topOffset)
        }

        canvas.setColor(theme.mediumContrastTextColor)
        repeat(7) { row ->
            val date = topLeftDate.plus(row)
            canvas.setTextAlign(TextAlign.LEFT)
            canvas.drawText(dateFormatter.shortWeekdayName(date),
                            padding + nColumns * squareSize + padding,
                            padding + squareSize * (row+1) + squareSize / 2)
        }
    }

    private fun drawColumn(canvas: Canvas,
                           column: Int,
                           topDate: LocalDate,
                           topOffset: Int) {
        drawHeader(canvas, column, topDate)
        repeat(7) { row ->
            val offset = topOffset - row
            val date = topDate.plus(row)
            if (offset < 0) return
            drawSquare(canvas,
                       padding + column * squareSize,
                       padding + (row + 1) * squareSize,
                       squareSize - squareSpacing,
                       squareSize - squareSpacing,
                       date,
                       offset)
        }
    }

    private fun drawHeader(canvas: Canvas, column: Int, date: LocalDate) {
        if (date.day >= 8) return

        canvas.setColor(theme.mediumContrastTextColor)
        if (date.month == 1) {
            canvas.drawText(date.year.toString(),
                            padding + column * squareSize + squareSize / 2,
                            padding + squareSize / 2)

        } else {
            canvas.drawText(dateFormatter.shortMonthName(date),
                            padding + column * squareSize + squareSize / 2,
                            padding + squareSize / 2)
        }
    }

    private fun drawSquare(canvas: Canvas,
                           x: Double,
                           y: Double,
                           width: Double,
                           height: Double,
                           date: LocalDate,
                           offset: Int) {

        var value = if (offset >= series.size) 0.0 else series[offset]
        value = round(value * 5.0) / 5.0

        var squareColor = color.blendWith(backgroundColor, 1 - value)
        var textColor = backgroundColor

        if (value == 0.0) squareColor = theme.lowContrastTextColor
        if (squareColor.luminosity > 0.8)
            textColor = squareColor.blendWith(theme.highContrastTextColor, 0.5)

        canvas.setColor(squareColor)
        canvas.fillRect(x, y, width, height)
        canvas.setColor(textColor)
        canvas.drawText(date.day.toString(), x + width / 2, y + width / 2)
    }
}