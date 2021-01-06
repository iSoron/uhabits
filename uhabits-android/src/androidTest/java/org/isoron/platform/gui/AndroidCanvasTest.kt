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

import android.graphics.Bitmap
import org.isoron.uhabits.BaseViewTest
import org.junit.Test

class AndroidCanvasTest : BaseViewTest() {
    @Test
    fun testDrawTestImage() {
        similarityCutoff = 0.0005
        val bmp = Bitmap.createBitmap(1000, 800, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas()
        canvas.context = testContext
        canvas.innerDensity = 2.0
        canvas.innerCanvas = android.graphics.Canvas(bmp)
        canvas.innerBitmap = bmp
        canvas.drawTestImage()
        assertRenders(bmp, "CanvasTest.png")
    }
}
