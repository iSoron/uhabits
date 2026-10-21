/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardState
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HabitCardViewTest : BaseViewTest() {

    val PATH = "habits/list/HabitCardView"
    private lateinit var view: HabitCardView
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit
    private lateinit var today: LocalDate

    override fun setUp() {
        super.setUp()
        setTheme(R.style.AppBaseTheme)

        habit1 = fixtures.createLongHabit()
        habit2 = fixtures.createLongNumericalHabit()
        today = getToday()

        view = component.getHabitCardViewFactory().create()
        view.setState(buildState(habit1, false))
        latch.countDown()

        latch.await()
        measureView(view, dpToPixels(400), dpToPixels(50))
    }

    private fun buildState(habit: Habit, isSelected: Boolean): HabitCardViewState {
        val entries = habit.computedEntries.getByInterval(today.minus(300), today)
        val record = HabitCardState(
            id = habit.id!!,
            name = habit.name,
            color = habit.color,
            unit = habit.unit,
            isNumerical = habit.isNumerical,
            archived = habit.isArchived,
            score = habit.scores[today].value,
            checkmarks = entries.map { it.value }.toList(),
            completed = entries.map { it.completed }.toList(),
            notes = entries.map { it.notes }.toList()
        )
        return HabitCardViewState(record, isSelected, 5)
    }

    @Test
    fun testRender() {
        assertRenders(view, "$PATH/render.png")
    }

    @Test
    fun testRender_archived() {
        val state = buildState(habit1, false)
        view.setState(state.copy(record = state.record.copy(archived = true)))
        measureView(view, dpToPixels(400), dpToPixels(50))
        assertRenders(view, "$PATH/render_archived.png")
    }

    @Test
    fun testSetState_idempotent() {
        val before = view.checkmarkPanel.buttons.toList()
        view.setState(buildState(habit1, false))
        assertThat(view.checkmarkPanel.buttons.size, equalTo(before.size))
        for (i in before.indices) {
            assertThat(view.checkmarkPanel.buttons[i], sameInstance(before[i]))
        }
    }

    @Test
    fun testRender_selected() {
        view.isSelected = true
        measureView(view, dpToPixels(400), dpToPixels(50))
        assertRenders(view, "$PATH/render_selected.png")
    }

    @Test
    fun testRender_numerical() {
        view.setState(buildState(habit2, false))
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
