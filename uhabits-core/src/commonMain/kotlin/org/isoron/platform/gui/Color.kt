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

data class Color(
    val red: Double,
    val green: Double,
    val blue: Double,
    val alpha: Double,
) {
    val luminosity: Double
        get() {
            return 0.21 * red + 0.72 * green + 0.07 * blue
        }

    constructor(rgb: Int) : this(
        ((rgb shr 16) and 0xFF) / 255.0,
        ((rgb shr 8) and 0xFF) / 255.0,
        ((rgb shr 0) and 0xFF) / 255.0,
        1.0
    )

    fun blendWith(other: Color, weight: Double): Color {
        return Color(
            red * (1 - weight) + other.red * weight,
            green * (1 - weight) + other.green * weight,
            blue * (1 - weight) + other.blue * weight,
            alpha * (1 - weight) + other.alpha * weight
        )
    }

    fun contrast(other: Color): Double {
        val l1 = this.luminosity
        val l2 = other.luminosity
        val relativeLuminosity = (l1 + 0.05) / (l2 + 0.05)
        return if (relativeLuminosity >= 1) relativeLuminosity else 1 / relativeLuminosity
    }

    fun withAlpha(newAlpha: Double) = Color(red, green, blue, newAlpha)

    companion object {
        val TRANSPARENT = Color(0.0, 0.0, 0.0, 0.0)
        val RED = Color(1.0, 0.0, 0.0, 1.0)
        val GREEN = Color(0.0, 1.0, 0.0, 1.0)
        val BLUE = Color(1.0, 0.0, 1.0, 1.0)
        val YELLOW = Color(1.0, 1.0, 0.0, 1.0)
        val MAGENTA = Color(1.0, 0.0, 1.0, 1.0)
        val CYAN = Color(0.0, 1.0, 1.0, 1.0)
        val WHITE = Color(1.0, 1.0, 1.0, 1.0)
        val BLACK = Color(0.0, 0.0, 0.0, 1.0)
    }
}
