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

package org.isoron.platform.gui

enum class TextAlign {
    LEFT, CENTER, RIGHT
}

enum class Font {
    REGULAR,
    BOLD,
    FONT_AWESOME
}

interface Canvas {
    fun setColor(color: Color)
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double)
    fun drawText(text: String, x: Double, y: Double)
    fun fillRect(x: Double, y: Double, width: Double, height: Double)
    fun drawRect(x: Double, y: Double, width: Double, height: Double)
    fun getHeight(): Double
    fun getWidth(): Double
    fun setFont(font: Font)
    fun setFontSize(size: Double)
    fun setStrokeWidth(size: Double)
    fun fillArc(centerX: Double,
                centerY: Double,
                radius: Double,
                startAngle: Double,
                swipeAngle: Double)
    fun fillCircle(centerX: Double, centerY: Double, radius: Double)
    fun setTextAlign(align: TextAlign)
    fun toImage(): Image
}

