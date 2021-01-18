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
package org.isoron.uhabits.core.models

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test

class StreakListTest : BaseUnitTest() {
    private lateinit var habit: Habit
    private lateinit var streaks: StreakList
    private lateinit var today: Timestamp

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createLongHabit()
        habit.frequency = Frequency.DAILY
        habit.recompute()
        streaks = habit.streaks
        today = getToday()
    }

    @Test
    @Throws(Exception::class)
    fun testGetBest() {
        var best = streaks.getBest(4)
        assertThat(best.size, equalTo(4))
        assertThat(best[0].length, equalTo(4))
        assertThat(best[1].length, equalTo(3))
        assertThat(best[2].length, equalTo(5))
        assertThat(best[3].length, equalTo(6))
        best = streaks.getBest(2)
        assertThat(best.size, equalTo(2))
        assertThat(best[0].length, equalTo(5))
        assertThat(best[1].length, equalTo(6))
    }

    @Test
    fun testGetBest_withUnknowns() {
        habit.originalEntries.clear()
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(5), Entry.NO))
        habit.recompute()
        val best = streaks.getBest(5)
        assertThat(best.size, equalTo(1))
        assertThat(best[0].length, equalTo(1))
    }
}
