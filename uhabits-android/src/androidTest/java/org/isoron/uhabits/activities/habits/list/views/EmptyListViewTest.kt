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
import org.isoron.uhabits.BaseViewTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class EmptyListViewTest : BaseViewTest() {

    private val path = "habits/list/EmptyListView"
    private val view: EmptyListView = EmptyListView(targetContext)

    @Before
    override fun setUp() {
        super.setUp()
        measureView(view, dpToPixels(200), dpToPixels(200))
    }

    @Test
    fun testRender_done() {
        view.showDone()
        assertRenders(view, "$path/done.png")
    }

    @Test
    fun testRender_empty() {
        view.showEmpty()
        assertRenders(view, "$path/empty.png")
    }
}
