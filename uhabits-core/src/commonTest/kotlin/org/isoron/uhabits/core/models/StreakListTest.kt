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
package org.isoron.uhabits.core.models

import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StreakListTest : BaseUnitTest() {
    private lateinit var habit: Habit
    private lateinit var streaks: StreakList
    private lateinit var today: LocalDate

    @BeforeTest
    override fun setUp() {
        super.setUp()
        habit = fixtures.createLongHabit()
        habit.frequency = Frequency.DAILY
        habit.recompute()
        streaks = habit.streaks
        today = getToday()
    }

    @Test
    fun testGetBest() {
        var best = streaks.getBest(4)
        assertEquals(4, best.size)
        assertEquals(4, best[0].length)
        assertEquals(3, best[1].length)
        assertEquals(5, best[2].length)
        assertEquals(6, best[3].length)
        best = streaks.getBest(2)
        assertEquals(2, best.size)
        assertEquals(5, best[0].length)
        assertEquals(6, best[1].length)
    }

    @Test
    fun testGetBest_withUnknowns() {
        habit.originalEntries.clear()
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(5), Entry.NO))
        habit.recompute()
        val best = streaks.getBest(5)
        assertEquals(1, best.size)
        assertEquals(1, best[0].length)
    }

    @Test
    fun testGetCurrentStreak_completedToday() {
        habit.originalEntries.clear()
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(1), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))
        habit.recompute()
        val current = streaks.getCurrentStreak(today)
        assertNotNull(current)
        assertEquals(3, current.length)
        assertEquals(today.minus(2), current.start)
        assertEquals(today, current.end)
    }

    @Test
    fun testGetCurrentStreak_completedYesterdayNotToday() {
        habit.originalEntries.clear()
        habit.originalEntries.add(Entry(today.minus(1), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))
        habit.recompute()
        val current = streaks.getCurrentStreak(today)
        assertNull(current)
    }

    @Test
    fun testGetCurrentStreak_brokenTwoDaysAgo() {
        habit.originalEntries.clear()
        habit.originalEntries.add(Entry(today.minus(2), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(3), Entry.YES_MANUAL))
        habit.recompute()
        val current = streaks.getCurrentStreak(today)
        assertNull(current)
    }

    @Test
    fun testGetCurrentStreak_empty() {
        habit.originalEntries.clear()
        habit.recompute()
        val current = streaks.getCurrentStreak(today)
        assertNull(current)
    }
}
