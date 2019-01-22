/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

import android.content.*
import dagger.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.activities.habits.edit.*
import org.isoron.uhabits.activities.habits.list.views.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.core.ui.callbacks.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.tasks.*
import org.junit.*
import org.junit.runner.*
import org.mockito.*
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.*

@RunWith(MockitoJUnitRunner::class)
class ListHabitsScreenTest : BaseAndroidJVMTest() {
    @Mock lateinit var activity: BaseActivity
    @Mock lateinit var habit: Habit
    @Mock lateinit var intent: Intent
    @Mock lateinit var rootView: ListHabitsRootView
    @Mock lateinit var confirmDeleteDialogFactory: ConfirmDeleteDialogFactory
    @Mock lateinit var intentFactory: IntentFactory
    @Mock lateinit var colorPickerDialogFactory: ColorPickerDialogFactory
    @Mock lateinit var editHabitDialogFactory: EditHabitDialogFactory
    @Mock lateinit var themeSwitcher: ThemeSwitcher
    @Mock lateinit var prefs: Preferences
    @Mock lateinit var menu: ListHabitsMenu
    @Mock lateinit var selectionMenu: ListHabitsSelectionMenu
    @Mock lateinit var adapter: HabitCardListAdapter
    @Mock lateinit var behavior: ListHabitsBehavior
    @Mock lateinit var exportDBFactory: ExportDBTaskFactory
    @Mock lateinit var importTaskFactory: ImportDataTaskFactory
    @Mock lateinit var numberPickerFactory: NumberPickerFactory

    lateinit var screen: ListHabitsScreen

    @Before
    override fun setUp() {
        super.setUp()
        commandRunner = mock(CommandRunner::class.java)
        screen = spy(ListHabitsScreen(
                activity = activity,
                rootView = rootView,
                commandRunner = commandRunner,
                intentFactory = intentFactory,
                themeSwitcher = themeSwitcher,
                preferences = prefs,
                confirmDeleteDialogFactory = confirmDeleteDialogFactory,
                colorPickerFactory = colorPickerDialogFactory,
                editHabitDialogFactory = editHabitDialogFactory,
                menu = Lazy { menu },
                selectionMenu = Lazy { selectionMenu },
                adapter = adapter,
                behavior = Lazy { behavior },
                taskRunner = taskRunner,
                exportDBFactory = exportDBFactory,
                importTaskFactory = importTaskFactory,
                numberPickerFactory = numberPickerFactory))

        doNothing().`when`(screen).showMessage(anyInt())
    }

    @Test
    fun testApplyTheme() {
        screen.applyTheme()
        verify(activity).restartWithFade(ListHabitsActivity::class.java)
    }

    @Test
    fun testOnAttached() {
        screen.onAttached()
        verify(commandRunner).addListener(screen)
    }

    @Test
    fun testOnCommand() {
        val c = mock(DeleteHabitsCommand::class.java)
        screen.onCommandExecuted(c, null)
        verify(screen).showMessage(R.string.toast_habit_deleted)
    }

    @Test
    fun testOnDetach() {
        screen.onDettached()
        verify(commandRunner).removeListener(screen)
    }

    @Test
    fun testOnResult_bugReport() {
        screen.onResult(REQUEST_SETTINGS, RESULT_BUG_REPORT, null)
        verify(behavior).onSendBugReport()
    }

    @Test
    fun testOnResult_exportCSV() {
        screen.onResult(REQUEST_SETTINGS, RESULT_EXPORT_CSV, null)
        verify(behavior).onExportCSV()
    }

    @Test
    fun testOnResult_importData() {
        screen.onResult(REQUEST_SETTINGS, RESULT_IMPORT_DATA, null)
        testShowImportScreen()
    }

    @Test
    @Throws(Exception::class)
    fun testShowAboutScreen() {
        `when`(intentFactory.startAboutActivity(activity)).thenReturn(intent)
        screen.showAboutScreen()
        verify(activity).startActivity(eq(intent))
    }

    @Test
    fun testShowColorPicker() {
        val picker = mock(ColorPickerDialog::class.java)
        `when`(colorPickerDialogFactory.create(999)).thenReturn(picker)
        val callback = mock(OnColorPickedCallback::class.java)

        screen.showColorPicker(999, callback)

        verify(activity).showDialog(eq(picker), any())
        verify(picker).setListener(callback)
    }

    @Test
    fun testShowDeleteConfirmationScreen() {
        val callback = mock(OnConfirmedCallback::class.java)
        val dialog = mock(ConfirmDeleteDialog::class.java)
        `when`(confirmDeleteDialogFactory.create(callback)).thenReturn(dialog)

        screen.showDeleteConfirmationScreen(callback)

        verify(activity).showDialog(dialog)
    }

    @Test
    fun testShowEditHabitScreen() {
        val dialog = mock(EditHabitDialog::class.java)
        `when`(editHabitDialogFactory.edit(habit)).thenReturn(dialog)
        screen.showEditHabitsScreen(listOf(habit))
        verify(activity).showDialog(eq(dialog), any())
    }

    @Test
    fun testShowFAQScreen() {
        `when`(intentFactory.viewFAQ(activity)).thenReturn(intent)
        screen.showFAQScreen()
        verify(activity).startActivity(intent)
    }

    @Test
    fun testShowHabitScreen() {
        `when`(intentFactory.startShowHabitActivity(activity, habit))
                .thenReturn(intent)
        screen.showHabitScreen(habit)
        verify(activity).startActivity(intent)
    }

    @Test
    fun testShowImportScreen() {
        `when`(intentFactory.openDocument()).thenReturn(intent)
        screen.showImportScreen()
        verify(activity).startActivityForResult(intent, REQUEST_OPEN_DOCUMENT)
    }

    @Test
    fun testShowIntroScreen() {
        `when`(intentFactory.startIntroActivity(activity)).thenReturn(intent)
        screen.showIntroScreen()
        verify(activity).startActivity(intent)
    }

    @Test
    fun testShowSettingsScreen() {
        `when`(intentFactory.startSettingsActivity(activity)).thenReturn(intent)
        screen.showSettingsScreen()
        verify(activity).startActivityForResult(eq(intent), anyInt())
    }
}