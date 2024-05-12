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

class FrequencyTest {
    @Test
    fun testNormalization() {
        val f = Frequency(7, 7)
        assertThat(f.numerator, equalTo(1))
        assertThat(f.denominator, equalTo(1))
    }

    @Test
    fun testToDouble() {
        assertThat(Frequency(1, 1).toDouble(), equalTo(1.0))
        assertThat(Frequency(1, 2).toDouble(), equalTo(0.5))
        assertThat(Frequency(3, 4).toDouble(), equalTo(0.75))
    }
}
