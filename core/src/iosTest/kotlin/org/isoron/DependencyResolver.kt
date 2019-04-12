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

@file:Suppress("UNCHECKED_CAST")

package org.isoron

import org.isoron.platform.gui.*
import org.isoron.platform.io.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

actual class DependencyResolver {
    actual suspend fun getFileOpener(): FileOpener {
        return IosFileOpener()
    }

    actual suspend fun getDatabase(): Database = TODO()

    actual fun createCanvas(width: Int, height: Int): Canvas {
        UIGraphicsBeginImageContext(CGSizeMake(width=500.0, height=600.0))
        return IosCanvas()
    }

    actual fun exportCanvas(canvas: Canvas, filename: String): Unit {
        val image = UIGraphicsGetImageFromCurrentImageContext()!!
        val manager = NSFileManager.defaultManager
        val paths = manager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask) as List<NSURL>
        val filePath = paths.first().URLByAppendingPathComponent("IosCanvasTest.png")!!.path!!
        val data = UIImagePNGRepresentation(image)!!
        data.writeToFile(filePath, false)
    }
}