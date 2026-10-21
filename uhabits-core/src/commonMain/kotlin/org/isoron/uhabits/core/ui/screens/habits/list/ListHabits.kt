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
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.Theme

data class ListHabitsState(
    val items: List<HabitCardState>,
    val hasHabits: Boolean,
    val primaryOrder: Order,
    val secondaryOrder: Order,
    val isSortable: Boolean,
    val checkmarkCount: Int,
    val theme: Theme
)

object ListHabitsPresenter {
    fun buildState(
        allHabits: HabitList,
        filter: HabitMatcher?,
        primaryOrder: Order,
        secondaryOrder: Order,
        checkmarkCount: Int,
        theme: Theme
    ): ListHabitsState {
        val view = allHabits.getFiltered(filter)
        view.primaryOrder = primaryOrder
        view.secondaryOrder = secondaryOrder

        val today = getToday()
        val dateFrom = today.minus(checkmarkCount - 1)
        val items = mutableListOf<HabitCardState>()
        for (habit in view) {
            val id = habit.id ?: continue
            val score = habit.scores[today].value
            val entries = habit.computedEntries.getByInterval(dateFrom, today)
            items.add(
                HabitCardState(
                    id = id,
                    name = habit.name,
                    color = habit.color,
                    unit = habit.unit,
                    isNumerical = habit.isNumerical,
                    archived = habit.isArchived,
                    score = score,
                    checkmarks = entries.map { it.value },
                    completed = entries.map { it.completed },
                    notes = entries.map { it.notes }
                )
            )
        }
        return ListHabitsState(
            items = items,
            hasHabits = !allHabits.isEmpty,
            primaryOrder = primaryOrder,
            secondaryOrder = secondaryOrder,
            isSortable = primaryOrder == Order.BY_POSITION,
            checkmarkCount = checkmarkCount,
            theme = theme
        )
    }
}

data class HabitCardState(
    val id: Long,
    val name: String,
    val color: PaletteColor,
    val unit: String,
    val isNumerical: Boolean,
    val archived: Boolean,
    val score: Double,
    val checkmarks: List<Int>,
    val completed: List<Boolean>,
    val notes: List<String>
)
