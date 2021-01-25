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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.utils.toFixedAndroidColor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class FrequencyChartTest : BaseViewTest() {
    private lateinit var view: FrequencyChart

    @Before
    override fun setUp() {
        super.setUp()
        fixtures.purgeHabits(habitList)
        val habit = fixtures.createLongHabit()
        view = FrequencyChart(targetContext).apply {
            setFrequency(habit.originalEntries.computeWeekdayFrequency(habit.isNumerical))
            setColor(habit.color.toFixedAndroidColor())
        }
        measureView(view, dpToPixels(300), dpToPixels(100))
    }

    @Test
    @Throws(Throwable::class)
    fun testRender() {
        assertRenders(view, BASE_PATH + "render.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withDataOffset() {
        view.onScroll(null, null, -dpToPixels(150), 0f)
        view.invalidate()
        assertRenders(view, BASE_PATH + "renderDataOffset.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withDifferentSize() {
        measureView(view, dpToPixels(200), dpToPixels(200))
        assertRenders(view, BASE_PATH + "renderDifferentSize.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withTransparentBackground() {
        view.setIsBackgroundTransparent(true)
        assertRenders(view, BASE_PATH + "renderTransparent.png")
    }

    companion object {
        const val BASE_PATH = "common/FrequencyChart/"
    }
}
