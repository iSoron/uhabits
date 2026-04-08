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
package org.isoron.uhabits.core.commands

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class CreateHabitGroupCommandTest : BaseUnitTest() {
    private lateinit var command: CreateHabitGroupCommand
    private lateinit var model: HabitGroup

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        model = groupFixtures.createEmptyHabitGroup()
        model.name = "New habit group"
        model.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        command = CreateHabitGroupCommand(modelFactory, habitGroupList, model)
    }

    @Test
    fun testExecute() {
        assertTrue(habitGroupList.isEmpty)
        command.run()
        assertThat(habitGroupList.size(), equalTo(1))
        val hgr = habitGroupList.getByPosition(0)
        assertThat(hgr.name, equalTo(model.name))
    }
}
