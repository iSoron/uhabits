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

import platform.UIKit.*
import platform.CoreGraphics.*
import platform.Foundation.*

class IosImage(val image: UIImage) : Image {

    override val width: Int
        get() {
            return CGImageGetWidth(image.CGImage).toInt()
        }

    override val height: Int
        get() {
            return CGImageGetHeight(image.CGImage).toInt()
        }

    override fun getPixel(x: Int, y: Int): Color {
        return Color(1.0, 0.0, 0.0, 1.0)
    }

    override fun setPixel(x: Int, y: Int, color: Color) {
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun export(path: String) {
        val tmpPath = "${NSTemporaryDirectory()}/$path"
        val dir = (tmpPath as NSString).stringByDeletingLastPathComponent
        NSFileManager.defaultManager.createDirectoryAtPath(dir, true, null, null)
        val data = UIImagePNGRepresentation(image)!!
        val success = data.writeToFile(tmpPath, true)
        if (!success) throw RuntimeException("could not write to $tmpPath")
        println(tmpPath)
    }
}