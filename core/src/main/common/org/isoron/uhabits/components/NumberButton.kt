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
import org.isoron.platform.io.*
import kotlin.math.*

fun Double.toShortString(): String = when {
    this >= 1e9 -> format("%.1fG", this / 1e9)
    this >= 1e8 -> format("%.0fM", this / 1e6)
    this >= 1e7 -> format("%.1fM", this / 1e6)
    this >= 1e6 -> format("%.1fM", this / 1e6)
    this >= 1e5 -> format("%.0fk", this / 1e3)
    this >= 1e4 -> format("%.1fk", this / 1e3)
    this >= 1e3 -> format("%.1fk", this / 1e3)
    this >= 1e2 -> format("%.0f", this)
    this >= 1e1 -> when {
        round(this) == this -> format("%.0f", this)
        else -> format("%.1f", this)
    }
    else -> when {
        round(this) == this -> format("%.0f", this)
        round(this * 10) == this * 10 -> format("%.1f", this)
        else -> format("%.2f", this)
    }
}

class NumberButton(val color: Color,
                   val value: Double,
                   val threshold: Double,
                   val units: String,
                   val theme: Theme) : Component {

    override fun draw(canvas: Canvas) {
        val width = canvas.getWidth()
        val height = canvas.getHeight()
        val em = theme.smallTextSize

        canvas.setColor(when {
                            value >= threshold -> color
                            value >= 0.01 -> theme.mediumContrastTextColor
                            else -> theme.lowContrastTextColor
                        })

        canvas.setFontSize(theme.regularTextSize)
        canvas.setFont(Font.BOLD)
        canvas.drawText(value.toShortString(), width / 2, height / 2 - 0.6 * em)

        canvas.setFontSize(theme.smallTextSize)
        canvas.setFont(Font.REGULAR)
        canvas.drawText(units, width / 2, height / 2 + 0.6 * em)
    }
}