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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.Timestamp
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

    @Before
    override fun setUp() {
        super.setUp()
        val checkmarks = doubleArrayOf(
            1400.0,
            5300.0,
            0.0,
            14600.0,
            2500.0,
            45000.0
        )

        view = component.getNumberPanelViewFactory().create().apply {
            values = checkmarks
            buttonCount = 4
            color = PaletteUtils.getAndroidTestColor(7)
            units = "steps"
            targetType = NumericalHabitType.AT_LEAST
            threshold = 5000.0
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
    fun testEdit() {
        val timestamps = mutableListOf<Timestamp>()
        view.onEdit = { _, t -> timestamps.plusAssign(t) }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(timestamps, equalTo(listOf(day(0), day(2), day(3))))
    }

    @Test
    fun testEdit_withOffset() {
        val timestamps = mutableListOf<Timestamp>()
        view.dataOffset = 3
        view.onEdit = { _, t -> timestamps += t }
        view.buttons[0].performLongClick()
        view.buttons[2].performLongClick()
        view.buttons[3].performLongClick()
        assertThat(timestamps, equalTo(listOf(day(3), day(5), day(6))))
    }
}
