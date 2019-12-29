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

import java.awt.image.*
import java.io.*
import javax.imageio.*

class JavaImage(val bufferedImage: BufferedImage) : Image {
    override fun setPixel(x: Int, y: Int, color: Color) {
        bufferedImage.setRGB(x, y, java.awt.Color(color.red.toFloat(),
                                                  color.green.toFloat(),
                                                  color.blue.toFloat()).rgb)
    }

    override suspend fun export(path: String) {
        val file = File(path)
        file.parentFile.mkdirs()
        ImageIO.write(bufferedImage, "png", file)
    }

    override val width: Int
        get() = bufferedImage.width

    override val height: Int
        get() = bufferedImage.height

    override fun getPixel(x: Int, y: Int): Color {
        return Color(bufferedImage.getRGB(x, y))
    }
}