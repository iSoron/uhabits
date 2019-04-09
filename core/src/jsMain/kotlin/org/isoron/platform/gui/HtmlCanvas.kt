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

import org.w3c.dom.*
import kotlin.browser.*
import kotlin.math.*

class HtmlCanvas(val canvas: HTMLCanvasElement) : Canvas {

    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    var fontSize = 12.0
    var fontWeight = ""
    var fontFamily = "sans-serif"
    var align = CanvasTextAlign.CENTER

    override fun setColor(color: Color) {
        val c = "rgb(${color.red * 255}, ${color.green * 255}, ${color.blue * 255})"
        ctx.fillStyle = c;
        ctx.strokeStyle = c;
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        ctx.beginPath()
        ctx.moveTo(x1 + 0.5, y1 + 0.5)
        ctx.lineTo(x2 + 0.5, y2 + 0.5)
        ctx.stroke()
    }

    override fun drawText(text: String, x: Double, y: Double) {
        ctx.font = "${fontWeight} ${fontSize}px ${fontFamily}"
        ctx.textAlign = align
        ctx.textBaseline = CanvasTextBaseline.MIDDLE
        ctx.fillText(text, x, y)
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        ctx.fillRect(x - 0.5, y - 0.5, width + 1.0, height + 1.0)
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        ctx.strokeRect(x - 0.5, y - 0.5, width + 1.0, height + 1.0)
    }

    override fun getHeight(): Double {
        return canvas.height.toDouble()
    }

    override fun getWidth(): Double {
        return canvas.width.toDouble()
    }

    override fun setFont(font: Font) {
        fontWeight = if (font == Font.BOLD) "bold" else ""
        fontFamily = if (font == Font.FONT_AWESOME) "FontAwesome" else "sans-serif"
    }

    override fun setFontSize(size: Double) {
        fontSize = size
    }

    override fun setStrokeWidth(size: Double) {
        ctx.lineWidth = size
    }

    override fun fillArc(centerX: Double,
                         centerY: Double,
                         radius: Double,
                         startAngle: Double,
                         swipeAngle: Double) {
        val from = startAngle / 180 * PI
        val to = (startAngle + swipeAngle) / 180 * PI
        ctx.beginPath()
        ctx.moveTo(centerX, centerY)
        ctx.arc(centerX, centerY, radius, -from, -to, swipeAngle >= 0)
        ctx.lineTo(centerX, centerY)
        ctx.fill()
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        ctx.beginPath()
        ctx.arc(centerX, centerY, radius, 0.0, 2 * PI)
        ctx.fill()
    }

    override fun setTextAlign(align: TextAlign) {
        this.align = when(align) {
            TextAlign.LEFT -> CanvasTextAlign.LEFT
            TextAlign.CENTER -> CanvasTextAlign.CENTER
            TextAlign.RIGHT -> CanvasTextAlign.RIGHT
        }
    }
}