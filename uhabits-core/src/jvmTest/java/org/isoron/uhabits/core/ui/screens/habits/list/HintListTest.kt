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
package org.isoron.uhabits.core.ui.screens.habits.list

import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito

class HintListTest : BaseUnitTest() {
    private var hintList: HintList? = null
    private lateinit var hints: Array<String>

    @Mock
    private val prefs: Preferences? = null
    private var today: Timestamp? = null
    private var yesterday: Timestamp? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        today = getToday()
        yesterday = today!!.minus(1)
        hints = arrayOf("hint1", "hint2", "hint3")
        hintList = HintList(prefs!!, hints)
    }

    @Test
    @Throws(Exception::class)
    fun pop() {
        Mockito.`when`(prefs!!.lastHintNumber).thenReturn(-1)
        assertThat(hintList!!.pop(), equalTo("hint1"))
        Mockito.verify(prefs).updateLastHint(0, today)
        Mockito.`when`(prefs.lastHintNumber).thenReturn(2)
        assertNull(hintList!!.pop())
    }

    @Test
    @Throws(Exception::class)
    fun shouldShow() {
        Mockito.`when`(prefs!!.lastHintTimestamp).thenReturn(today)
        TestCase.assertFalse(hintList!!.shouldShow())
        Mockito.`when`(prefs.lastHintTimestamp).thenReturn(yesterday)
        assertTrue(hintList!!.shouldShow())
    }
}
