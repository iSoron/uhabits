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

package org.isoron.uhabits.activities.habits.list

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsMenuBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.utils.StyledResources
import javax.inject.Inject

@ActivityScope
class ListHabitsMenu @Inject constructor(
    @ActivityContext context: Context,
    private val preferences: Preferences,
    private val themeSwitcher: ThemeSwitcher,
    val behavior: ListHabitsMenuBehavior
) {
    val activity = (context as AppCompatActivity)

    fun onCreate(inflater: MenuInflater, menu: Menu) {
        menu.clear()
        inflater.inflate(R.menu.list_habits, menu)
        val nightModeItem = menu.findItem(R.id.actionToggleNightMode)
        val hideArchivedItem = menu.findItem(R.id.actionHideArchived)
        val hideCompletedItem = menu.findItem(R.id.actionHideCompleted)
        nightModeItem.isChecked = themeSwitcher.isNightMode
        hideArchivedItem.isChecked = !preferences.showArchived
        hideCompletedItem.isChecked = !preferences.showCompleted
        if (preferences.areQuestionMarksEnabled || preferences.isSkipEnabled) {
            hideCompletedItem.title = activity.resources.getString(R.string.hide_entered)
        } else {
            hideCompletedItem.title = activity.resources.getString(R.string.hide_completed)
        }
        updateArrows(menu)
    }

    private fun updateArrows(menu: Menu) {
        val styledResources = StyledResources(activity)
        val sortManual = menu.findItem(R.id.actionSortManual)
        val sortName = menu.findItem(R.id.actionSortName)
        val sortColor = menu.findItem(R.id.actionSortColor)
        val sortScore = menu.findItem(R.id.actionSortScore)
        val sortStatus = menu.findItem(R.id.actionSortStatus)
        val arrowUp = styledResources.getDrawable(R.attr.iconArrowUp)
        val arrowDown = styledResources.getDrawable(R.attr.iconArrowDown)
        when (preferences.defaultPrimaryOrder) {
            HabitList.Order.BY_NAME_ASC -> sortName.icon = arrowDown
            HabitList.Order.BY_NAME_DESC -> sortName.icon = arrowUp
            HabitList.Order.BY_COLOR_ASC -> sortColor.icon = arrowDown
            HabitList.Order.BY_COLOR_DESC -> sortColor.icon = arrowUp
            HabitList.Order.BY_SCORE_ASC -> sortScore.icon = arrowDown
            HabitList.Order.BY_SCORE_DESC -> sortScore.icon = arrowUp
            HabitList.Order.BY_STATUS_ASC -> sortStatus.icon = arrowDown
            HabitList.Order.BY_STATUS_DESC -> sortStatus.icon = arrowUp
            HabitList.Order.BY_POSITION -> sortManual.icon = arrowUp
        }
    }

    fun onItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionToggleNightMode -> {
                behavior.onToggleNightMode()
                return true
            }

            R.id.actionCreateHabit -> {
                behavior.onCreateHabit()
                return true
            }

            R.id.actionFAQ -> {
                behavior.onViewFAQ()
                return true
            }

            R.id.actionAbout -> {
                behavior.onViewAbout()
                return true
            }

            R.id.actionSettings -> {
                behavior.onViewSettings()
                return true
            }

            R.id.actionHideArchived -> {
                behavior.onToggleShowArchived()
                activity.invalidateOptionsMenu()
                return true
            }

            R.id.actionHideCompleted -> {
                behavior.onToggleShowCompleted()
                activity.invalidateOptionsMenu()
                return true
            }

            R.id.actionSortColor -> {
                behavior.onSortByColor()
                return true
            }

            R.id.actionSortManual -> {
                behavior.onSortByManually()
                return true
            }

            R.id.actionSortName -> {
                behavior.onSortByName()
                return true
            }

            R.id.actionSortScore -> {
                behavior.onSortByScore()
                return true
            }

            R.id.actionSortStatus -> {
                behavior.onSortByStatus()
                return true
            }

            else -> return false
        }
    }
}
