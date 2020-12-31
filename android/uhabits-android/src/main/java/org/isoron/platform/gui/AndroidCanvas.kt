/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import org.isoron.uhabits.utils.InterfaceUtils.getFontAwesome

class AndroidCanvas : Canvas {

    lateinit var innerCanvas: android.graphics.Canvas
    lateinit var context: Context
    var innerBitmap: Bitmap? = null
    var density = 1.0
    var paint = Paint().apply {
        isAntiAlias = true
    }
    var textPaint = TextPaint().apply {
        isAntiAlias = true
    }
    var textBounds = Rect()

    private fun Double.toDp() = (this * density).toFloat()

    override fun setColor(color: Color) {
        paint.color = color.toInt()
        textPaint.color = color.toInt()
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        innerCanvas.drawLine(
            x1.toDp(),
            y1.toDp(),
            x2.toDp(),
            y2.toDp(),
            paint,
        )
    }

    override fun drawText(text: String, x: Double, y: Double) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        innerCanvas.drawText(
            text,
            x.toDp(),
            y.toDp() - textBounds.exactCenterY(),
            textPaint,
        )
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        paint.style = Paint.Style.FILL
        rect(x, y, width, height)
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        paint.style = Paint.Style.STROKE
        rect(x, y, width, height)
    }

    private fun rect(x: Double, y: Double, width: Double, height: Double) {
        innerCanvas.drawRect(
            x.toDp(),
            y.toDp(),
            (x + width).toDp(),
            (y + height).toDp(),
            paint,
        )
    }

    override fun getHeight(): Double {
        return innerCanvas.height / density
    }

    override fun getWidth(): Double {
        return innerCanvas.width / density
    }

    override fun setFont(font: Font) {
        textPaint.typeface = when (font) {
            Font.REGULAR -> Typeface.DEFAULT
            Font.BOLD -> Typeface.DEFAULT_BOLD
            Font.FONT_AWESOME -> getFontAwesome(context)
        }
    }

    override fun setFontSize(size: Double) {
        textPaint.textSize = size.toDp() * 1.07f
    }

    override fun setStrokeWidth(size: Double) {
        paint.strokeWidth = size.toDp()
    }

    override fun fillArc(
        centerX: Double,
        centerY: Double,
        radius: Double,
        startAngle: Double,
        swipeAngle: Double,
    ) {
        paint.style = Paint.Style.FILL
        innerCanvas.drawArc(
            (centerX - radius).toDp(),
            (centerY - radius).toDp(),
            (centerX + radius).toDp(),
            (centerY + radius).toDp(),
            -startAngle.toFloat(),
            -swipeAngle.toFloat(),
            true,
            paint,
        )
    }

    override fun fillCircle(
        centerX: Double,
        centerY: Double,
        radius: Double,
    ) {
        paint.style = Paint.Style.FILL
        innerCanvas.drawCircle(centerX.toDp(), centerY.toDp(), radius.toDp(), paint)
    }

    override fun setTextAlign(align: TextAlign) {
        textPaint.textAlign = when (align) {
            TextAlign.LEFT -> Paint.Align.LEFT
            TextAlign.CENTER -> Paint.Align.CENTER
            TextAlign.RIGHT -> Paint.Align.RIGHT
        }
    }

    override fun toImage(): Image {
        val bmp = innerBitmap ?: throw UnsupportedOperationException()
        return AndroidImage(bmp)
    }
}

class AndroidCanvasTestView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val canvas = AndroidCanvas()

    override fun onDraw(canvas: android.graphics.Canvas) {
        this.canvas.context = context
        this.canvas.innerCanvas = canvas
        this.canvas.density = resources.displayMetrics.density.toDouble()
        this.canvas.drawTestImage()
    }
}
