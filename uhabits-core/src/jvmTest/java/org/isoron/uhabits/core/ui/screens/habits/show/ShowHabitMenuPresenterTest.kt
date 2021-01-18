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
package org.isoron.uhabits.core.ui.screens.habits.show

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.junit.Test
import java.nio.file.Files

class ShowHabitMenuPresenterTest : BaseUnitTest() {
    private lateinit var system: ShowHabitMenuPresenter.System
    private lateinit var screen: ShowHabitMenuPresenter.Screen
    private lateinit var habit: Habit
    private lateinit var menu: ShowHabitMenuPresenter

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        system = mock()
        screen = mock()
        habit = fixtures.createShortHabit()
        menu = ShowHabitMenuPresenter(
            commandRunner,
            habit,
            habitList,
            screen,
            system,
            taskRunner
        )
    }

    @Test
    fun testOnEditHabit() {
        menu.onEditHabit()
        verify(screen).showEditHabitScreen(habit)
    }

    @Test
    @Throws(Exception::class)
    fun testOnExport() {
        val outputDir = Files.createTempDirectory("CSV").toFile()
        whenever(system.getCSVOutputDir()).thenReturn(outputDir)
        menu.onExportCSV()
        assertThat(FileUtils.listFiles(outputDir, null, false).size, equalTo(1))
        FileUtils.deleteDirectory(outputDir)
    }
}
