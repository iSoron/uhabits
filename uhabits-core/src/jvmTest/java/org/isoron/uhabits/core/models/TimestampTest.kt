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

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test

class TimestampTest : BaseUnitTest() {
    @Test
    @Throws(Exception::class)
    fun testCompare() {
        val t1 = getToday()
        val t2 = t1.minus(1)
        val t3 = t1.plus(3)
        assertThat(t1.compareTo(t2), greaterThan(0))
        assertThat(t1.compareTo(t1), equalTo(0))
        assertThat(t1.compareTo(t3), lessThan(0))
        assertTrue(t1.isNewerThan(t2))
        assertFalse(t1.isNewerThan(t1))
        assertFalse(t2.isNewerThan(t1))
        assertTrue(t2.isOlderThan(t1))
        assertFalse(t1.isOlderThan(t2))
    }

    @Test
    @Throws(Exception::class)
    fun testDaysUntil() {
        val t = getToday()
        assertThat(t.daysUntil(t), equalTo(0))
        assertThat(t.daysUntil(t.plus(1)), equalTo(1))
        assertThat(t.daysUntil(t.plus(3)), equalTo(3))
        assertThat(t.daysUntil(t.plus(300)), equalTo(300))
        assertThat(t.daysUntil(t.minus(1)), equalTo(-1))
        assertThat(t.daysUntil(t.minus(3)), equalTo(-3))
        assertThat(t.daysUntil(t.minus(300)), equalTo(-300))
    }

    @Test
    @Throws(Exception::class)
    fun testInexact() {
        val t = Timestamp(1578054764000L)
        assertThat(t.unixTime, equalTo(1578009600000L))
    }
}
