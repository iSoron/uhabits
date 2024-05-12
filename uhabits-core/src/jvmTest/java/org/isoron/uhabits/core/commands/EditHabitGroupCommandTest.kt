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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.junit.Before
import org.junit.Test

class EditHabitGroupCommandTest : BaseUnitTest() {
    private lateinit var command: EditHabitGroupCommand
    private lateinit var hgr: HabitGroup
    private lateinit var modified: HabitGroup
    private lateinit var today: Timestamp

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        hgr = groupFixtures.createGroupWithShortHabits()
        hgr.name = "original"
        hgr.recompute()
        habitGroupList.add(hgr)
        modified = groupFixtures.createEmptyHabitGroup()
        modified.copyFrom(hgr)
        modified.name = "modified"
        habitGroupList.add(modified)
        today = getTodayWithOffset()
    }

    @Test
    fun testExecute() {
        command = EditHabitGroupCommand(habitGroupList, hgr.id!!, modified)
        val originalScore = hgr.scores[today].value
        assertThat(hgr.name, equalTo("original"))
        command.run()
        assertThat(hgr.name, equalTo("modified"))
        assertThat(hgr.scores[today].value, equalTo(originalScore))
    }
}
