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
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.BaseAndroidTest
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardState
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsState
import org.isoron.uhabits.core.ui.views.LightTheme
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HabitCardListAdapterTest : BaseAndroidTest() {

    @Test
    fun testSetStateWhileDragging_coalesces() {
        val adapter = HabitCardListAdapter(habitList, prefs)
        adapter.setState(buildState(listOf(1L, 2L)))
        assertThat(adapter.itemCount, equalTo(2))
        adapter.onDragStart(0)
        adapter.setState(buildState(listOf(1L, 2L, 3L, 4L, 5L)))
        assertThat(adapter.itemCount, equalTo(2))
        assertThat(adapter.getItemId(0), equalTo(1L))
        assertThat(adapter.getItemId(1), equalTo(2L))
        adapter.onDragEnd()
        adapter.flushPending()
        assertThat(adapter.itemCount, equalTo(5))
        assertThat(adapter.getItemId(2), equalTo(3L))
    }

    private fun buildState(ids: List<Long>): ListHabitsState {
        val checkmarkCount = 5
        return ListHabitsState(
            items = ids.map { id ->
                HabitCardState(
                    id = id,
                    name = "Habit $id",
                    color = PaletteColor(7),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.0,
                    checkmarks = List(checkmarkCount) { -1 },
                    completed = List(checkmarkCount) { false },
                    notes = List(checkmarkCount) { "" }
                )
            },
            hasHabits = true,
            primaryOrder = Order.BY_POSITION,
            secondaryOrder = Order.BY_NAME_ASC,
            isSortable = true,
            checkmarkCount = checkmarkCount,
            theme = LightTheme()
        )
    }
}
