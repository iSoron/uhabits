/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.models

import org.isoron.platform.time.*
import org.isoron.uhabits.models.Checkmark.Companion.CHECKED_MANUAL
import org.isoron.uhabits.models.Frequency.Companion.DAILY
import org.isoron.uhabits.models.ScoreList.Companion.compute
import kotlin.test.*

class ScoreListTest {
    val today = LocalDate(2019, 1, 1)

    @Test
    fun computeWithDailyHabit() {
        val freq = DAILY
        var check = 1
        assertEquals(compute(freq, 0.0, check), 0.051922)
        assertEquals(compute(freq, 0.5, check), 0.525961)
        assertEquals(compute(freq, 0.75, check), 0.762980)

        check = 0
        assertEquals(compute(freq, 0.0, check), 0.0)
        assertEquals(compute(freq, 0.5, check), 0.474038)
        assertEquals(compute(freq, 0.75, check), 0.711058)
    }

    @Test
    fun computeWithNonDailyHabit() {
        var check = 1
        val freq = Frequency(1, 3)
        assertEquals(compute(freq, 0.0, check), 0.017615)
        assertEquals(compute(freq, 0.5, check), 0.508807)
        assertEquals(compute(freq, 0.75, check), 0.754403)

        check = 0
        assertEquals(compute(freq, 0.0, check), 0.0)
        assertEquals(compute(freq, 0.5, check), 0.491192)
        assertEquals(compute(freq, 0.75, check), 0.736788)
    }

    @Test
    fun getValueUntilWithBooleanHabit() {
        val checks = CheckmarkList(DAILY,
                                   HabitType.BOOLEAN_HABIT)
        checks.setManualCheckmarks((0..19).map {
            Checkmark(today.minus(it), CHECKED_MANUAL)
        })
        val scoreList = ScoreList(checks)
        val actual = scoreList.getUntil(today)
        val expected = listOf(Score(today.minus(0), 0.655741),
                              Score(today.minus(1), 0.636888),
                              Score(today.minus(2), 0.617002),
                              Score(today.minus(3), 0.596027),
                              Score(today.minus(4), 0.573903),
                              Score(today.minus(5), 0.550568),
                              Score(today.minus(6), 0.525955),
                              Score(today.minus(7), 0.499994),
                              Score(today.minus(8), 0.472611),
                              Score(today.minus(9), 0.443729),
                              Score(today.minus(10), 0.413265),
                              Score(today.minus(11), 0.381132),
                              Score(today.minus(12), 0.347240),
                              Score(today.minus(13), 0.311491),
                              Score(today.minus(14), 0.273785),
                              Score(today.minus(15), 0.234014),
                              Score(today.minus(16), 0.192065),
                              Score(today.minus(17), 0.147818),
                              Score(today.minus(18), 0.101148),
                              Score(today.minus(19), 0.051922))

        assertEquals(expected, actual)
    }
}