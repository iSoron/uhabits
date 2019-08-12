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

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import kotlin.math.*

val Color.uicolor: UIColor
    get() = UIColor.colorWithRed(this.red, this.green, this.blue, this.alpha)

val Color.cgcolor: CGColorRef?
    get() = uicolor.CGColor

class IosCanvas(val width: Double,
                val height: Double,
                val scale: Double = 2.0
               ) : Canvas {

    var textColor = UIColor.blackColor
    var font = Font.REGULAR
    var fontSize = 12.0
    var textAlign = TextAlign.CENTER
    val ctx = UIGraphicsGetCurrentContext()!!

    override fun setColor(color: Color) {
        CGContextSetStrokeColorWithColor(ctx, color.cgcolor)
        CGContextSetFillColorWithColor(ctx, color.cgcolor)
        textColor = color.uicolor
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
        CGContextMoveToPoint(ctx, x1 * scale, y1 * scale)
        CGContextAddLineToPoint(ctx, x2 * scale, y2 * scale)
        CGContextStrokePath(ctx)
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun drawText(text: String, x: Double, y: Double) {
        val sx = scale * x
        val sy = scale * y
        val nsText = (text as NSString)
        val uiFont = when (font) {
            Font.REGULAR -> UIFont.systemFontOfSize(fontSize)
            Font.BOLD -> UIFont.boldSystemFontOfSize(fontSize)
            Font.FONT_AWESOME -> UIFont.fontWithName("FontAwesome", fontSize)
        }
        val size = nsText.sizeWithFont(uiFont)
        val width = size.useContents { width }
        val height = size.useContents { height }
        val origin = when (textAlign) {
            TextAlign.CENTER -> CGPointMake(sx - width / 2, sy - height / 2)
            TextAlign.LEFT -> CGPointMake(sx, sy - height / 2)
            TextAlign.RIGHT -> CGPointMake(sx - width, sy - height / 2)
        }
        nsText.drawAtPoint(origin, uiFont)
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
        CGContextFillRect(ctx,
                          CGRectMake(x * scale,
                                     y * scale,
                                     width * scale,
                                     height * scale))
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
        CGContextStrokeRect(ctx,
                            CGRectMake(x * scale,
                                       y * scale,
                                       width * scale,
                                       height * scale))
    }

    override fun getHeight(): Double {
        return height
    }

    override fun getWidth(): Double {
        return width
    }

    override fun setFont(font: Font) {
        this.font = font
    }

    override fun setFontSize(size: Double) {
        this.fontSize = size * scale
    }

    override fun setStrokeWidth(size: Double) {
        CGContextSetLineWidth(ctx, size * scale)
    }

    override fun fillArc(centerX: Double,
                         centerY: Double,
                         radius: Double,
                         startAngle: Double,
                         swipeAngle: Double) {
        val a1 = startAngle / 180 * PI * (-1)
        val a2 = a1 - swipeAngle / 180 * PI
        CGContextBeginPath(ctx)
        CGContextMoveToPoint(ctx, centerX * scale, centerY * scale)
        CGContextAddArc(ctx,
                        centerX * scale,
                        centerY * scale,
                        radius * scale,
                        a1,
                        a2,
                        if (swipeAngle > 0) 1 else 0)
        CGContextClosePath(ctx)
        CGContextFillPath(ctx)
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
        val rect = CGRectMake(scale * (centerX - radius),
                              scale * (centerY - radius),
                              scale * radius * 2.0,
                              scale * radius * 2.0)
        CGContextFillEllipseInRect(ctx, rect)
    }

    override fun setTextAlign(align: TextAlign) {
        this.textAlign = align
    }

    override fun toImage(): Image {
        return IosImage(UIGraphicsGetImageFromCurrentImageContext()!!)
    }
}
