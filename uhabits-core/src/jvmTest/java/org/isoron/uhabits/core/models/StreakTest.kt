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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreakTest {
    @Test
    fun testLength() {
        val start = Timestamp(0)
        val end = Timestamp(Timestamp.DAY_LENGTH * 2)
        val streak = Streak(start, end)
        assertThat(streak.length, equalTo(3))
    }

    @Test
    fun testCompareLonger() {
        val s1 = Streak(Timestamp(0), Timestamp(Timestamp.DAY_LENGTH)) // length 2
        val s2 = Streak(Timestamp(0), Timestamp(Timestamp.DAY_LENGTH * 2)) // length 3

        assertTrue(s2.compareLonger(s1) > 0)
        assertTrue(s1.compareLonger(s2) < 0)
    }

    @Test
    fun testCompareLongerSameLength() {
        val s1 = Streak(Timestamp(0), Timestamp(Timestamp.DAY_LENGTH))
        val s2 = Streak(Timestamp(Timestamp.DAY_LENGTH), Timestamp(Timestamp.DAY_LENGTH * 2))

        assertTrue(s2.compareLonger(s1) > 0) // s2 is newer
    }

    @Test
    fun testIsInStreak() {
        val streak = Streak(Timestamp(Timestamp.DAY_LENGTH), Timestamp(Timestamp.DAY_LENGTH * 3))

        assertFalse(streak.isInStreak(Timestamp(0)))
        assertTrue(streak.isInStreak(Timestamp(Timestamp.DAY_LENGTH)))
        assertTrue(streak.isInStreak(Timestamp(Timestamp.DAY_LENGTH * 2)))
        assertTrue(streak.isInStreak(Timestamp(Timestamp.DAY_LENGTH * 3)))
        assertFalse(streak.isInStreak(Timestamp(Timestamp.DAY_LENGTH * 4)))
    }
}
