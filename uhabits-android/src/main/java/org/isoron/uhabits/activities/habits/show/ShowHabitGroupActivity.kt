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
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.HabitGroupsDirFinder
import org.isoron.uhabits.activities.common.dialogs.ConfirmDeleteDialog
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitGroupMenuPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitGroupPresenter
import org.isoron.uhabits.intents.IntentFactory
import org.isoron.uhabits.utils.dismissCurrentAndShow
import org.isoron.uhabits.utils.dismissCurrentDialog
import org.isoron.uhabits.utils.showMessage
import org.isoron.uhabits.utils.showSendFileScreen
import org.isoron.uhabits.widgets.WidgetUpdater

class ShowHabitGroupActivity : AppCompatActivity(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var menu: ShowHabitGroupMenu
    private lateinit var view: ShowHabitGroupView
    private lateinit var habitGroup: HabitGroup
    private lateinit var preferences: Preferences
    private lateinit var themeSwitcher: AndroidThemeSwitcher
    private lateinit var widgetUpdater: WidgetUpdater

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var presenter: ShowHabitGroupPresenter
    private val screen = Screen()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val habitGroupList = appComponent.habitGroupList
        habitGroup = habitGroupList.getById(ContentUris.parseId(intent.data!!))!!
        preferences = appComponent.preferences
        commandRunner = appComponent.commandRunner
        widgetUpdater = appComponent.widgetUpdater

        themeSwitcher = AndroidThemeSwitcher(this, preferences)
        themeSwitcher.apply()

        presenter = ShowHabitGroupPresenter(
            commandRunner = commandRunner,
            habitGroup = habitGroup,
            preferences = preferences,
            screen = screen
        )

        view = ShowHabitGroupView(this)

        val menuPresenter = ShowHabitGroupMenuPresenter(
            commandRunner = commandRunner,
            habitGroup = habitGroup,
            habitGroupList = habitGroupList,
            screen = screen,
            system = HabitGroupsDirFinder(AndroidDirFinder(this)),
            taskRunner = appComponent.taskRunner
        )

        menu = ShowHabitGroupMenu(
            activity = this,
            presenter = menuPresenter,
            preferences = preferences
        )

        view.setListener(presenter)
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

    inner class Screen : ShowHabitGroupMenuPresenter.Screen, ShowHabitGroupPresenter.Screen {
        override fun updateWidgets() {
            widgetUpdater.updateWidgets()
        }

        override fun refresh() {
            scope.launch {
                view.setState(
                    ShowHabitGroupPresenter.buildState(
                        habitGroup = habitGroup,
                        preferences = preferences,
                        theme = themeSwitcher.currentTheme
                    )
                )
            }
        }

        override fun showEditHabitGroupScreen(habitGroup: HabitGroup) {
            startActivity(IntentFactory().startEditGroupActivity(this@ShowHabitGroupActivity, habitGroup))
        }

        override fun showMessage(m: ShowHabitGroupMenuPresenter.Message?) {
            when (m) {
                ShowHabitGroupMenuPresenter.Message.COULD_NOT_EXPORT -> {
                    showMessage(resources.getString(R.string.could_not_export))
                }
                else -> {}
            }
        }

        override fun showSendFileScreen(filename: String) {
            this@ShowHabitGroupActivity.showSendFileScreen(filename)
        }

        override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
            ConfirmDeleteDialog(this@ShowHabitGroupActivity, callback, 1).dismissCurrentAndShow()
        }

        override fun close() {
            this@ShowHabitGroupActivity.finish()
        }
    }
}
