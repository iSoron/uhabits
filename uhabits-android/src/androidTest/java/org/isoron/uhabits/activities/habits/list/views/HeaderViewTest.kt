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
package org.isoron.uhabits.activities.habits.list.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import org.isoron.uhabits.BaseViewTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HeaderViewTest : BaseViewTest() {
    private lateinit var view: HeaderView

    @Before
    override fun setUp() {
        super.setUp()
        prefs = mock()
        view = HeaderView(targetContext, prefs, mock())
        view.setState(HeaderState(buttonCount = 5, maxDataOffset = 0))
        measureView(view, dpToPixels(600), dpToPixels(48))
    }

    @Test
    @Throws(Exception::class)
    fun testRender() {
        every { prefs.isCheckmarkSequenceReversed } returns false
        assertRenders(view, PATH + "render.png")
        verify { prefs.isCheckmarkSequenceReversed }
        verifyNoMoreCalls(prefs)
    }

    @Test
    @Throws(Exception::class)
    fun testRender_reverse() {
        every { prefs.isCheckmarkSequenceReversed } returns true
        assertRenders(view, PATH + "render_reverse.png")
        verify { prefs.isCheckmarkSequenceReversed }
        verifyNoMoreCalls(prefs)
    }

    companion object {
        const val PATH = "habits/list/HeaderView/"
    }
}
