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

package org.isoron

import org.isoron.platform.gui.*
import org.isoron.platform.io.*
import org.isoron.platform.time.*
import org.isoron.uhabits.*
import org.w3c.dom.*
import kotlin.browser.*

actual object DependencyResolver {
    actual val ignoreViewTests = false

    var fileOpener: JsFileOpener? = null

    actual suspend fun getFileOpener(): FileOpener {
        if (fileOpener == null) {
            val fs = JsFileStorage()
            fs.init()
            fileOpener = JsFileOpener(fs)
        }
        return fileOpener!!
    }

    actual suspend fun getDatabase(): Database {
        val nativeDB = eval("new SQL.Database()")
        val db = JsDatabase(nativeDB)
        db.migrateTo(LOOP_DATABASE_VERSION, getFileOpener(), StandardLog())
        return db
    }

    actual fun getDateFormatter(locale: Locale): LocalDateFormatter {
        return when (locale) {
            Locale.US -> JsDateFormatter("en-US")
            Locale.JAPAN -> JsDateFormatter("ja-JP")
        }
    }

    actual fun createCanvas(width: Int, height: Int): Canvas {
        val element = document.createElement("canvas") as HTMLCanvasElement
        element.width = 2 * width
        element.height = 2 * height
        element.style.width = "${2 * width}px"
        element.style.height = "${2 * height}px"
        val canvas = JsCanvas(element, 2.0)
        canvas.setColor(Color(0xffffff))
        canvas.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
        return canvas
    }
}
