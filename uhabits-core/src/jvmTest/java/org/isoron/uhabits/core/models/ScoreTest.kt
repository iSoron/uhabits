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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.IsCloseTo.closeTo
import org.junit.Test

class ScoreTest {
    @Test
    fun testCompute() {
        val frequency = 1.0 // daily
        val previousScore = 0.5
        val checkmarkValue = 1.0

        // Expected value based on formula:
        // multiplier = 0.5 ^ (sqrt(1.0) / 13.0) = 0.5 ^ (1/13) ≈ 0.9479
        // score = 0.5 * 0.9479 + 1.0 * (1 - 0.9479) ≈ 0.47395 + 0.0521 ≈ 0.526
        val score = Score.compute(frequency, previousScore, checkmarkValue)
        assertThat(score, closeTo(0.526, 0.001))
    }

    @Test
    fun testComputeStayAtZero() {
        val score = Score.compute(1.0, 0.0, 0.0)
        assertThat(score, closeTo(0.0, 0.001))
    }

    @Test
    fun testComputeStayAtOne() {
        val score = Score.compute(1.0, 1.0, 1.0)
        assertThat(score, closeTo(1.0, 0.001))
    }
}
