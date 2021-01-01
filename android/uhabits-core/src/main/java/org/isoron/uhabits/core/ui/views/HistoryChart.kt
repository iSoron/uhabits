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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Canvas
import org.isoron.platform.gui.Color
import org.isoron.platform.gui.DataView
import org.isoron.platform.gui.TextAlign
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.LocalDateFormatter
import org.isoron.uhabits.core.models.PaletteColor
import kotlin.math.floor
import kotlin.math.round

class HistoryChart(
    var today: LocalDate,
    var paletteColor: PaletteColor,
    var theme: Theme,
    var dateFormatter: LocalDateFormatter
) : DataView {

    enum class Square {
        ON,
        OFF,
        DIMMED,
        HATCHED,
    }

    // Data
    var series = listOf<Square>()

    // Style
    var padding = 0.0
    var squareSpacing = 1.0
    override var dataOffset = 0
    private var squareSize = 0.0

    var lastPrintedMonth = ""
    var lastPrintedYear = ""

    override val dataColumnWidth: Double
        get() = squareSpacing + squareSize

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        canvas.setColor(theme.cardBackgroundColor)
        canvas.fillRect(0.0, 0.0, width, height)
        squareSize = round((height - 2 * padding) / 8.0)
        canvas.setFontSize(height * 0.06)

        val nColumns = floor((width - 2 * padding) / squareSize).toInt() - 2
        val todayWeekday = today.dayOfWeek
        val topLeftOffset = (nColumns - 1 + dataOffset) * 7 + todayWeekday.index
        val topLeftDate = today.minus(topLeftOffset)

        lastPrintedYear = ""
        lastPrintedMonth = ""

        // Draw main columns
        repeat(nColumns) { column ->
            val topOffset = topLeftOffset - 7 * column
            val topDate = topLeftDate.plus(7 * column)
            drawColumn(canvas, column, topDate, topOffset)
        }

        // Draw week day names
        canvas.setColor(theme.mediumContrastTextColor)
        repeat(7) { row ->
            val date = topLeftDate.plus(row)
            canvas.setTextAlign(TextAlign.LEFT)
            canvas.drawText(
                dateFormatter.shortWeekdayName(date),
                padding + nColumns * squareSize + squareSpacing * 3,
                padding + squareSize * (row + 1) + squareSize / 2
            )
        }
    }

    private fun drawColumn(
        canvas: Canvas,
        column: Int,
        topDate: LocalDate,
        topOffset: Int
    ) {
        drawHeader(canvas, column, topDate)
        repeat(7) { row ->
            val offset = topOffset - row
            val date = topDate.plus(row)
            if (offset < 0) return
            drawSquare(
                canvas,
                padding + column * squareSize,
                padding + (row + 1) * squareSize,
                squareSize - squareSpacing,
                squareSize - squareSpacing,
                date,
                offset
            )
        }
    }

    private fun drawHeader(canvas: Canvas, column: Int, date: LocalDate) {
        canvas.setColor(theme.mediumContrastTextColor)
        val monthText = dateFormatter.shortMonthName(date)
        val yearText = date.year.toString()
        val headerText: String
        when {
            monthText != lastPrintedMonth -> {
                headerText = monthText
                lastPrintedMonth = monthText
            }
            yearText != lastPrintedYear -> {
                headerText = yearText
                lastPrintedYear = headerText
            }
            else -> {
                headerText = ""
            }
        }
        canvas.setTextAlign(TextAlign.LEFT)
        canvas.drawText(
            headerText,
            padding + column * squareSize,
            padding + squareSize / 2
        )
    }

    private fun drawSquare(
        canvas: Canvas,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        date: LocalDate,
        offset: Int
    ) {

        val value = if (offset >= series.size) Square.OFF else series[offset]
        val squareColor: Color
        val color = theme.color(paletteColor.paletteIndex)
        when (value) {
            Square.ON -> {
                squareColor = color
            }
            Square.OFF -> {
                squareColor = theme.lowContrastTextColor
            }
            Square.DIMMED, Square.HATCHED -> {
                squareColor = color.blendWith(theme.cardBackgroundColor, 0.5)
            }
        }

        canvas.setColor(squareColor)
        canvas.fillRoundRect(x, y, width, height, width * 0.15)

        if (value == Square.HATCHED) {
            canvas.setStrokeWidth(0.75)
            canvas.setColor(theme.cardBackgroundColor)
            var k = width / 10
            repeat(5) {
                canvas.drawLine(x + k, y, x, y + k)
                canvas.drawLine(
                    x + width - k,
                    y + height,
                    x + width,
                    y + height - k
                )
                k += width / 5
            }
        }

        val c1 = squareColor.contrast(theme.cardBackgroundColor)
        val c2 = squareColor.contrast(theme.mediumContrastTextColor)
        val textColor = if (c1 > c2) theme.cardBackgroundColor else theme.mediumContrastTextColor

        canvas.setColor(textColor)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.drawText(date.day.toString(), x + width / 2, y + width / 2)
    }
}
