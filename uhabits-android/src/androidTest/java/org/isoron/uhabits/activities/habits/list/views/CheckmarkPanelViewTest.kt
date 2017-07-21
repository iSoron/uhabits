/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

import android.support.test.filters.*
import android.support.test.runner.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.Checkmark.*
import org.isoron.uhabits.utils.*
import org.junit.*
import org.junit.runner.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class CheckmarkPanelViewTest : BaseViewTest() {

    private val PATH = "habits/list/CheckmarkPanelView"
    private lateinit var view: CheckmarkPanelView

    @Before
    override fun setUp() {
        super.setUp()
        prefs.isCheckmarkSequenceReversed = false

        val checkmarks = intArrayOf(CHECKED_EXPLICITLY,
                                    CHECKED_EXPLICITLY,
                                    CHECKED_IMPLICITLY,
                                    UNCHECKED,
                                    UNCHECKED,
                                    UNCHECKED,
                                    CHECKED_EXPLICITLY)

        view = component.getCheckmarkPanelViewFactory().create().apply {
            values = checkmarks
            buttonCount = 4
            color = PaletteUtils.getAndroidTestColor(7)
        }
        view.onAttachedToWindow()
        measureView(view, dpToPixels(200), dpToPixels(200))
    }

    @After
    public override fun tearDown() {
//        view.onDetachedFromWindow()
        super.tearDown()
    }

    @Test
    fun testRender() {
        assertRenders(view, "$PATH/render.png")
    }

    @Test
    fun testRender_withDifferentColor() {
        view.color = PaletteUtils.getAndroidTestColor(1)
        assertRenders(view, "$PATH/render_different_color.png")
    }

    @Test
    fun testRender_Reversed() {
        prefs.isCheckmarkSequenceReversed = true
        assertRenders(view, "$PATH/render_reversed.png")
    }

    @Test
    fun testRender_withOffset() {
        view.dataOffset = 3
        assertRenders(view, "$PATH/render_offset.png")
    }

    @Test
    fun testToggle() {
        var timestamps = mutableListOf<Long>()
        view.onToggle = { timestamps.add(it) }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(timestamps, equalTo(listOf(day(0), day(2), day(3))))
    }

    @Test
    fun testToggle_withOffset() {
        var timestamps = LongArray(0)
        view.dataOffset = 3
        view.onToggle = { timestamps += it }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(timestamps, equalTo(longArrayOf(day(3), day(5), day(6))))
    }
}