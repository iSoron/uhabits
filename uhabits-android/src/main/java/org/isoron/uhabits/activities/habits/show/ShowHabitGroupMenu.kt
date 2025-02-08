package org.isoron.uhabits.activities.habits.show

import android.view.Menu
import android.view.MenuItem
import org.isoron.uhabits.R
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitGroupMenuPresenter

class ShowHabitGroupMenu(
    val activity: ShowHabitGroupActivity,
    val presenter: ShowHabitGroupMenuPresenter,
    val preferences: Preferences
) {
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.show_habit_group, menu)
        return true
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_habit_group -> {
                presenter.onEditHabitGroup()
                return true
            }
            R.id.action_delete -> {
                presenter.onDeleteHabitGroup()
                return true
            }
        }
        return false
    }
}
