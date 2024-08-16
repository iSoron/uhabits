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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.apache.commons.io.FileUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.nio.file.Files
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListHabitsBehaviorTest : BaseUnitTest() {
    private val dirFinder: ListHabitsBehavior.DirFinder = mock()

    private val prefs: Preferences = mock()
    private lateinit var behavior: ListHabitsBehavior

    private val screen: ListHabitsBehavior.Screen = mock()
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit
    private lateinit var hgr1: HabitGroup
    private lateinit var hgr2: HabitGroup

    var picker: KArgumentCaptor<ListHabitsBehavior.NumberPickerCallback> = argumentCaptor()
    var picker2: KArgumentCaptor<ListHabitsBehavior.CheckMarkDialogCallback> = argumentCaptor()

    private val bugReporter: ListHabitsBehavior.BugReporter = mock()

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit1 = fixtures.createShortHabit()
        habit2 = fixtures.createNumericalHabit()
        hgr1 = groupFixtures.createGroupWithShortHabits(id = 2L)
        hgr2 = groupFixtures.createGroupWithNumericalHabits(id = 4L)
        habitList.add(habit1)
        habitList.add(habit2)
        habitGroupList.add(hgr1)
        habitGroupList.add(hgr2)
        clearInvocations(habitList)
        clearInvocations(habitGroupList)
        behavior = ListHabitsBehavior(
            habitList,
            habitGroupList,
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
        behavior.onEdit(habit2, getToday())
        verify(screen).showNumberPopup(
            eq(0.1),
            eq(""),
            picker.capture()
        )
        picker.lastValue.onNumberPicked(100.0, "", 0f, 0f)
        val today = getTodayWithOffset()
        assertThat(habit2.computedEntries.get(today).value, equalTo(100000))
    }

    @Test
    fun testOnEditBoolean() {
        behavior.onEdit(habit1, getToday())
        verify(screen).showCheckmarkPopup(
            eq(2),
            eq(""),
            eq(PaletteColor(8)),
            picker2.capture()
        )
        picker2.lastValue.onNotesSaved(2, "", 0f, 0f)
        val today = getTodayWithOffset()
        assertThat(habit1.computedEntries.get(today).value, equalTo(2))
    }

    @Test
    @Throws(Exception::class)
    fun testOnExportCSV() {
        val outputDir = Files.createTempDirectory("CSV").toFile()
        whenever(dirFinder.getCSVOutputDir()).thenReturn(outputDir)
        behavior.onExportCSV()
        verify(screen).showSendFileScreen(any())
        assertThat(FileUtils.listFiles(outputDir, null, false).size, equalTo(1))
        FileUtils.deleteDirectory(outputDir)
    }

    @Test
    @Throws(Exception::class)
    fun testOnExportCSV_fail() {
        val outputDir = Files.createTempDirectory("CSV").toFile()
        outputDir.setWritable(false)
        whenever(dirFinder.getCSVOutputDir()).thenReturn(outputDir)
        behavior.onExportCSV()
        verify(screen).showMessage(ListHabitsBehavior.Message.COULD_NOT_EXPORT)
        assertTrue(outputDir.delete())
    }

    @Test
    fun testOnHabitClick() {
        behavior.onClickHabit(habit1)
        verify(screen).showHabitScreen(habit1)
    }

    @Test
    fun testOnHabitGroupClick() {
        behavior.onClickHabitGroup(hgr1)
        verify(screen).showHabitGroupScreen(hgr1)
    }

    @Test
    fun testOnHabitReorder() {
        val from = habit1
        val to = habit2
        behavior.onReorderHabit(from, to)
        verify(habitList).reorder(from, to)
    }

    @Test
    fun testOnHabitGroupReorder() {
        val from = hgr1
        val to = hgr2
        behavior.onReorderHabitGroup(from, to)
        verify(habitGroupList).reorder(from, to)
    }

    @Test
    fun testOnRepairDB() {
        behavior.onRepairDB()
        verify(habitList).repair()
        verify(screen).showMessage(ListHabitsBehavior.Message.DATABASE_REPAIRED)
    }

    @Test
    @Throws(IOException::class)
    fun testOnSendBugReport() {
        whenever(bugReporter.getBugReport()).thenReturn("hello")
        behavior.onSendBugReport()
        verify(bugReporter).dumpBugReportToFile()
        verify(screen).showSendBugReportToDeveloperScreen("hello")
        whenever(bugReporter.getBugReport()).thenThrow(IOException())
        behavior.onSendBugReport()
        verify(screen).showMessage(ListHabitsBehavior.Message.COULD_NOT_GENERATE_BUG_REPORT)
    }

    @Test
    fun testOnStartup_firstLaunch() {
        val today = getToday()
        whenever(prefs.isFirstRun).thenReturn(true)
        behavior.onStartup()
        verify(prefs).isFirstRun = false
        verify(prefs).updateLastHint(-1, today)
        verify(screen).showIntroScreen()
    }

    @Test
    fun testOnStartup_notFirstLaunch() {
        whenever(prefs.isFirstRun).thenReturn(false)
        behavior.onStartup()
        verify(prefs).incrementLaunchCount()
    }

    @Test
    fun testOnToggle() {
        assertTrue(habit1.isCompletedToday())
        behavior.onToggle(
            habit = habit1,
            timestamp = getToday(),
            value = Entry.NO,
            notes = "",
            x = 0f,
            y = 0f
        )
        assertFalse(habit1.isCompletedToday())
    }
}
