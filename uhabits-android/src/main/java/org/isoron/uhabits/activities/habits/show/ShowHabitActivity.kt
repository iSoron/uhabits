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

import android.content.ContentUris
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.AndroidDirFinder
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.HabitsDirFinder
import org.isoron.uhabits.activities.common.dialogs.CheckmarkDialog
import org.isoron.uhabits.activities.common.dialogs.ConfirmDeleteDialog
import org.isoron.uhabits.activities.common.dialogs.HistoryEditorDialog
import org.isoron.uhabits.activities.common.dialogs.NumberDialog
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitMenuPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitPresenter
import org.isoron.uhabits.core.ui.views.OnDateClickedListener
import org.isoron.uhabits.intents.IntentFactory
import org.isoron.uhabits.utils.applyRootViewInsets
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.dismissCurrentAndShow
import org.isoron.uhabits.utils.dismissCurrentDialog
import org.isoron.uhabits.utils.showMessage
import org.isoron.uhabits.utils.showSendFileScreen
import org.isoron.uhabits.widgets.WidgetUpdater

class ShowHabitActivity : AppCompatActivity(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var menu: ShowHabitMenu
    private lateinit var view: ShowHabitView
    private lateinit var habit: Habit
    private lateinit var preferences: Preferences
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var widgetUpdater: WidgetUpdater

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var presenter: ShowHabitPresenter
    private val screen = Screen()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val habitList = appComponent.habitList
        habit = habitList.getById(ContentUris.parseId(intent.data!!))!!
        preferences = appComponent.preferences
        commandRunner = appComponent.commandRunner
        widgetUpdater = appComponent.widgetUpdater

        themeSwitcher = AndroidThemeSwitcher(this, preferences)
        themeSwitcher.apply()

        presenter = ShowHabitPresenter(
            commandRunner = commandRunner,
            habit = habit,
            habitList = habitList,
            preferences = preferences,
            screen = screen
        )

        view = ShowHabitView(this)

        val menuPresenter = ShowHabitMenuPresenter(
            commandRunner = commandRunner,
            habit = habit,
            habitList = habitList,
            screen = screen,
            system = HabitsDirFinder(AndroidDirFinder(this)),
            taskRunner = appComponent.taskRunner
        )

        menu = ShowHabitMenu(
            activity = this,
            presenter = menuPresenter,
            preferences = preferences
        )

        view.setListener(presenter)
        view.applyRootViewInsets()
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
        supportFragmentManager.findFragmentByTag("historyEditor")?.let {
            (it as HistoryEditorDialog).setOnDateClickedListener(presenter.historyCardPresenter)
        }
        screen.refresh()
    }

    override fun onPause() {
        dismissCurrentDialog()
        commandRunner.removeListener(this)
        super.onPause()
    }

    override fun onCommandFinished(command: Command) {
        screen.refresh()
    }

    inner class Screen : ShowHabitMenuPresenter.Screen, ShowHabitPresenter.Screen {
        override fun updateWidgets() {
            widgetUpdater.updateWidgets()
        }

        override fun refresh() {
            scope.launch {
                view.setState(
                    ShowHabitPresenter.buildState(
                        habit = habit,
                        preferences = preferences,
                        theme = themeSwitcher.currentTheme
                    )
                )
            }
        }

        override fun showHistoryEditorDialog(listener: OnDateClickedListener) {
            val dialog = HistoryEditorDialog()
            dialog.arguments = Bundle().apply {
                putLong("habit", habit.id!!)
            }
            dialog.setOnDateClickedListener(listener)
            dialog.show(supportFragmentManager, "historyEditor")
        }

        override fun showFeedback() {
            window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        override fun showNumberPopup(
            value: Double,
            notes: String,
            callback: ListHabitsBehavior.NumberPickerCallback
        ) {
            val dialog = NumberDialog()
            dialog.arguments = Bundle().apply {
                putDouble("value", value)
                putString("notes", notes)
            }
            dialog.onToggle = { v, n -> callback.onNumberPicked(v, n) }
            dialog.dismissCurrentAndShow(supportFragmentManager, "numberDialog")
        }

        override fun showCheckmarkPopup(
            selectedValue: Int,
            notes: String,
            color: PaletteColor,
            callback: ListHabitsBehavior.CheckMarkDialogCallback
        ) {
            val theme = view.currentTheme()
            val dialog = CheckmarkDialog()
            dialog.arguments = Bundle().apply {
                putInt("color", theme.color(color).toInt())
                putInt("value", selectedValue)
                putString("notes", notes)
            }
            dialog.onToggle = { v, n -> callback.onNotesSaved(v, n) }
            dialog.dismissCurrentAndShow(supportFragmentManager, "checkmarkDialog")
        }

        private fun getPopupAnchor(): View? {
            val dialog = supportFragmentManager.findFragmentByTag("historyEditor") as HistoryEditorDialog?
            return dialog?.dataView
        }

        override fun showEditHabitScreen(habit: Habit) {
            startActivity(IntentFactory().startEditActivity(this@ShowHabitActivity, habit))
        }

        override fun showMessage(m: ShowHabitMenuPresenter.Message?) {
            when (m) {
                ShowHabitMenuPresenter.Message.COULD_NOT_EXPORT -> {
                    showMessage(resources.getString(R.string.could_not_export))
                }
                else -> {}
            }
        }

        override fun showSendFileScreen(filename: String) {
            this@ShowHabitActivity.showSendFileScreen(filename)
        }

        override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
            ConfirmDeleteDialog(this@ShowHabitActivity, callback, 1).dismissCurrentAndShow()
        }

        override fun close() {
            this@ShowHabitActivity.finish()
        }
    }
}
