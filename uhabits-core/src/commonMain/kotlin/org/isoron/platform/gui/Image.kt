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

import kotlin.math.abs
import kotlin.math.min

interface Image {
    val width: Int
    val height: Int

    fun getPixel(x: Int, y: Int): Color
    fun setPixel(x: Int, y: Int, color: Color)

    suspend fun export(path: String)

    fun diff(other: Image) {
        if (width != other.width) error("Width must match: $width !== ${other.width}")
        if (height != other.height) error("Height must match: $height !== ${other.height}")

        for (x in 0 until width) {
            for (y in 0 until height) {
                val p1 = getPixel(x, y)
                var l = 1.0
                for (dx in -2..2) {
                    if (x + dx < 0 || x + dx >= width) continue
                    for (dy in -2..2) {
                        if (y + dy < 0 || y + dy >= height) continue
                        val p2 = other.getPixel(x + dx, y + dy)
                        l = min(l, abs(p1.luminosity - p2.luminosity))
                    }
                }
                setPixel(x, y, Color(l, l, l, 1.0))
            }
        }
    }

    val averageLuminosity: Double
        get() {
            var luminosity = 0.0
            for (x in 0 until width) {
                for (y in 0 until height) {
                    luminosity += getPixel(x, y).luminosity
                }
            }
            return luminosity / (width * height)
        }
}
