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

import android.app.*
import android.content.*
import android.support.annotation.*
import dagger.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.activities.habits.edit.*
import org.isoron.uhabits.activities.habits.list.views.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.core.ui.callbacks.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.tasks.*
import java.io.*
import javax.inject.*

const val RESULT_IMPORT_DATA = 1
const val RESULT_EXPORT_CSV = 2
const val RESULT_EXPORT_DB = 3
const val RESULT_BUG_REPORT = 4
const val RESULT_REPAIR_DB = 5
const val REQUEST_OPEN_DOCUMENT = 6
const val REQUEST_SETTINGS = 7

@ActivityScope
class ListHabitsScreen
@Inject constructor(
        activity: BaseActivity,
        rootView: ListHabitsRootView,
        private val commandRunner: CommandRunner,
        private val intentFactory: IntentFactory,
        private val themeSwitcher: ThemeSwitcher,
        private val preferences: Preferences,
        private val adapter: HabitCardListAdapter,
        private val taskRunner: TaskRunner,
        private val exportDBFactory: ExportDBTaskFactory,
        private val importTaskFactory: ImportDataTaskFactory,
        private val confirmDeleteDialogFactory: ConfirmDeleteDialogFactory,
        private val colorPickerFactory: ColorPickerDialogFactory,
        private val editHabitDialogFactory: EditHabitDialogFactory,
        private val numberPickerFactory: NumberPickerFactory,
        private val behavior: Lazy<ListHabitsBehavior>,
        private val menu: Lazy<ListHabitsMenu>,
        private val selectionMenu: Lazy<ListHabitsSelectionMenu>
) : BaseScreen(activity),
    CommandRunner.Listener,
    ListHabitsBehavior.Screen,
    ListHabitsMenuBehavior.Screen,
    ListHabitsSelectionMenuBehavior.Screen {

    init {
        setRootView(rootView)
    }

    fun onAttached() {
        setMenu(menu.get())
        setSelectionMenu(selectionMenu.get())
        commandRunner.addListener(this)
    }

    fun onDettached() {
        commandRunner.removeListener(this)
    }

    override fun onCommandExecuted(command: Command, refreshKey: Long?) {
        if (command.isRemote) return
        showMessage(getExecuteString(command))
    }

    override fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OPEN_DOCUMENT -> onOpenDocumentResult(resultCode, data)
            REQUEST_SETTINGS -> onSettingsResult(resultCode)
        }
    }

    private fun onOpenDocumentResult(resultCode: Int, data: Intent?) {
        if (data == null) return
        if (resultCode != Activity.RESULT_OK) return
        try {
            val inStream = activity.contentResolver.openInputStream(data.data)
            val cacheDir = activity.externalCacheDir
            val tempFile = File.createTempFile("import", "", cacheDir)
            FileUtils.copy(inStream, tempFile)
            onImportData(tempFile) { tempFile.delete() }
        } catch (e: IOException) {
            showMessage(R.string.could_not_import)
            e.printStackTrace()
        }
    }

    private fun onSettingsResult(resultCode: Int) {
        when (resultCode) {
            RESULT_IMPORT_DATA -> showImportScreen()
            RESULT_EXPORT_CSV -> behavior.get().onExportCSV()
            RESULT_EXPORT_DB -> onExportDB()
            RESULT_BUG_REPORT -> behavior.get().onSendBugReport()
            RESULT_REPAIR_DB -> behavior.get().onRepairDB()
        }
    }

    override fun applyTheme() {
        themeSwitcher.apply()
        activity.restartWithFade(ListHabitsActivity::class.java)
    }

    override fun showAboutScreen() {
        val intent = intentFactory.startAboutActivity(activity)
        activity.startActivity(intent)
    }

    fun showCreateBooleanHabitScreen() {
        val dialog = editHabitDialogFactory.createBoolean()
        activity.showDialog(dialog, "editHabit")
    }

    override fun showCreateHabitScreen() {
        if (!preferences.isNumericalHabitsFeatureEnabled) {
            showCreateBooleanHabitScreen()
            return
        }

        val dialog = AlertDialog.Builder(activity)
                .setTitle("Type of habit")
                .setItems(R.array.habitTypes) { _, which ->
                    if (which == 0) showCreateBooleanHabitScreen()
                    else showCreateNumericalHabitScreen()
                }
                .create()

        dialog.show()
    }

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback) {
        activity.showDialog(confirmDeleteDialogFactory.create(callback))
    }

    override fun showEditHabitsScreen(habits: List<Habit>) {
        val dialog = editHabitDialogFactory.edit(habits[0])
        activity.showDialog(dialog, "editNumericalHabit")
    }

    override fun showFAQScreen() {
        val intent = intentFactory.viewFAQ(activity)
        activity.startActivity(intent)
    }

    override fun showHabitScreen(habit: Habit) {
        val intent = intentFactory.startShowHabitActivity(activity, habit)
        activity.startActivity(intent)
    }

    fun showImportScreen() {
        val intent = intentFactory.openDocument()
        activity.startActivityForResult(intent, REQUEST_OPEN_DOCUMENT)
    }

    override fun showIntroScreen() {
        val intent = intentFactory.startIntroActivity(activity)
        activity.startActivity(intent)
    }

    override fun showMessage(m: ListHabitsBehavior.Message) {
        showMessage(when (m) {
                        COULD_NOT_EXPORT -> R.string.could_not_export
                        IMPORT_SUCCESSFUL -> R.string.habits_imported
                        IMPORT_FAILED -> R.string.could_not_import
                        DATABASE_REPAIRED -> R.string.database_repaired
                        COULD_NOT_GENERATE_BUG_REPORT -> R.string.bug_report_failed
                        FILE_NOT_RECOGNIZED -> R.string.file_not_recognized
                    })
    }

    override fun showSendBugReportToDeveloperScreen(log: String) {
        val to = R.string.bugReportTo
        val subject = R.string.bugReportSubject
        showSendEmailScreen(to, subject, log)
    }

    override fun showSettingsScreen() {
        val intent = intentFactory.startSettingsActivity(activity)
        activity.startActivityForResult(intent, REQUEST_SETTINGS)
    }

    override fun showColorPicker(defaultColor: Int,
                                 callback: OnColorPickedCallback) {
        val picker = colorPickerFactory.create(defaultColor)
        picker.setListener(callback)
        activity.showDialog(picker, "picker")
    }

    override fun showNumberPicker(value: Double,
                                  unit: String,
                                  callback: ListHabitsBehavior.NumberPickerCallback) {
        numberPickerFactory.create(value, unit, callback).show()
    }

    @StringRes
    private fun getExecuteString(command: Command): Int? {
        when (command) {
            is ArchiveHabitsCommand -> return R.string.toast_habit_archived
            is ChangeHabitColorCommand -> return R.string.toast_habit_changed
            is CreateHabitCommand -> return R.string.toast_habit_created
            is DeleteHabitsCommand -> return R.string.toast_habit_deleted
            is EditHabitCommand -> return R.string.toast_habit_changed
            is UnarchiveHabitsCommand -> return R.string.toast_habit_unarchived
            else -> return null
        }
    }

    private fun showCreateNumericalHabitScreen() {
        val dialog = editHabitDialogFactory.createNumerical()
        activity.showDialog(dialog, "editHabit")
    }

    private fun onImportData(file: File, onFinished: () -> Unit) {
        taskRunner.execute(importTaskFactory.create(file) { result ->
            if (result == ImportDataTask.SUCCESS) {
                adapter.refresh()
                showMessage(R.string.habits_imported)
            } else if (result == ImportDataTask.NOT_RECOGNIZED) {
                showMessage(R.string.file_not_recognized)
            } else {
                showMessage(R.string.could_not_import)
            }
            onFinished()
        })
    }

    private fun onExportDB() {
        taskRunner.execute(exportDBFactory.create { filename ->
            if (filename != null) showSendFileScreen(filename)
            else showMessage(R.string.could_not_export)
        })
    }
}
