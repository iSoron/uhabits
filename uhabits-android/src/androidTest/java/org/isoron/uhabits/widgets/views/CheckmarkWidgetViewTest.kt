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
package org.isoron.uhabits.widgets.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.isoron.uhabits.utils.PaletteUtils.getAndroidTestColor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@MediumTest
class CheckmarkWidgetViewTest : BaseViewTest() {
    private lateinit var view: CheckmarkWidgetView
    @Before
    override fun setUp() {
        super.setUp()
        setTheme(R.style.WidgetTheme)
        val habit = fixtures.createShortHabit()
        val computedEntries = habit.computedEntries
        val scores = habit.scores
        val today = getTodayWithOffset()
        val score = scores[today].value
        view = CheckmarkWidgetView(targetContext).apply {
            activeColor = getAndroidTestColor(0)
            entryState = computedEntries.get(today).value
            entryValue = computedEntries.get(today).value
            percentage = score.toFloat()
            name = habit.name
        }
        view.refresh()
        measureView(view, dpToPixels(100), dpToPixels(125))
    }

    @Test
    @Throws(IOException::class)
    fun testRender_checked() {
        assertRenders(view, PATH + "checked.png")
    }

    @Test
    @Throws(IOException::class)
    fun testRender_largeSize() {
        measureView(view, dpToPixels(300), dpToPixels(300))
        assertRenders(view, PATH + "large_size.png")
    }

    companion object {
        private const val PATH = "widgets/CheckmarkWidgetView/"
    }
}
