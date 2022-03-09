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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Test

class WeekdayListTest : BaseUnitTest() {
    @Test
    fun test() {
        val daysInt = 124
        val daysArray = booleanArrayOf(false, false, true, true, true, true, true)
        var list = WeekdayList(daysArray)
        assertThat(list.toArray(), equalTo(daysArray))
        assertThat(list.toInteger(), equalTo(daysInt))
        list = WeekdayList(daysInt)
        assertThat(list.toArray(), equalTo(daysArray))
        assertThat(list.toInteger(), equalTo(daysInt))
    }

    @Test
    fun testEmpty() {
        val list = WeekdayList(0)
        assertTrue(list.isEmpty)
        assertFalse(WeekdayList.EVERY_DAY.isEmpty)
    }

    @Test
    fun testWeekdayList_IntConstructor_toString() {
        val string = WeekdayList(0).toString()
        assertThat(string, equalTo("{weekdays: [false,false,false,false,false,false,false]}"))
    }

    @Test
    fun testWeekdayList_BooleanArrayConstructor_toString() {
        val string = WeekdayList(
            booleanArrayOf(false, false, true, true, true, true, true)
        ).toString()
        assertThat(string, equalTo("{weekdays: [false,false,true,true,true,true,true]}"))
    }
}
