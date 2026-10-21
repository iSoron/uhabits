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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.utils.PaletteUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class NumberPanelViewTest : BaseViewTest() {

    private val PATH = "habits/list/NumberPanelView"
    private lateinit var view: NumberPanelView
    private lateinit var state: NumberPanelState

    @Before
    override fun setUp() {
        super.setUp()
        val checkmarks = listOf(
            1400.0,
            5300.0,
            0.0,
            14600.0,
            2500.0,
            45000.0
        )

        state = NumberPanelState(
            checkmarks,
            listOf(false, true, false, true),
            PaletteUtils.getAndroidTestColor(7),
            "steps",
            emptyList(),
            4
        )
        view = component.getNumberPanelViewFactory().create().apply {
            setState(state)
        }
        view.onAttachedToWindow()
        measureView(view, dpToPixels(200), dpToPixels(200))
    }

    @After
    public override fun tearDown() {
        view.onDetachedFromWindow()
    }

    @Test
    fun testRender() {
        assertRenders(view, "$PATH/render.png")
    }

    @Test
    fun testSetState_idempotent() {
        val before = view.buttons.toList()
        view.setState(state)
        assertThat(view.buttons.size, equalTo(before.size))
        for (i in before.indices) {
            assertThat(view.buttons[i], sameInstance(before[i]))
        }
    }

    @Test
    fun testEdit() {
        val dates = mutableListOf<LocalDate>()
        view.onEdit = { t -> dates.plusAssign(t) }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(dates, equalTo(listOf(day(0), day(2), day(3))))
    }

    @Test
    fun testEdit_withOffset() {
        val dates = mutableListOf<LocalDate>()
        view.setDataOffset(3)
        view.onEdit = { t -> dates += t }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(dates, equalTo(listOf(day(3), day(5), day(6))))
    }
}
