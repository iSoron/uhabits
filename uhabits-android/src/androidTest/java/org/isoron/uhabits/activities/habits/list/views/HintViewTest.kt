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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.core.ui.screens.habits.list.HintList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HintViewTest : BaseViewTest() {
    private lateinit var view: HintView
    private lateinit var list: HintList

    @Before
    override fun setUp() {
        super.setUp()
        list = mock()
        view = HintView(targetContext, list)
        measureView(view, 400f, 200f)
        val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        doReturn(true).whenever(list).shouldShow()
        doReturn(text).whenever(list).pop()
        view.showNext()
        skipAnimation(view)
    }

    @Test
    @Throws(Exception::class)
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }

    @Test
    @Throws(Exception::class)
    fun testClick() {
        assertThat(view.alpha, equalTo(1f))
        view.performClick()
        skipAnimation(view)
        assertThat(view.alpha, equalTo(0f))
    }

    companion object {
        const val PATH = "habits/list/HintView/"
    }
}
