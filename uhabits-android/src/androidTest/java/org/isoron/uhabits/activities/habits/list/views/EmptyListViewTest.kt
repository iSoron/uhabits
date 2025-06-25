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
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class EmptyListViewTest : BaseViewTest() {
    init {
        // TODO: fix rendering differences across APIs
        similarityCutoff = 0.00035
    }

    private val path = "habits/list/EmptyListView"

    @Test
    @Ignore("non-deterministic failure")
    fun testRender_done() {
        val view = EmptyListView(targetContext)
        view.showDone()
        measureView(view, dpToPixels(200), dpToPixels(200))
        assertRenders(view, "$path/done.png")
    }

    @Test
    @Ignore("non-deterministic failure")
    fun testRender_empty() {
        val view = EmptyListView(targetContext)
        view.showEmpty()
        measureView(view, dpToPixels(200), dpToPixels(200))
        assertRenders(view, "$path/empty.png")
    }
}
