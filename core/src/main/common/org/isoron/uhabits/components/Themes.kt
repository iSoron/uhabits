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

package org.isoron.uhabits.components

import org.isoron.platform.gui.*

abstract class Theme {
    val toolbarColor = Color(0xffffff)

    val lowContrastTextColor = Color(0xe0e0e0)
    val mediumContrastTextColor = Color(0x808080)
    val highContrastTextColor = Color(0x202020)

    val cardBackgroundColor = Color(0xFFFFFF)
    val appBackgroundColor = Color(0xf4f4f4)
    val toolbarBackgroundColor = Color(0xf4f4f4)
    val statusBarBackgroundColor = Color(0x333333)

    val headerBackgroundColor = Color(0xeeeeee)
    val headerBorderColor = Color(0xcccccc)
    val headerTextColor = mediumContrastTextColor

    val itemBackgroundColor = Color(0xffffff)

    fun color(paletteIndex: Int): Color {
        return when (paletteIndex) {
            0 -> Color(0xD32F2F)
            1 -> Color(0x512DA8)
            2 -> Color(0xF57C00)
            3 -> Color(0xFF8F00)
            4 -> Color(0xF9A825)
            5 -> Color(0xAFB42B)
            6 -> Color(0x7CB342)
            7 -> Color(0x388E3C)
            8 -> Color(0x00897B)
            9 -> Color(0x00ACC1)
            10 -> Color(0x039BE5)
            11 -> Color(0x1976D2)
            12 -> Color(0x303F9F)
            13 -> Color(0x5E35B1)
            14 -> Color(0x8E24AA)
            15 -> Color(0xD81B60)
            16 -> Color(0x5D4037)
            else -> Color(0x000000)
        }
    }

    val checkmarkButtonSize = 48.0
    val smallTextSize = 12.0
    val regularTextSize = 17.0
}

class LightTheme : Theme()