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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import org.isoron.platform.gui.ScreenLocation
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.dialogs.CheckmarkPopup
import org.isoron.uhabits.activities.common.dialogs.ColorPickerDialogFactory
import org.isoron.uhabits.activities.common.dialogs.ConfirmDeleteDialog
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.activities.common.dialogs.POPUP_WIDTH
import org.isoron.uhabits.activities.habits.edit.HabitTypeDialog
import org.isoron.uhabits.activities.habits.list.views.HabitCardListAdapter
import org.isoron.uhabits.core.commands.ArchiveHabitsCommand
import org.isoron.uhabits.core.commands.ChangeHabitColorCommand
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateHabitCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.commands.EditHabitCommand
import org.isoron.uhabits.core.commands.UnarchiveHabitsCommand
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.COULD_NOT_EXPORT
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.COULD_NOT_GENERATE_BUG_REPORT
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.DATABASE_REPAIRED
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.FILE_NOT_RECOGNIZED
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.IMPORT_FAILED
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior.Message.IMPORT_SUCCESSFUL
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsMenuBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.intents.IntentFactory
import org.isoron.uhabits.tasks.ExportDBTaskFactory
import org.isoron.uhabits.tasks.ImportDataTask
import org.isoron.uhabits.tasks.ImportDataTaskFactory
import org.isoron.uhabits.utils.copyTo
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.restartWithFade
import org.isoron.uhabits.utils.showMessage
import org.isoron.uhabits.utils.showSendEmailScreen
import org.isoron.uhabits.utils.showSendFileScreen
import java.io.File
import java.io.IOException
import javax.inject.Inject

const val RESULT_IMPORT_DATA = 101
const val RESULT_EXPORT_CSV = 102
const val RESULT_EXPORT_DB = 103
const val RESULT_BUG_REPORT = 104
const val RESULT_REPAIR_DB = 105
const val REQUEST_OPEN_DOCUMENT = 106
const val REQUEST_SETTINGS = 107

@ActivityScope
class ListHabitsScreen
@Inject constructor(
    @ActivityContext val context: Context,
    private val commandRunner: CommandRunner,
    private val intentFactory: IntentFactory,
    private val themeSwitcher: ThemeSwitcher,
    private val adapter: HabitCardListAdapter,
    private val taskRunner: TaskRunner,
    private val exportDBFactory: ExportDBTaskFactory,
    private val importTaskFactory: ImportDataTaskFactory,
    private val colorPickerFactory: ColorPickerDialogFactory,
    private val numberPickerFactory: NumberPickerFactory,
    private val behavior: Lazy<ListHabitsBehavior>,
    private val preferences: Preferences,
    private val rootView: Lazy<ListHabitsRootView>,
) : CommandRunner.Listener,
    ListHabitsBehavior.Screen,
    ListHabitsMenuBehavior.Screen,
    ListHabitsSelectionMenuBehavior.Screen {

    val activity = (context as AppCompatActivity)

    fun onAttached() {
        commandRunner.addListener(this)
    }

    fun onDetached() {
        commandRunner.removeListener(this)
    }

    override fun onCommandFinished(command: Command) {
        val msg = getExecuteString(command)
        if (msg != null) activity.showMessage(msg)
    }

    fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_OPEN_DOCUMENT -> onOpenDocumentResult(resultCode, data)
            REQUEST_SETTINGS -> onSettingsResult(resultCode)
        }
    }

    private fun onOpenDocumentResult(resultCode: Int, data: Intent?) {
        if (data == null) return
        if (resultCode != Activity.RESULT_OK) return
        try {
            val inStream = activity.contentResolver.openInputStream(data.data!!)!!
            val cacheDir = activity.externalCacheDir
            val tempFile = File.createTempFile("import", "", cacheDir)
            inStream.copyTo(tempFile)
            onImportData(tempFile) { tempFile.delete() }
        } catch (e: IOException) {
            activity.showMessage(activity.resources.getString(R.string.could_not_import))
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

    override fun showSelectHabitTypeDialog() {
        val dialog = HabitTypeDialog()
        dialog.show(activity.supportFragmentManager, "habitType")
    }

    override fun showDeleteConfirmationScreen(callback: OnConfirmedCallback, quantity: Int) {
        ConfirmDeleteDialog(activity, callback, quantity).show()
    }

    override fun showEditHabitsScreen(selected: List<Habit>) {
        val intent = intentFactory.startEditActivity(activity, selected[0])
        activity.startActivity(intent)
    }

    override fun showFAQScreen() {
        val intent = intentFactory.viewFAQ(activity)
        activity.startActivity(intent)
    }

    override fun showHabitScreen(h: Habit) {
        val intent = intentFactory.startShowHabitActivity(activity, h)
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
        activity.showMessage(
            activity.resources.getString(
                when (m) {
                    COULD_NOT_EXPORT -> R.string.could_not_export
                    IMPORT_SUCCESSFUL -> R.string.habits_imported
                    IMPORT_FAILED -> R.string.could_not_import
                    DATABASE_REPAIRED -> R.string.database_repaired
                    COULD_NOT_GENERATE_BUG_REPORT -> R.string.bug_report_failed
                    FILE_NOT_RECOGNIZED -> R.string.file_not_recognized
                }
            )
        )
    }

    override fun showSendBugReportToDeveloperScreen(log: String) {
        val to = R.string.bugReportTo
        val subject = R.string.bugReportSubject
        activity.showSendEmailScreen(to, subject, log)
    }

    override fun showSendFileScreen(filename: String) {
        activity.showSendFileScreen(filename)
    }

    override fun showSettingsScreen() {
        val intent = intentFactory.startSettingsActivity(activity)
        activity.startActivityForResult(intent, REQUEST_SETTINGS)
    }

    override fun showColorPicker(defaultColor: PaletteColor, callback: OnColorPickedCallback) {
        val picker = colorPickerFactory.create(defaultColor, themeSwitcher.currentTheme!!)
        picker.setListener(callback)
        picker.show(activity.supportFragmentManager, "picker")
    }

    override fun showNumberPicker(
        value: Double,
        unit: String,
        notes: String,
        dateString: String,
        frequency: Frequency,
        callback: ListHabitsBehavior.NumberPickerCallback
    ) {
        numberPickerFactory.create(value, unit, notes, dateString, frequency, callback).show()
    }

    override fun showCheckmarkPopup(
        selectedValue: Int,
        notes: String,
        color: PaletteColor,
        location: ScreenLocation,
        callback: ListHabitsBehavior.CheckMarkDialogCallback
    ) {
        val view = rootView.get()
        CheckmarkPopup(
            context = context,
            prefs = preferences,
            anchor = view,
            color = view.currentTheme().color(color).toInt(),
            notes = notes,
            value = selectedValue,
        ).apply {
            onToggle = { value, notes -> callback.onNotesSaved(value, notes) }
            show(
                ScreenLocation(
                    x = location.x - POPUP_WIDTH / 2,
                    y = location.y
                )
            )
        }
    }

    private fun getExecuteString(command: Command): String? {
        when (command) {
            is ArchiveHabitsCommand -> {
                return activity.resources.getQuantityString(
                    R.plurals.toast_habits_archived,
                    command.selected.size
                )
            }
            is ChangeHabitColorCommand -> {
                return activity.resources.getQuantityString(
                    R.plurals.toast_habits_changed,
                    command.selected.size
                )
            }
            is CreateHabitCommand -> {
                return activity.resources.getString(R.string.toast_habit_created)
            }
            is DeleteHabitsCommand -> {
                return activity.resources.getQuantityString(
                    R.plurals.toast_habits_deleted,
                    command.selected.size
                )
            }
            is EditHabitCommand -> {
                return activity.resources.getQuantityString(R.plurals.toast_habits_changed, 1)
            }
            is UnarchiveHabitsCommand -> {
                return activity.resources.getQuantityString(
                    R.plurals.toast_habits_unarchived,
                    command.selected.size
                )
            }
            else -> return null
        }
    }

    private fun onImportData(file: File, onFinished: () -> Unit) {
        taskRunner.execute(
            importTaskFactory.create(file) { result ->
                when (result) {
                    ImportDataTask.SUCCESS -> {
                        adapter.refresh()
                        activity.showMessage(activity.resources.getString(R.string.habits_imported))
                    }
                    ImportDataTask.NOT_RECOGNIZED -> {
                        activity.showMessage(activity.resources.getString(R.string.file_not_recognized))
                    }
                    else -> {
                        activity.showMessage(activity.resources.getString(R.string.could_not_import))
                    }
                }
                onFinished()
            }
        )
    }

    private fun onExportDB() {
        taskRunner.execute(
            exportDBFactory.create { filename ->
                if (filename != null) activity.showSendFileScreen(filename)
                else activity.showMessage(activity.resources.getString(R.string.could_not_export))
            }
        )
    }
}
