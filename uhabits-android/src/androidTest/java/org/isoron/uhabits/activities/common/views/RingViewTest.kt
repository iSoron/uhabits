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
package org.isoron.uhabits.activities.common.views

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@MediumTest
class RingViewTest : BaseViewTest() {
    private lateinit var view: RingView

    @Before
    override fun setUp() {
        super.setUp()
        view = RingView(targetContext).apply {
            setPercentage(0.6f)
            setText("60%")
            setColor(getAndroidTestColor(0))
            setBackgroundColor(Color.WHITE)
            setThickness(dpToPixels(3))
        }
    }

    @Test
    @Throws(IOException::class)
    fun testRender_base() {
        measureView(view, dpToPixels(100), dpToPixels(100))
        assertRenders(view, BASE_PATH + "render.png")
    }

    @Test
    @Throws(IOException::class)
    fun testRender_withDifferentParams() {
        view.setPercentage(0.25f)
        view.setColor(getAndroidTestColor(5))
        measureView(view, dpToPixels(200), dpToPixels(200))
        assertRenders(view, BASE_PATH + "renderDifferentParams.png")
    }

    companion object {
        private const val BASE_PATH = "common/RingView/"
    }
}
