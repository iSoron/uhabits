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
import org.junit.Before
import org.junit.Test
import java.util.*

class DeleteHabitGroupsCommandTest : BaseUnitTest() {
    private lateinit var command: DeleteHabitGroupsCommand
    private lateinit var selected: LinkedList<HabitGroup>

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        selected = LinkedList()

        // Habits that should be deleted
        for (i in 0..2) {
            val hgr = groupFixtures.createGroupWithShortHabits()
            habitGroupList.add(hgr)
            selected.add(hgr)
        }

        // Extra habit that should not be deleted
        val extraHgr = groupFixtures.createGroupWithShortHabits()
        extraHgr.name = "extra"
        habitGroupList.add(extraHgr)
        command = DeleteHabitGroupsCommand(habitGroupList, selected)
    }

    @Test
    fun testExecute() {
        assertThat(habitGroupList.size(), equalTo(4))
        command.run()
        assertThat(habitGroupList.size(), equalTo(1))
        assertThat(habitGroupList.getByPosition(0).name, equalTo("extra"))
    }
}
