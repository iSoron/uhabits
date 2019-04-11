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

import kotlinx.coroutines.*
import org.isoron.platform.gui.*
import org.isoron.platform.io.*
import org.isoron.uhabits.*
import java.awt.image.*
import java.io.*
import javax.imageio.*

actual class DependencyResolver actual constructor() {

    val log = StandardLog()
    val fileOpener = JavaFileOpener()
    val databaseOpener = JavaDatabaseOpener(log)

    actual suspend fun getFileOpener(): FileOpener = fileOpener

    actual suspend fun getDatabase(): Database {
        val dbFile = fileOpener.openUserFile("test.sqlite3")
        if (dbFile.exists()) dbFile.delete()
        val db = databaseOpener.open(dbFile)
        db.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
        return db
    }

    actual fun createCanvas(width: Int, height: Int): Canvas {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        return JavaCanvas(image, pixelScale = 1.0)
    }

    actual fun exportCanvas(canvas: Canvas, filename: String) {
        val javaCanvas = canvas as JavaCanvas
        ImageIO.write(javaCanvas.image, "png", File("/tmp/$filename"))
    }
}