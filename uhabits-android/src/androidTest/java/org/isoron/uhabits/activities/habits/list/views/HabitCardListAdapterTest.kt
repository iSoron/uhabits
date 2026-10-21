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
import org.hamcrest.CoreMatchers.nullValue
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
    fun testSetState_buildsRowsWithCategoryHeaders() {
        val adapter = HabitCardListAdapter(habitList, prefs)

        adapter.setState(
            ListHabitsState(
                items = emptyList(),
                hasHabits = true,
                primaryOrder = Order.BY_POSITION,
                secondaryOrder = Order.BY_NAME_ASC,
                isSortable = true,
                checkmarkCount = 0,
                theme = LightTheme()
            )
        )
        assertThat(adapter.itemCount, equalTo(0))

        adapter.setState(
            ListHabitsState(
                items = listOf(
                    HabitCardState(
                        id = 1L, name = "Habit 1", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Health"
                    ),
                    HabitCardState(
                        id = 2L, name = "Habit 2", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = ""
                    ),
                    HabitCardState(
                        id = 3L, name = "Habit 3", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Health"
                    ),
                    HabitCardState(
                        id = 4L, name = "Habit 4", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Work"
                    )
                ),
                hasHabits = true,
                primaryOrder = Order.BY_POSITION,
                secondaryOrder = Order.BY_NAME_ASC,
                isSortable = true,
                checkmarkCount = 0,
                theme = LightTheme()
            )
        )
        assertThat(adapter.itemCount, equalTo(6))
        assertThat(adapter.getItemViewType(0), equalTo(HabitCardListAdapter.VIEW_TYPE_CATEGORY))
        assertThat(adapter.getItemId(0), equalTo(1L shl 62))
        assertThat(adapter.getItemViewType(1), equalTo(HabitCardListAdapter.VIEW_TYPE_HABIT))
        assertThat(adapter.getItemId(1), equalTo(1L))
        assertThat(adapter.getItemViewType(2), equalTo(HabitCardListAdapter.VIEW_TYPE_HABIT))
        assertThat(adapter.getItemId(2), equalTo(2L))
        assertThat(adapter.getItemViewType(3), equalTo(HabitCardListAdapter.VIEW_TYPE_HABIT))
        assertThat(adapter.getItemId(3), equalTo(3L))
        assertThat(adapter.getItemViewType(4), equalTo(HabitCardListAdapter.VIEW_TYPE_CATEGORY))
        assertThat(adapter.getItemId(4), equalTo((1L shl 62) + 1))
        assertThat(adapter.getItemViewType(5), equalTo(HabitCardListAdapter.VIEW_TYPE_HABIT))
        assertThat(adapter.getItemId(5), equalTo(4L))
    }

    @Test
    fun testDrag_reordersRowsAndReportsTarget() {
        val adapter = HabitCardListAdapter(habitList, prefs)

        adapter.setState(
            ListHabitsState(
                items = listOf(
                    HabitCardState(
                        id = 1L, name = "Habit 1", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Health"
                    ),
                    HabitCardState(
                        id = 2L, name = "Habit 2", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Health"
                    ),
                    HabitCardState(
                        id = 3L, name = "Habit 3", color = PaletteColor(7), unit = "",
                        isNumerical = false, archived = false, score = 0.0,
                        checkmarks = emptyList(), completed = emptyList(), notes = emptyList(),
                        category = "Work"
                    )
                ),
                hasHabits = true,
                primaryOrder = Order.BY_POSITION,
                secondaryOrder = Order.BY_NAME_ASC,
                isSortable = true,
                checkmarkCount = 0,
                theme = LightTheme()
            )
        )
        adapter.onDragStart(0)
        assertThat(adapter.onDragEnd(), nullValue())

        adapter.onDragStart(1)
        adapter.onDragMove(1, 2)
        assertThat(adapter.onDragEnd(), equalTo(ReorderDragResult(1L, 2L, false, "Health")))

        adapter.onDragStart(2)
        adapter.onDragMove(2, 3)
        assertThat(adapter.itemCount, equalTo(5))
        assertThat(adapter.getItemViewType(0), equalTo(HabitCardListAdapter.VIEW_TYPE_CATEGORY))
        assertThat(adapter.getItemId(0), equalTo(1L shl 62))
        assertThat(adapter.getItemViewType(2), equalTo(HabitCardListAdapter.VIEW_TYPE_CATEGORY))
        assertThat(adapter.getItemId(2), equalTo((1L shl 62) + 1))
        assertThat(adapter.getItemId(3), equalTo(1L))
        assertThat(adapter.onDragEnd(), equalTo(ReorderDragResult(1L, 3L, true, "Work")))

        adapter.onDragStart(3)
        adapter.onDragMove(3, 4)
        assertThat(adapter.onDragEnd(), equalTo(ReorderDragResult(1L, 3L, false, "Work")))
    }
}
