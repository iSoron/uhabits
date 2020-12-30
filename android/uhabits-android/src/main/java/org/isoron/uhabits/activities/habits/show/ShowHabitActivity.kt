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
package org.isoron.uhabits.activities.habits.show

import android.content.ContentUris
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.HabitsDirFinder
import org.isoron.uhabits.activities.common.dialogs.ConfirmDeleteDialogFactory
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitBehavior
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitMenuBehavior
import org.isoron.uhabits.intents.IntentFactory

class ShowHabitActivity : AppCompatActivity(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var menu: ShowHabitMenu
    private lateinit var presenter: ShowHabitPresenter
    private lateinit var view: ShowHabitView

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val habitList = appComponent.habitList
        val habit = habitList.getById(ContentUris.parseId(intent.data!!))!!
        val preferences = appComponent.preferences
        commandRunner = appComponent.commandRunner
        AndroidThemeSwitcher(this, preferences).apply()

        view = ShowHabitView(this)
        presenter = ShowHabitPresenter(
            context = this,
            habit = habit,
            preferences = appComponent.preferences,
        )

        val screen = ShowHabitScreen(
            activity = this,
            confirmDeleteDialogFactory = ConfirmDeleteDialogFactory { this },
            habit = habit,
            intentFactory = IntentFactory(),
            numberPickerFactory = NumberPickerFactory(this),
            widgetUpdater = appComponent.widgetUpdater,
        )

        val behavior = ShowHabitBehavior(
            commandRunner = commandRunner,
            habit = habit,
            habitList = habitList,
            preferences = preferences,
            screen = screen,
        )

        val menuBehavior = ShowHabitMenuBehavior(
            commandRunner = commandRunner,
            habit = habit,
            habitList = habitList,
            screen = screen,
            system = HabitsDirFinder(AndroidDirFinder(this)),
            taskRunner = appComponent.taskRunner,
        )

        menu = ShowHabitMenu(
            activity = this,
            behavior = menuBehavior,
            preferences = preferences,
        )

        view.onScoreCardSpinnerPosition = behavior::onScoreCardSpinnerPosition
        view.onBarCardBoolSpinnerPosition = behavior::onBarCardBoolSpinnerPosition
        view.onBarCardNumericalSpinnerPosition = behavior::onBarCardNumericalSpinnerPosition
        view.onClickEditHistoryButton = behavior::onClickEditHistory

        setContentView(view)
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        return menu.onCreateOptionsMenu(m)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menu.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        commandRunner.addListener(this)
        refresh()
    }

    override fun onPause() {
        commandRunner.removeListener(this)
        super.onPause()
    }

    override fun onCommandFinished(command: Command) {
        refresh()
    }

    fun refresh() {
        scope.launch {
            view.update(presenter.present())
        }
    }
}
