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

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import java.io.IOException
import java.nio.file.Files

class ListHabitsBehaviorTest : BaseUnitTest() {
    @Mock
    private val dirFinder: ListHabitsBehavior.DirFinder? = null

    @Mock
    private val prefs: Preferences? = null
    private var behavior: ListHabitsBehavior? = null

    @Mock
    private val screen: ListHabitsBehavior.Screen? = null
    private var habit1: Habit? = null
    private var habit2: Habit? = null

    @Captor
    var picker: ArgumentCaptor<ListHabitsBehavior.NumberPickerCallback>? = null

    @Mock
    private val bugReporter: ListHabitsBehavior.BugReporter? = null

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit1 = fixtures.createShortHabit()
        habit2 = fixtures.createNumericalHabit()
        habitList.add(habit1!!)
        habitList.add(habit2!!)
        Mockito.clearInvocations(habitList)
        behavior = ListHabitsBehavior(
            habitList,
            dirFinder!!,
            taskRunner,
            screen!!,
            commandRunner,
            prefs!!,
            bugReporter!!
        )
    }

    @Test
    fun testOnEdit() {
        behavior!!.onEdit(habit2!!, getToday())
        Mockito.verify(screen)!!.showNumberPicker(
            ArgumentMatchers.eq(0.1),
            ArgumentMatchers.eq("miles"),
            picker!!.capture()
        )
        picker!!.value.onNumberPicked(100.0)
        val today = getTodayWithOffset()
        assertThat(
            habit2!!.computedEntries.get(today).value,
            CoreMatchers.equalTo(100000)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testOnExportCSV() {
        val outputDir = Files.createTempDirectory("CSV").toFile()
        Mockito.`when`(dirFinder!!.csvOutputDir).thenReturn(outputDir)
        behavior!!.onExportCSV()
        Mockito.verify(screen)!!.showSendFileScreen(ArgumentMatchers.any())
        assertThat(
            FileUtils.listFiles(outputDir, null, false).size,
            CoreMatchers.equalTo(1)
        )
        FileUtils.deleteDirectory(outputDir)
    }

    @Test
    @Throws(Exception::class)
    fun testOnExportCSV_fail() {
        val outputDir = Files.createTempDirectory("CSV").toFile()
        outputDir.setWritable(false)
        Mockito.`when`(dirFinder!!.csvOutputDir).thenReturn(outputDir)
        behavior!!.onExportCSV()
        Mockito.verify(screen)!!.showMessage(ListHabitsBehavior.Message.COULD_NOT_EXPORT)
        assertTrue(outputDir.delete())
    }

    @Test
    fun testOnHabitClick() {
        behavior!!.onClickHabit(habit1!!)
        Mockito.verify(screen)!!.showHabitScreen(
            habit1!!
        )
    }

    @Test
    fun testOnHabitReorder() {
        val from = habit1
        val to = habit2
        behavior!!.onReorderHabit(from!!, to!!)
        Mockito.verify(habitList)!!.reorder(from, to)
    }

    @Test
    fun testOnRepairDB() {
        behavior!!.onRepairDB()
        Mockito.verify(habitList)!!.repair()
        Mockito.verify(screen)!!.showMessage(ListHabitsBehavior.Message.DATABASE_REPAIRED)
    }

    @Test
    @Throws(IOException::class)
    fun testOnSendBugReport() {
        Mockito.`when`(bugReporter!!.bugReport).thenReturn("hello")
        behavior!!.onSendBugReport()
        Mockito.verify(bugReporter).dumpBugReportToFile()
        Mockito.verify(screen)!!.showSendBugReportToDeveloperScreen("hello")
        Mockito.`when`(bugReporter.bugReport).thenThrow(IOException())
        behavior!!.onSendBugReport()
        Mockito.verify(screen)!!
            .showMessage(ListHabitsBehavior.Message.COULD_NOT_GENERATE_BUG_REPORT)
    }

    @Test
    fun testOnStartup_firstLaunch() {
        val today = getToday()
        Mockito.`when`(prefs!!.isFirstRun).thenReturn(true)
        behavior!!.onStartup()
        Mockito.verify(prefs).isFirstRun = false
        Mockito.verify(prefs).updateLastHint(-1, today)
        Mockito.verify(screen)!!.showIntroScreen()
    }

    @Test
    fun testOnStartup_notFirstLaunch() {
        Mockito.`when`(prefs!!.isFirstRun).thenReturn(false)
        behavior!!.onStartup()
        Mockito.verify(prefs).incrementLaunchCount()
    }

    @Test
    fun testOnToggle() {
        assertTrue(habit1!!.isCompletedToday())
        behavior!!.onToggle(habit1!!, getToday(), Entry.NO)
        assertFalse(habit1!!.isCompletedToday())
    }
}
