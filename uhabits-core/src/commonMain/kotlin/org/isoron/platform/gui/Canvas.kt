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

package org.isoron.platform.gui

enum class TextAlign {
    LEFT, CENTER, RIGHT
}

enum class Font {
    REGULAR,
    BOLD,
    FONT_AWESOME
}

data class ScreenLocation(
    val x: Double,
    val y: Double,
)

interface Canvas {
    fun setColor(color: Color)
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double)
    fun drawText(text: String, x: Double, y: Double)
    fun fillRect(x: Double, y: Double, width: Double, height: Double)
    fun fillRoundRect(x: Double, y: Double, width: Double, height: Double, cornerRadius: Double)
    fun drawRect(x: Double, y: Double, width: Double, height: Double)
    fun getHeight(): Double
    fun getWidth(): Double
    fun setFont(font: Font)
    fun setFontSize(size: Double)
    fun setStrokeWidth(size: Double)
    fun fillArc(
        centerX: Double,
        centerY: Double,
        radius: Double,
        startAngle: Double,
        swipeAngle: Double
    )
    fun fillCircle(centerX: Double, centerY: Double, radius: Double)
    fun setTextAlign(align: TextAlign)
    fun toImage(): Image
    fun measureText(text: String): Double

    /**
     * Fills entire canvas with the current color.
     */
    fun fill() {
        fillRect(0.0, 0.0, getWidth(), getHeight())
    }

    fun drawTestImage() {
        // Draw transparent background
        setColor(Color(0.1, 0.1, 0.1, 0.5))
        fillRect(0.0, 0.0, 500.0, 400.0)

        // Draw center rectangle
        setColor(Color(0x606060))
        setStrokeWidth(25.0)
        drawRect(100.0, 100.0, 300.0, 200.0)

        // Draw squares, circles and arcs
        setColor(Color.YELLOW)
        setStrokeWidth(1.0)
        drawRect(0.0, 0.0, 100.0, 100.0)
        fillCircle(50.0, 50.0, 30.0)
        drawRect(0.0, 100.0, 100.0, 100.0)
        fillArc(50.0, 150.0, 30.0, 90.0, 135.0)
        drawRect(0.0, 200.0, 100.0, 100.0)
        fillArc(50.0, 250.0, 30.0, 90.0, -135.0)
        drawRect(0.0, 300.0, 100.0, 100.0)
        fillArc(50.0, 350.0, 30.0, 45.0, 90.0)

        // Draw two red crossing lines
        setColor(Color.RED)
        setStrokeWidth(2.0)
        drawLine(0.0, 0.0, 500.0, 400.0)
        drawLine(500.0, 0.0, 0.0, 400.0)

        // Draw text
        setFont(Font.BOLD)
        setFontSize(50.0)
        setColor(Color.GREEN)
        setTextAlign(TextAlign.CENTER)
        drawText("HELLO", 250.0, 100.0)
        setTextAlign(TextAlign.RIGHT)
        drawText("HELLO", 250.0, 150.0)
        setTextAlign(TextAlign.LEFT)
        drawText("HELLO", 250.0, 200.0)

        // Draw FontAwesome icon
        setFont(Font.FONT_AWESOME)
        drawText(FontAwesome.CHECK, 250.0, 300.0)
    }
}
