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
import java.awt.image.*
import java.io.*
import java.lang.Math.*
import java.nio.file.*
import javax.imageio.*

actual object DependencyResolver {
    actual val supportsDatabaseTests = true
    actual val supportsCanvasTests = true

    actual suspend fun getFileOpener(): FileOpener = JavaFileOpener()
    actual fun getCanvasHelper(): CanvasHelper = JavaCanvasHelper()

    actual suspend fun getDatabase(): Database {
        val log = StandardLog()
        val fileOpener = JavaFileOpener()
        val databaseOpener = JavaDatabaseOpener(log)

        val dbFile = fileOpener.openUserFile("test.sqlite3")
        if (dbFile.exists()) dbFile.delete()
        val db = databaseOpener.open(dbFile)
        db.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
        return db
    }

    actual fun getDateFormatter(locale: Locale): LocalDateFormatter {
        return when (locale) {
            Locale.US -> JavaLocalDateFormatter(java.util.Locale.US)
            Locale.JAPAN -> JavaLocalDateFormatter(java.util.Locale.JAPAN)
        }
    }
}

class JavaCanvasHelper : CanvasHelper {
    override suspend fun compare(imageFile: ResourceFile, canvas: Canvas): Double {
        val actual = (canvas as JavaCanvas).image
        val expected = ImageIO.read((imageFile as JavaResourceFile).stream())
        return compare(expected, actual)
    }

    private fun compare(expected: BufferedImage,
                        actual: BufferedImage): Double {

        if (actual.width != expected.width) return Double.POSITIVE_INFINITY
        if (actual.height != expected.height) return Double.POSITIVE_INFINITY

        var distance = 0.0;
        for (x in 0 until actual.width) {
            for (y in 0 until actual.height) {
                val p1 = Color(actual.getRGB(x, y))
                val p2 = Color(expected.getRGB(x, y))
                distance += abs(p1.red - p2.red)
                distance += abs(p1.green - p2.green)
                distance += abs(p1.blue - p2.blue)
            }
        }

        return distance / 4.0
    }

    override fun createCanvas(width: Int, height: Int): Canvas {
        val widthPx = width * 2
        val heightPx = height * 2
        val image = BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB)
        return JavaCanvas(image, pixelScale = 2.0)
    }

    override suspend fun exportCanvas(canvas: Canvas, filename: String) {
        val file = File("/tmp/$filename")
        file.parentFile.mkdirs()
        ImageIO.write((canvas as JavaCanvas).image, "png", file)
    }
}