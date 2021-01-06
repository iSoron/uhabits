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

package org.isoron.uhabits.activities.habits.show

import android.view.Menu
import android.view.MenuItem
import org.isoron.uhabits.R
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitMenuPresenter

class ShowHabitMenu(
    val activity: ShowHabitActivity,
    val presenter: ShowHabitMenuPresenter,
    val preferences: Preferences,
) {
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.show_habit, menu)
        if (preferences.isDeveloper) {
            menu.findItem(R.id.action_randomize).isVisible = true
        }
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_habit -> {
                presenter.onEditHabit()
                return true
            }
            R.id.action_delete -> {
                presenter.onDeleteHabit()
                return true
            }
            R.id.action_randomize -> {
                presenter.onRandomize()
                return true
            }
            R.id.export -> {
                presenter.onExportCSV()
                return true
            }
        }
        return false
    }
}
