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
package org.isoron.uhabits.activities.habits.list.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.isoron.uhabits.BaseViewTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@MediumTest
class HeaderViewTest : BaseViewTest() {
    private var view: HeaderView? = null

    @Before
    override fun setUp() {
        super.setUp()
        prefs = mock()
        view = HeaderView(targetContext, prefs, mock())
        view!!.buttonCount = 5
        measureView(view, dpToPixels(600), dpToPixels(48))
    }

    @Test
    @Throws(Exception::class)
    fun testRender() {
        Mockito.`when`(prefs.isCheckmarkSequenceReversed).thenReturn(false)
        assertRenders(view, PATH + "render.png")
        Mockito.verify(prefs).isCheckmarkSequenceReversed
        Mockito.verifyNoMoreInteractions(prefs)
    }

    @Test
    @Throws(Exception::class)
    fun testRender_reverse() {
        doReturn(true).whenever(prefs).isCheckmarkSequenceReversed
        // Mockito.`when`(prefs.isCheckmarkSequenceReversed).thenReturn(true)
        assertRenders(view, PATH + "render_reverse.png")
        Mockito.verify(prefs).isCheckmarkSequenceReversed
        Mockito.verifyNoMoreInteractions(prefs)
    }

    companion object {
        const val PATH = "habits/list/HeaderView/"
    }
}
