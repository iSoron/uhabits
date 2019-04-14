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

import platform.CoreGraphics.*
import platform.UIKit.*

val Color.uicolor: UIColor
    get() = UIColor.colorWithRed(this.red, this.green, this.blue, this.alpha)

val Color.cgcolor: CGColorRef?
    get() = uicolor.CGColor

class IosCanvas() : Canvas {
    val ctx = UIGraphicsGetCurrentContext()
    var textColor = UIColor.blackColor

    override fun setColor(color: Color) {
        CGContextSetStrokeColorWithColor(ctx, color.cgcolor)
        CGContextSetFillColorWithColor(ctx, color.cgcolor)
        textColor = color.uicolor
    }

    override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
    }

    override fun drawText(text: String, x: Double, y: Double) {
    }

    override fun fillRect(x: Double, y: Double, width: Double, height: Double) {
    }

    override fun drawRect(x: Double, y: Double, width: Double, height: Double) {
    }

    override fun getHeight(): Double {
        return 0.0
    }

    override fun getWidth(): Double {
        return 0.0
    }

    override fun setFont(font: Font) {
    }

    override fun setFontSize(size: Double) {
    }

    override fun setStrokeWidth(size: Double) {
    }

    override fun fillArc(centerX: Double,
                         centerY: Double,
                         radius: Double,
                         startAngle: Double,
                         swipeAngle: Double) {
    }

    override fun fillCircle(centerX: Double, centerY: Double, radius: Double) {
    }

    override fun setTextAlign(align: TextAlign) {
    }
}