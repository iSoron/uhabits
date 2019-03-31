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

package org.isoron.uhabits

import org.isoron.platform.concurrency.*
import org.isoron.platform.gui.*
import org.isoron.platform.io.*
import org.isoron.platform.time.*
import org.isoron.uhabits.components.*
import org.junit.*
import java.awt.image.*
import java.io.*
import javax.imageio.*
import kotlin.math.*

open class BaseTest {

    val fileOpener = JavaFileOpener()

    val log = StandardLog()

    val databaseOpener = JavaDatabaseOpener(log)

    val taskRunner = SequentialTaskRunner()

    lateinit var db: Database

    @Before
    open fun setUp() {
        val dbFile = fileOpener.openUserFile("test.sqlite3")
        if (dbFile.exists()) dbFile.delete()
        db = databaseOpener.open(dbFile)
        db.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
    }
}

open class BaseViewTest {
    val theme = LightTheme()
    fun distance(actual: BufferedImage,
                         expected: BufferedImage): Double {

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

        return 255 * distance / (actual.width * actual.height)
    }

    fun assertRenders(width: Int,
                      height: Int,
                      expectedPath: String,
                      component: Component,
                      threshold: Double = 1e-3) {
        val actual = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val canvas = JavaCanvas(actual.createGraphics(), width, height)
        val expectedFile: JavaResourceFile
        val actualPath = "/tmp/${expectedPath}"

        component.draw(canvas)
        try {
            expectedFile = JavaFileOpener().openResourceFile(expectedPath) as JavaResourceFile
        } catch(e: RuntimeException) {
            File(actualPath).parentFile.mkdirs()
            ImageIO.write(actual, "png", File(actualPath))
            //fail("Expected file is missing. Actual render saved to $actualPath")
            return
        }

        val expected = ImageIO.read(expectedFile.stream())
        val d = distance(actual, expected)
        if (d >= threshold) {
            File(actualPath).parentFile.mkdirs()
            ImageIO.write(actual, "png", File(actualPath))
            ImageIO.write(expected, "png", File(actualPath.replace(".png", ".expected.png")))
            //fail("Images differ (distance=${d}). Actual rendered saved to ${actualPath}.")
        }
    }
}