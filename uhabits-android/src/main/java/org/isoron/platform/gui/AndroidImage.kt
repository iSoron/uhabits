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

import android.graphics.Bitmap
import kotlin.math.roundToInt

class AndroidImage(private val bmp: Bitmap) : Image {
    override val width: Int
        get() = bmp.width

    override val height: Int
        get() = bmp.height

    override fun getPixel(x: Int, y: Int): Color {
        return Color(bmp.getPixel(x, y))
    }

    override fun setPixel(x: Int, y: Int, color: Color) {
        bmp.setPixel(x, y, color.toInt())
    }

    override suspend fun export(path: String) {
        TODO("Not yet implemented")
    }
}

fun Color.toInt(): Int {
    return android.graphics.Color.argb(
        (255 * this.alpha).roundToInt(),
        (255 * this.red).roundToInt(),
        (255 * this.green).roundToInt(),
        (255 * this.blue).roundToInt(),
    )
}
