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
import org.hamcrest.number.IsCloseTo.closeTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Score.Companion.compute
import org.junit.Before
import org.junit.Test

class ScoreTest : BaseUnitTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun test_compute_withDailyHabit() {
        var check = 1
        val freq = 1.0
        assertThat(compute(freq, 0.0, check.toDouble()), closeTo(0.051922, E))
        assertThat(compute(freq, 0.5, check.toDouble()), closeTo(0.525961, E))
        assertThat(compute(freq, 0.75, check.toDouble()), closeTo(0.762981, E))
        check = 0
        assertThat(compute(freq, 0.0, check.toDouble()), closeTo(0.0, E))
        assertThat(compute(freq, 0.5, check.toDouble()), closeTo(0.474039, E))
        assertThat(compute(freq, 0.75, check.toDouble()), closeTo(0.711058, E))
    }

    @Test
    fun test_compute_withNonDailyHabit() {
        var check = 1
        val freq = 1 / 3.0
        assertThat(compute(freq, 0.0, check.toDouble()), closeTo(0.030314, E))
        assertThat(compute(freq, 0.5, check.toDouble()), closeTo(0.515157, E))
        assertThat(compute(freq, 0.75, check.toDouble()), closeTo(0.757578, E))
        check = 0
        assertThat(compute(freq, 0.0, check.toDouble()), closeTo(0.0, E))
        assertThat(compute(freq, 0.5, check.toDouble()), closeTo(0.484842, E))
        assertThat(compute(freq, 0.75, check.toDouble()), closeTo(0.727263, E))
    }

    companion object {
        private const val E = 1e-6
    }
}
