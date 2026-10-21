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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.LightTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ListHabitsPresenterTest : BaseUnitTest() {
    private val theme = LightTheme()
    private val checkmarkCount = 3

    @Test
    fun testBuildState() {
        addHabit("Beta", PaletteColor(1), position = 0, completedOffsets = intArrayOf(0))
        addHabit("Alpha", PaletteColor(1), position = 1, completedOffsets = intArrayOf(1))
        addHabit("Gamma", PaletteColor(0), position = 2, completedOffsets = intArrayOf(0))
        addHabit("Delta", PaletteColor(0), position = 3, archived = true)
        addHabit("Yoga", PaletteColor(2), position = 4, numerical = true)
        addHabit("Zeta", PaletteColor(2), position = 5)
        addHabit("Reading", PaletteColor(3), position = 6, category = "Books")
        addHabit("Meditation", PaletteColor(4), position = 7, category = "Mind")

        val state = ListHabitsPresenter.buildState(
            allHabits = habitList,
            filter = HabitMatcher(searchQuery = "a"),
            primaryOrder = Order.BY_COLOR_ASC,
            secondaryOrder = Order.BY_NAME_ASC,
            checkmarkCount = checkmarkCount,
            theme = theme
        )

        val expected = ListHabitsState(
            items = listOf(
                HabitCardState(
                    id = 2,
                    name = "Gamma",
                    color = PaletteColor(0),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.05192248566082858,
                    checkmarks = listOf(2, -1, -1),
                    completed = listOf(true, false, false),
                    notes = listOf("", "", ""),
                    category = ""
                ),
                HabitCardState(
                    id = 1,
                    name = "Alpha",
                    color = PaletteColor(1),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.04922654114362963,
                    checkmarks = listOf(-1, 2, -1),
                    completed = listOf(false, true, false),
                    notes = listOf("", "", ""),
                    category = ""
                ),
                HabitCardState(
                    id = 0,
                    name = "Beta",
                    color = PaletteColor(1),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.05192248566082858,
                    checkmarks = listOf(2, -1, -1),
                    completed = listOf(true, false, false),
                    notes = listOf("", "", ""),
                    category = ""
                ),
                HabitCardState(
                    id = 4,
                    name = "Yoga",
                    color = PaletteColor(2),
                    unit = "miles",
                    isNumerical = true,
                    archived = false,
                    score = 0.06464757443715247,
                    checkmarks = listOf(100, 200, -1),
                    completed = listOf(false, false, false),
                    notes = listOf("", "", ""),
                    category = ""
                ),
                HabitCardState(
                    id = 5,
                    name = "Zeta",
                    color = PaletteColor(2),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.0,
                    checkmarks = listOf(-1, -1, -1),
                    completed = listOf(false, false, false),
                    notes = listOf("", "", ""),
                    category = ""
                ),
                HabitCardState(
                    id = 6,
                    name = "Reading",
                    color = PaletteColor(3),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.0,
                    checkmarks = listOf(-1, -1, -1),
                    completed = listOf(false, false, false),
                    notes = listOf("", "", ""),
                    category = "Books"
                ),
                HabitCardState(
                    id = 7,
                    name = "Meditation",
                    color = PaletteColor(4),
                    unit = "",
                    isNumerical = false,
                    archived = false,
                    score = 0.0,
                    checkmarks = listOf(-1, -1, -1),
                    completed = listOf(false, false, false),
                    notes = listOf("", "", ""),
                    category = "Mind"
                )
            ),
            hasHabits = true,
            primaryOrder = Order.BY_COLOR_ASC,
            secondaryOrder = Order.BY_NAME_ASC,
            isSortable = false,
            checkmarkCount = checkmarkCount,
            theme = theme
        )

        assertEquals(expected, state)
    }

    private fun addHabit(
        name: String,
        color: PaletteColor,
        position: Int,
        category: String = "",
        archived: Boolean = false,
        numerical: Boolean = false,
        completedOffsets: IntArray = intArrayOf()
    ): Habit {
        val habit = if (numerical) fixtures.createNumericalHabit() else fixtures.createEmptyHabit()
        habit.name = name
        habit.color = color
        habit.position = position
        habit.category = category
        habit.isArchived = archived
        val today = getToday()
        for (offset in completedOffsets) {
            habit.originalEntries.add(Entry(today.minus(offset), Entry.YES_MANUAL))
        }
        habit.recompute()
        habitList.add(habit)
        return habit
    }
}
