/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.androidbase.utils

import android.graphics.Color
import kotlin.math.max

object ColorUtils {
    private const val ALPHA_CHANNEL = 24
    private const val RED_CHANNEL = 16
    private const val GREEN_CHANNEL = 8
    private const val BLUE_CHANNEL = 0

    @JvmStatic
    fun mixColors(color1: Int, color2: Int, amount: Float): Int {
        val a = getComponent(color1, color2, amount, ALPHA_CHANNEL)
        val r = getComponent(color1, color2, amount, RED_CHANNEL)
        val g = getComponent(color1, color2, amount, GREEN_CHANNEL)
        val b = getComponent(color1, color2, amount, BLUE_CHANNEL)
        return a or r or g or b
    }

    @JvmStatic
    fun setAlpha(color: Int, newAlpha: Float): Int {
        val intAlpha = (newAlpha * 255).toInt()
        return Color.argb(intAlpha, Color.red(color), Color.green(color),
                Color.blue(color))
    }

    @JvmStatic
    fun setMinValue(color: Int, newValue: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = max(hsv[2], newValue)
        return Color.HSVToColor(hsv)
    }

    private fun getComponent(color1: Int, color2: Int, amount: Float, channel: Int): Int {
        val inverseAmount = 1.0f - amount
        return ((color1 shr channel and 0xff).toFloat() * amount +
                (color2 shr channel and 0xff).toFloat() * inverseAmount).toInt() and 0xff shl channel
    }
}