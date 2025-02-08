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

import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitGroup
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArchiveHabitGroupsCommandTest : BaseUnitTest() {
    private lateinit var command: ArchiveHabitGroupsCommand
    private lateinit var hgr: HabitGroup

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        hgr = groupFixtures.createGroupWithShortHabits()
        habitGroupList.add(hgr)
        command = ArchiveHabitGroupsCommand(habitGroupList, listOf(hgr))
    }

    @Test
    fun testExecute() {
        assertFalse(hgr.isArchived)
        command.run()
        assertTrue(hgr.isArchived)
    }
}
