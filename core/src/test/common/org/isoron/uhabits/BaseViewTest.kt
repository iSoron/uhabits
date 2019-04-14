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

import org.isoron.*
import org.isoron.platform.gui.*
import org.isoron.uhabits.components.*
import kotlin.test.*

var SIMILARITY_THRESHOLD = 5.0

open class BaseViewTest {
    var theme = LightTheme()
    suspend fun assertRenders(width: Int,
                              height: Int,
                              expectedPath: String,
                              component: Component) {

        val helper = DependencyResolver.getCanvasHelper()
        val canvas = helper.createCanvas(width, height)
        component.draw(canvas)
        assertRenders(expectedPath, canvas)
    }

    suspend fun assertRenders(expectedPath: String,
                              canvas: Canvas) {

        val helper = DependencyResolver.getCanvasHelper()
        val fileOpener = DependencyResolver.getFileOpener()
        val expectedFile = fileOpener.openResourceFile(expectedPath)
        val actualPath = "/failed/${expectedPath}"

        if (expectedFile.exists()) {
            val d = helper.compare(expectedFile, canvas)
            if (d >= SIMILARITY_THRESHOLD) {
                helper.exportCanvas(canvas, actualPath)
                val expectedCopy = expectedPath.replace(".png", ".expected.png")
                expectedFile.copyTo(fileOpener.openUserFile("/failed/$expectedCopy"))
                fail("Images differ (distance=${d}). Actual rendered saved to ${actualPath}.")
            }
        } else {
            helper.exportCanvas(canvas, actualPath)
            fail("Expected file is missing. Actual render saved to $actualPath")
        }
    }
}