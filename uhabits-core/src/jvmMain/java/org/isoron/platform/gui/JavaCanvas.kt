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

import kotlinx.coroutines.runBlocking
import org.isoron.platform.io.JavaFileOpener
import org.isoron.platform.io.JavaResourceFile
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.KEY_FRACTIONALMETRICS
import java.awt.RenderingHints.KEY_TEXT_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON
import java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON
import java.awt.font.FontRenderContext
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class JavaCanvas(
    val image: BufferedImage,
    val pixelScale: Double = 2.0,
) : Canvas {

    override fun toImage(): Image {
        return JavaImage(image)
    }

    override fun measureText(text: String): Double {
        val metrics = g2d.getFontMetrics(g2d.font)
        return toDp(metrics.stringWidth(text))
    }

    private val frc = FontRenderContext(null, true, true)
    private var fontSize = 12.0
    private var font = Font.REGULAR
    private var textAlign = TextAlign.CENTER
    val widthPx = image.width
    val heightPx = image.height
    val g2d: Graphics2D = image.createGraphics()

    private val NOTO_REGULAR_FONT = createFont("fonts/NotoSans-Regular.ttf")
    private val NOTO_BOLD_FONT = createFont("fonts/NotoSans-Bold.ttf")
    private val FONT_AWESOME_FONT = createFont("fonts/FontAwesome.ttf")

    init {
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON)
        updateFont()
    }

    private fun toPixel(x: Double): Int {
        return (pixelScale * x).toInt()
    }

    private fun toDp(x: Int): Double {
        return x / pixelScale
    }

    override fun setColor(color: Color) {
        g2d.color = java.awt.Color(
            color.red.toFloat(),
            color.green.toFloat(),
            color.blue.toFloat(),
            color.alpha.toFloat()
        )
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

        when (textAlign) {
            TextAlign.CENTER -> {
                g2d.drawString(
                    text,
                    toPixel(x) - bx - bWidth / 2,
                    toPixel(y) - by - bHeight / 2
                )
            }
            TextAlign.LEFT -> {
                g2d.drawString(
                    text,
                    toPixel(x) - bx,
                    toPixel(y) - by - bHeight / 2
                )
            }
            else -> {
                g2d.drawString(
                    text,
                    toPixel(x) - bx - bWidth,
                    toPixel(y) - by - bHeight / 2
                )
            }
        }
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        g2d.fillRect(toPixel(x), toPixel(y), toPixel(width), toPixel(height))
    }

    override fun fillRoundRect(
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        cornerRadius: Double,
    ) {
        g2d.fill(
            RoundRectangle2D.Double(
                toPixel(x).toDouble(),
                toPixel(y).toDouble(),
                toPixel(width).toDouble(),
                toPixel(height).toDouble(),
                toPixel(cornerRadius).toDouble(),
                toPixel(cornerRadius).toDouble(),
            )
        )
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
        g2d.fillOval(
            toPixel(centerX - radius),
            toPixel(centerY - radius),
            toPixel(radius * 2),
            toPixel(radius * 2)
        )
    }

    override fun fillArc(
        centerX: Double,
        centerY: Double,
        radius: Double,
        startAngle: Double,
        swipeAngle: Double,
    ) {

        g2d.fillArc(
            toPixel(centerX - radius),
            toPixel(centerY - radius),
            toPixel(radius * 2),
            toPixel(radius * 2),
            startAngle.roundToInt(),
            swipeAngle.roundToInt()
        )
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
