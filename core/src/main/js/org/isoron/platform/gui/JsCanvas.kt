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

import kotlinx.coroutines.*
import org.w3c.dom.*
import kotlin.js.*
import kotlin.math.*

class JsCanvas(val element: HTMLCanvasElement,
               val pixelScale: Double) : Canvas {


    val ctx = element.getContext("2d") as CanvasRenderingContext2D
    var fontSize = 12.0
    var fontFamily = "NotoRegular"
    var align = CanvasTextAlign.CENTER

    private fun toPixel(x: Double): Double {
        return pixelScale * x
    }

    private fun toDp(x: Int): Double {
        return x / pixelScale
    }

    override fun setColor(color: Color) {
        val c = "rgb(${color.red * 255}, ${color.green * 255}, ${color.blue * 255})"
        ctx.fillStyle = c;
        ctx.strokeStyle = c;
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        ctx.beginPath()
        ctx.moveTo(toPixel(x1), toPixel(y1))
        ctx.lineTo(toPixel(x2), toPixel(y2))
        ctx.stroke()
    }

    override fun drawText(text: String, x: Double, y: Double) {
        ctx.font = "${fontSize}px ${fontFamily}"
        ctx.textAlign = align
        ctx.textBaseline = CanvasTextBaseline.MIDDLE
        ctx.fillText(text, toPixel(x), toPixel(y + fontSize * 0.025))
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        ctx.fillRect(toPixel(x),
                     toPixel(y),
                     toPixel(width),
                     toPixel(height))
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        ctx.strokeRect(toPixel(x),
                       toPixel(y),
                       toPixel(width),
                       toPixel(height))
    }

    override fun getHeight(): Double {
        return toDp(element.height)
    }

    override fun getWidth(): Double {
        return toDp(element.width)
    }

    override fun setFont(font: Font) {
        fontFamily = when (font) {
            Font.REGULAR -> "NotoRegular"
            Font.BOLD -> "NotoBold"
            Font.FONT_AWESOME -> "FontAwesome"
        }
    }

    override fun setFontSize(size: Double) {
        fontSize = size * pixelScale
    }

    override fun setStrokeWidth(size: Double) {
        ctx.lineWidth = size * pixelScale
    }

    override fun fillArc(centerX: Double,
                         centerY: Double,
                         radius: Double,
                         startAngle: Double,
                         swipeAngle: Double) {
        val x = toPixel(centerX)
        val y = toPixel(centerY)
        val from = startAngle / 180 * PI
        val to = (startAngle + swipeAngle) / 180 * PI
        ctx.beginPath()
        ctx.moveTo(x, y)
        ctx.arc(x, y, toPixel(radius), -from, -to, swipeAngle >= 0)
        ctx.lineTo(x, y)
        ctx.fill()
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        ctx.beginPath()
        ctx.arc(toPixel(centerX),
                toPixel(centerY),
                toPixel(radius),
                0.0,
                2 * PI)
        ctx.fill()
    }

    override fun setTextAlign(align: TextAlign) {
        this.align = when (align) {
            TextAlign.LEFT -> CanvasTextAlign.LEFT
            TextAlign.CENTER -> CanvasTextAlign.CENTER
            TextAlign.RIGHT -> CanvasTextAlign.RIGHT
        }
    }

    override fun toImage(): Image {
        return JsImage(this,
                       ctx.getImageData(0.0,
                                        0.0,
                                        element.width.toDouble(),
                                        element.height.toDouble()))
    }
}