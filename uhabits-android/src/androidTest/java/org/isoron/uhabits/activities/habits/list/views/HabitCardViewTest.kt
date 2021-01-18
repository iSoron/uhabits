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

package org.isoron.uhabits.activities.habits.list.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HabitCardViewTest : BaseViewTest() {

    val PATH = "habits/list/HabitCardView"
    private lateinit var view: HabitCardView
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit
    private lateinit var today: Timestamp

    override fun setUp() {
        super.setUp()
        setTheme(R.style.AppBaseTheme)

        habit1 = fixtures.createLongHabit()
        habit2 = fixtures.createLongNumericalHabit()
        today = DateUtils.getTodayWithOffset()

        val entries = habit1
            .computedEntries
            .getByInterval(today.minus(300), today)
            .map { it.value }.toIntArray()

        view = component.getHabitCardViewFactory().create().apply {
            habit = habit1
            values = entries
            score = habit1.scores[today].value
            isSelected = false
            buttonCount = 5
        }
        latch.countDown()

        latch.await()
        measureView(view, dpToPixels(400), dpToPixels(50))
    }

    @Test
    fun testRender() {
        assertRenders(view, "$PATH/render.png")
    }

    @Test
    fun testRender_selected() {
        view.isSelected = true
        measureView(view, dpToPixels(400), dpToPixels(50))
        assertRenders(view, "$PATH/render_selected.png")
    }

    @Test
    fun testRender_numerical() {
        val entries = habit2
            .computedEntries
            .getByInterval(today.minus(300), today)
            .map { it.value }.toIntArray()

        view.apply {
            habit = habit2
            values = entries
        }
        assertRenders(view, "$PATH/render_numerical.png")
    }

    @Test
    fun testChangeModel() {
        habit1.name = "Wake up early"
        habit1.color = PaletteColor(2)
        habit1.observable.notifyListeners()
        Thread.sleep(500)
        assertRenders(view, "$PATH/render_changed.png")
    }
}
