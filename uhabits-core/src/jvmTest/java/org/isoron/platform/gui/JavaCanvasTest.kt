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

import junit.framework.Assert.fail
import kotlinx.coroutines.runBlocking
import org.isoron.platform.io.JavaFileOpener
import org.junit.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class JavaCanvasTest {
    @Test
    fun run() = runBlocking {
        assertRenders("views/CanvasTest.png", createCanvas(500, 400).apply { drawTestImage() })
    }
}

fun createCanvas(w: Int, h: Int) = JavaCanvas(BufferedImage(2 * w, 2 * h, TYPE_INT_ARGB), 2.0)

suspend fun assertRenders(
    path: String,
    canvas: Canvas
) {
    val actualImage = canvas.toImage()
    val failedActualPath = "/tmp/failed/$path"
    val failedExpectedPath = failedActualPath.replace(
        ".png",
        ".expected.png"
    )
    val failedDiffPath = failedActualPath.replace(".png", ".diff.png")
    val fileOpener = JavaFileOpener()
    val expectedFile = fileOpener.openResourceFile(path)
    if (expectedFile.exists()) {
        val expectedImage = expectedFile.toImage()
        val diffImage = expectedFile.toImage()
        diffImage.diff(actualImage)
        val distance = diffImage.averageLuminosity * 100
        if (distance >= 1.0) {
            expectedImage.export(failedExpectedPath)
            actualImage.export(failedActualPath)
            diffImage.export(failedDiffPath)
            fail("Images differ (distance=$distance)")
        }
    } else {
        actualImage.export(failedActualPath)
        fail("Expected image file is missing. Actual image: $failedActualPath")
    }
}

suspend fun assertRenders(
    width: Int,
    height: Int,
    expectedPath: String,
    view: View,
) {
    val canvas = createCanvas(width, height)
    view.draw(canvas)
    assertRenders(expectedPath, canvas)
}
