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
import kotlin.test.assertFailsWith

class NumericalHabitTypeTest {
    @Test
    fun testFromInt() {
        assertThat(NumericalHabitType.fromInt(0), equalTo(NumericalHabitType.AT_LEAST))
        assertThat(NumericalHabitType.fromInt(1), equalTo(NumericalHabitType.AT_MOST))
    }

    @Test
    fun testFromIntInvalid() {
        assertFailsWith<IllegalStateException> {
            NumericalHabitType.fromInt(2)
        }
    }
}
