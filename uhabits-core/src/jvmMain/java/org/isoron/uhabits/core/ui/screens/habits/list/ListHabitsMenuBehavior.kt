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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import javax.inject.Inject

class ListHabitsMenuBehavior @Inject constructor(
    private val screen: Screen,
    private val adapter: Adapter,
    private val preferences: Preferences,
    private val themeSwitcher: ThemeSwitcher
) {
    private var showCompleted: Boolean
    private var showArchived: Boolean

    fun onCreateHabit() {
        screen.showSelectHabitTypeDialog()
    }

    fun onViewFAQ() {
        screen.showFAQScreen()
    }

    fun onViewAbout() {
        screen.showAboutScreen()
    }

    fun onViewSettings() {
        screen.showSettingsScreen()
    }

    fun onToggleShowArchived() {
        showArchived = !showArchived
        preferences.showArchived = showArchived
        updateAdapterFilter()
    }

    fun onToggleShowCompleted() {
        showCompleted = !showCompleted
        preferences.showCompleted = showCompleted
        updateAdapterFilter()
    }

    fun onSortByManually() {
        adapter.primaryOrder = HabitList.Order.BY_POSITION
    }

    fun onSortByColor() {
        onSortToggleBy(HabitList.Order.BY_COLOR_ASC, HabitList.Order.BY_COLOR_DESC)
    }

    fun onSortByScore() {
        onSortToggleBy(HabitList.Order.BY_SCORE_DESC, HabitList.Order.BY_SCORE_ASC)
    }

    fun onSortByName() {
        onSortToggleBy(HabitList.Order.BY_NAME_ASC, HabitList.Order.BY_NAME_DESC)
    }

    fun onSortByStatus() {
        onSortToggleBy(HabitList.Order.BY_STATUS_ASC, HabitList.Order.BY_STATUS_DESC)
    }

    private fun onSortToggleBy(defaultOrder: HabitList.Order, reversedOrder: HabitList.Order) {
        if (adapter.primaryOrder != defaultOrder) {
            if (adapter.primaryOrder != reversedOrder) {
                adapter.secondaryOrder = adapter.primaryOrder
            }
            adapter.primaryOrder = defaultOrder
        } else {
            adapter.primaryOrder = reversedOrder
        }
    }

    fun onToggleNightMode() {
        themeSwitcher.toggleNightMode()
        screen.applyTheme()
    }

    fun onPreferencesChanged() {
        updateAdapterFilter()
    }

    private fun updateAdapterFilter() {
        if (preferences.areQuestionMarksEnabled) {
            adapter.setFilter(
                HabitMatcher(
                    isArchivedAllowed = showArchived,
                    isEnteredAllowed = showCompleted,
                )
            )
        } else {
            adapter.setFilter(
                HabitMatcher(
                    isArchivedAllowed = showArchived,
                    isCompletedAllowed = showCompleted,
                )
            )
        }
        adapter.refresh()
    }

    interface Adapter {
        fun refresh()
        fun setFilter(matcher: HabitMatcher)
        var primaryOrder: HabitList.Order
        var secondaryOrder: HabitList.Order
    }

    interface Screen {
        fun applyTheme()
        fun showAboutScreen()
        fun showFAQScreen()
        fun showSettingsScreen()
        fun showSelectHabitTypeDialog()
    }

    init {
        showCompleted = preferences.showCompleted
        showArchived = preferences.showArchived
        updateAdapterFilter()
    }
}
