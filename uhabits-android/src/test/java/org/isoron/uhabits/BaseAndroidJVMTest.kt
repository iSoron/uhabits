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
package org.isoron.uhabits

import com.nhaarman.mockitokotlin2.spy
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.memory.MemoryModelFactory
import org.isoron.uhabits.core.tasks.SingleThreadTaskRunner
import org.isoron.uhabits.core.test.HabitFixtures
import org.isoron.uhabits.core.utils.DateUtils.Companion.setFixedLocalTime
import org.isoron.uhabits.core.utils.DateUtils.Companion.setStartDayOffset
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
open class BaseAndroidJVMTest {
    private lateinit var habitList: HabitList
    protected lateinit var fixtures: HabitFixtures
    private lateinit var modelFactory: MemoryModelFactory
    private lateinit var taskRunner: SingleThreadTaskRunner
    private lateinit var commandRunner: CommandRunner

    @Before
    open fun setUp() {
        val fixedLocalTime = 1422172800000L
        setFixedLocalTime(fixedLocalTime)
        setStartDayOffset(0, 0)
        modelFactory = MemoryModelFactory()
        habitList = spy(modelFactory.buildHabitList())
        fixtures = HabitFixtures(modelFactory, habitList)
        taskRunner = SingleThreadTaskRunner()
        commandRunner = CommandRunner(taskRunner)
    }

    @After
    fun tearDown() {
        setFixedLocalTime(null)
        setStartDayOffset(0, 0)
    }

    @Test
    fun nothing() {
    }
}
