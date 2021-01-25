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
class StreakChartTest : BaseViewTest() {
    private lateinit var view: StreakChart
    @Before
    override fun setUp() {
        super.setUp()
        fixtures.purgeHabits(habitList)
        val habit = fixtures.createLongHabit()
        view = StreakChart(targetContext).apply {
            setColor(habit.color.toFixedAndroidColor())
            setStreaks(habit.streaks.getBest(5))
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
    fun testRender_withSmallSize() {
        measureView(view, dpToPixels(100), dpToPixels(100))
        assertRenders(view, BASE_PATH + "renderSmallSize.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withTransparentBackground() {
        view.setIsBackgroundTransparent(true)
        assertRenders(view, BASE_PATH + "renderTransparent.png")
    }

    companion object {
        private const val BASE_PATH = "common/StreakChart/"
    }
}
