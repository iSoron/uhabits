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

class HabitListHeader(private val today: LocalDate,
                      private val nButtons: Int,
                      private val theme: Theme,
                      private val fmt: LocalDateFormatter) : Component {

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val buttonSize = theme.checkmarkButtonSize
        canvas.setColor(theme.headerBackgroundColor)
        canvas.fillRect(0.0, 0.0, width, height)

        canvas.setColor(theme.headerBorderColor)
        canvas.setStrokeWidth(0.5)
        canvas.drawLine(0.0, height - 0.5, width, height - 0.5)

        canvas.setColor(theme.headerTextColor)
        canvas.setFont(Font.BOLD)
        canvas.setFontSize(theme.smallTextSize)

        repeat(nButtons) { index ->
            val date = today.minus(nButtons - index - 1)
            val name = fmt.shortWeekdayName(date).toUpperCase()
            val number = date.day.toString()

            val x = width - (index + 1) * buttonSize + buttonSize / 2
            val y = height / 2
            canvas.drawText(name, x, y - theme.smallTextSize * 0.6)
            canvas.drawText(number, x, y + theme.smallTextSize * 0.6)
        }
    }
}
