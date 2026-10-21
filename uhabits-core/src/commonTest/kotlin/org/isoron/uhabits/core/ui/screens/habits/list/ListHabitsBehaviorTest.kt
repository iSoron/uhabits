/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.ui.screens.habits.list

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import org.isoron.platform.io.UserFile
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.callbacks.NumberPickerCallback
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListHabitsBehaviorTest : BaseUnitTest() {
    private val dirFinder: ListHabitsBehavior.DirFinder = mock()

    private val prefs: Preferences = mock()
    private lateinit var behavior: ListHabitsBehavior

    private val screen: ListHabitsBehavior.Screen = mock()
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit

    private var capturedPicker: NumberPickerCallback? = null

    private val bugReporter: ListHabitsBehavior.BugReporter = mock()

    @BeforeTest
    override fun setUp() {
        super.setUp()
        habit1 = fixtures.createShortHabit()
        habit2 = fixtures.createNumericalHabit()
        habitList.add(habit1)
        habitList.add(habit2)
        habitList = spy(habitList)
        behavior = ListHabitsBehavior(
            habitList,
            dirFinder,
            taskRunner,
            screen,
            commandRunner,
            prefs,
            bugReporter
        )
    }

    @Test
    fun testOnEdit() {
        val today = getToday()
        every {
            screen.showNumberPopup(any(), any(), any())
        } calls { args ->
            capturedPicker = args.arg<NumberPickerCallback>(2)
            Unit
        }
        behavior.onEdit(habit2.id!!, today, 0f, 0f)
        verify {
            screen.showNumberPopup(0.1, "", any())
        }
        capturedPicker!!.onNumberPicked(100.0, "")
        assertEquals(100000, habit2.computedEntries.get(today).value)
    }

    @Test
    fun testOnExportCSV() = runTest {
        val outputDir = createTempDir()
        every { dirFinder.getCSVOutputDir() } returns outputDir
        behavior.onExportCSV()
        taskRunner.await()
        verify { screen.showSendFileScreen(any()) }
        val files = outputDir.listFiles()
        assertEquals(1, files!!.size)
    }

    @Test
    fun testOnExportCSV_fail() = runTest {
        val mockDir: UserFile = mock()
        every { mockDir.resolve(any()) } throws RuntimeException("not writable")
        every { dirFinder.getCSVOutputDir() } returns mockDir
        behavior.onExportCSV()
        taskRunner.await()
        verify { screen.showMessage(ListHabitsBehavior.Message.COULD_NOT_EXPORT) }
    }

    @Test
    fun testOnHabitClick() {
        behavior.onClickHabit(habit1.id!!)
        verify { screen.showHabitScreen(habit1.id!!) }
    }

    @Test
    fun testReorderHabit() {
        habitList.primaryOrder = HabitList.Order.BY_POSITION
        behavior.reorderHabit(habit2.id!!, habit1.id!!, true)
        assertEquals(habit2, habitList.getByPosition(0))
        assertEquals(habit1, habitList.getByPosition(1))
    }

    @Test
    fun testOnRepairDB() {
        behavior.onRepairDB()
        verify { habitList.repair() }
        verify { screen.showMessage(ListHabitsBehavior.Message.DATABASE_REPAIRED) }
    }

    @Test
    fun testOnSendBugReport() {
        every { bugReporter.getBugReport() } returns "hello"
        behavior.onSendBugReport()
        verify { bugReporter.dumpBugReportToFile() }
        verify { screen.showSendBugReportToDeveloperScreen("hello") }
        every { bugReporter.getBugReport() } throws RuntimeException()
        behavior.onSendBugReport()
        verify { screen.showMessage(ListHabitsBehavior.Message.COULD_NOT_GENERATE_BUG_REPORT) }
    }

    @Test
    fun testOnStartup_firstLaunch() {
        val today = getToday()
        every { prefs.isFirstRun } returns true
        behavior.onStartup()
        verify { prefs.isFirstRun = false }
        verify { prefs.updateLastHint(-1, today) }
        verify { screen.showIntroScreen() }
    }

    @Test
    fun testOnStartup_notFirstLaunch() {
        every { prefs.isFirstRun } returns false
        behavior.onStartup()
        verify { prefs.incrementLaunchCount() }
    }

    @Test
    fun testOnToggle() {
        assertTrue(habit1.isCompletedToday())
        behavior.onToggle(
            habitId = habit1.id!!,
            date = getToday(),
            value = Entry.NO,
            notes = "",
            x = 0f,
            y = 0f
        )
        assertFalse(habit1.isCompletedToday())
    }
}
