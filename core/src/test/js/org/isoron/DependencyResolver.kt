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
import org.khronos.webgl.*
import org.w3c.dom.*
import kotlin.browser.*
import kotlin.math.*

actual object DependencyResolver {
    actual val supportsDatabaseTests = true
    actual val supportsCanvasTests = true
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

    actual fun getCanvasHelper(): CanvasHelper {
        return JsCanvasHelper()
    }

    actual fun getDateFormatter(locale: Locale): LocalDateFormatter {
        return when (locale) {
            Locale.US -> JsDateFormatter("en-US")
            Locale.JAPAN -> JsDateFormatter("ja-JP")
        }
    }
}

class JsCanvasHelper : CanvasHelper {
    override suspend fun compare(imageFile: ResourceFile,
                                 canvas: Canvas): Double {
        canvas as JsCanvas
        imageFile as JsResourceFile
        val width = canvas.element.width
        val height = canvas.element.height

        val expectedCanvasElement = document.createElement("canvas") as HTMLCanvasElement
        expectedCanvasElement.width = width
        expectedCanvasElement.height = height
        expectedCanvasElement.style.width = canvas.element.style.width
        expectedCanvasElement.style.height = canvas.element.style.height
        expectedCanvasElement.className = "canvasTest"
        document.body?.appendChild(expectedCanvasElement)
        val expectedCanvas = JsCanvas(expectedCanvasElement, 1.0)
        expectedCanvas.loadImage("../assets/${imageFile.filename}")

        val actualData = canvas.ctx.getImageData(0.0,
                                                 0.0,
                                                 width.toDouble(),
                                                 height.toDouble()).data
        val expectedData = expectedCanvas.ctx.getImageData(0.0,
                                                           0.0,
                                                           width.toDouble(),
                                                           height.toDouble()).data

        var distance = 0.0;
        for (x in 0 until width) {
            for (y in 0 until height) {
                val k = (y * width + x) * 4
                distance += abs(actualData[k] - expectedData[k])
                distance += abs(actualData[k + 1] - expectedData[k + 1])
                distance += abs(actualData[k + 2] - expectedData[k + 2])
                distance += abs(actualData[k + 3] - expectedData[k + 3])
            }
        }

        val adjustedDistance = distance / 255.0 / 4 / 1000

        if (adjustedDistance > SIMILARITY_THRESHOLD) {
            expectedCanvasElement.style.display = "block"
            canvas.element.style.display = "block"
        }

        return adjustedDistance
    }

    override fun createCanvas(width: Int, height: Int): Canvas {
        val canvasElement = document.createElement("canvas") as HTMLCanvasElement
        canvasElement.width = width * 2
        canvasElement.height = height * 2
        canvasElement.style.width = "${width}px"
        canvasElement.style.height = "${height}px"
        canvasElement.className = "canvasTest"
        document.body?.appendChild(canvasElement)
        return JsCanvas(canvasElement, 2.0)
    }

    override suspend fun exportCanvas(canvas: Canvas, filename: String) {
        // do nothing
    }
}