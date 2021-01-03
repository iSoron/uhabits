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
import org.isoron.platform.io.*
import java.awt.*
import java.awt.RenderingHints.*
import java.awt.font.*
import java.awt.image.*
import kotlin.math.*


class JavaCanvas(val image: BufferedImage,
                 val pixelScale: Double = 2.0) : Canvas {
    override fun toImage(): Image {
        return JavaImage(image)
    }

    private val frc = FontRenderContext(null, true, true)
    private var fontSize = 12.0
    private var font = Font.REGULAR
    private var textAlign = TextAlign.CENTER
    val widthPx = image.width
    val heightPx = image.height
    val g2d = image.createGraphics()

    private val NOTO_REGULAR_FONT = createFont("fonts/NotoSans-Regular.ttf")
    private val NOTO_BOLD_FONT = createFont("fonts/NotoSans-Bold.ttf")
    private val FONT_AWESOME_FONT = createFont("fonts/FontAwesome.ttf")

    init {
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);
        updateFont()
    }

    private fun toPixel(x: Double): Int {
        return (pixelScale * x).toInt()
    }

    private fun toDp(x: Int): Double {
        return x / pixelScale
    }

    override fun setColor(color: Color) {
        g2d.color = java.awt.Color(color.red.toFloat(),
                                   color.green.toFloat(),
                                   color.blue.toFloat(),
                                   color.alpha.toFloat())
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        g2d.drawLine(toPixel(x1), toPixel(y1), toPixel(x2), toPixel(y2))
    }

    override fun drawText(text: String, x: Double, y: Double) {
        updateFont()
        val bounds = g2d.font.getStringBounds(text, frc)
        val bWidth = bounds.width.roundToInt()
        val bHeight = bounds.height.roundToInt()
        val bx = bounds.x.roundToInt()
        val by = bounds.y.roundToInt()

        if (textAlign == TextAlign.CENTER) {
            g2d.drawString(text,
                           toPixel(x) - bx - bWidth / 2,
                           toPixel(y) - by - bHeight / 2)
        } else if (textAlign == TextAlign.LEFT) {
            g2d.drawString(text,
                           toPixel(x) - bx,
                           toPixel(y) - by - bHeight / 2)
        } else {
            g2d.drawString(text,
                           toPixel(x) - bx - bWidth,
                           toPixel(y) - by - bHeight / 2)
        }
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        g2d.fillRect(toPixel(x), toPixel(y), toPixel(width), toPixel(height))
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        g2d.drawRect(toPixel(x), toPixel(y), toPixel(width), toPixel(height))
    }

    override fun getHeight(): Double {
        return toDp(heightPx)
    }

    override fun getWidth(): Double {
        return toDp(widthPx)
    }


    override fun setFont(font: Font) {
        this.font = font
        updateFont()
    }

    override fun setFontSize(size: Double) {
        fontSize = size
        updateFont()
    }

    override fun setStrokeWidth(size: Double) {
        g2d.stroke = BasicStroke((size * pixelScale).toFloat())
    }

    private fun updateFont() {
        val size = (fontSize * pixelScale).toFloat()
        g2d.font = when (font) {
            Font.REGULAR -> NOTO_REGULAR_FONT.deriveFont(size)
            Font.BOLD -> NOTO_BOLD_FONT.deriveFont(size)
            Font.FONT_AWESOME -> FONT_AWESOME_FONT.deriveFont(size)
        }
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        g2d.fillOval(toPixel(centerX - radius),
                     toPixel(centerY - radius),
                     toPixel(radius * 2),
                     toPixel(radius * 2))
    }

    override fun fillArc(centerX: Double,
                         centerY: Double,
                         radius: Double,
                         startAngle: Double,
                         swipeAngle: Double) {

        g2d.fillArc(toPixel(centerX - radius),
                    toPixel(centerY - radius),
                    toPixel(radius * 2),
                    toPixel(radius * 2),
                    startAngle.roundToInt(),
                    swipeAngle.roundToInt())
    }

    override fun setTextAlign(align: TextAlign) {
        this.textAlign = align
    }

    private fun createFont(path: String) = runBlocking<java.awt.Font> {
        val file = JavaFileOpener().openResourceFile(path) as JavaResourceFile
        if (!file.exists()) throw RuntimeException("File not found: ${file.path}")
        java.awt.Font.createFont(0, file.stream())
    }
}