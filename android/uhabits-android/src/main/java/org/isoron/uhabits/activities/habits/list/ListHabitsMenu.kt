/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

import android.view.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import javax.inject.*

@ActivityScope
class ListHabitsMenu @Inject constructor(
        activity: BaseActivity,
        private val preferences: Preferences,
        private val themeSwitcher: ThemeSwitcher,
        private val behavior: ListHabitsMenuBehavior
) : BaseMenu(activity), Preferences.Listener {

    private lateinit var topBarMenu: Menu

    override fun onCreate(menu: Menu) {
        val nightModeItem = menu.findItem(R.id.actionToggleNightMode)
        val hideArchivedItem = menu.findItem(R.id.actionHideArchived)
        val hideCompletedItem = menu.findItem(R.id.actionHideCompleted)

        nightModeItem.isChecked = themeSwitcher.isNightMode
        hideArchivedItem.isChecked = !preferences.showArchived
        hideCompletedItem.isChecked = !preferences.showCompleted

        topBarMenu = menu
        //the habit creation menu should be disabled when numeric habits are also disabled
        if (!preferences.isNumericalHabitsFeatureEnabled) {
            setCreateHabitMenuEnabled(false, menu)
        }
        //let the class add itself as listener
        preferences.addListener(this)
    }

    override fun onNumericalHabitsFeatureChanged() {
        if(topBarMenu==null){return}
        setCreateHabitMenuEnabled(preferences.isNumericalHabitsFeatureEnabled, topBarMenu)
    }

    override fun onItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionToggleNightMode -> {
                behavior.onToggleNightMode()
                return true
            }

            R.id.actionAdd -> {
                behavior.onCreateHabit()
                return true
            }

            R.id.actionCreateBooleanHabit -> {
                behavior.onCreateBooleanHabit()
                return true
            }

            R.id.actionCreateNumeralHabit -> {
                behavior.onCreateNumericalHabit()
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
                invalidate()
                return true
            }

            R.id.actionHideCompleted -> {
                behavior.onToggleShowCompleted()
                invalidate()
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

            else -> return false
        }
    }

    /**
     * @param enabled whether the create habit menu should be enabled or disabled
     * @param menu a reference to the menu on which should be enabled or disabled
     */
    fun setCreateHabitMenuEnabled(enabled: Boolean, menu: Menu) {
        val habitCreationMenu = menu.findItem(R.id.actionAdd).subMenu
        for (itemIndex: Int in 0 until habitCreationMenu.size()) {
            val menuItem = habitCreationMenu.getItem(itemIndex)
            menuItem.isEnabled = enabled
            menuItem.isVisible = enabled
        }
    }

    override fun getMenuResourceId() = R.menu.list_habits
}
