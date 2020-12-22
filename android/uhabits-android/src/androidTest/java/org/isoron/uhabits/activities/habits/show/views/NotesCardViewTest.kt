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
package org.isoron.uhabits.activities.habits.show.views

import android.view.*
import android.view.View.*
import androidx.test.ext.junit.runners.*
import androidx.test.filters.*
import org.hamcrest.Matchers.*
import org.isoron.uhabits.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class NotesCardViewTest : BaseViewTest() {
    val PATH = "habits/show/NotesCard"
    private lateinit var view: NotesCardView

    @Before
    override fun setUp() {
        super.setUp()
        view = LayoutInflater
                .from(targetContext)
                .inflate(R.layout.show_habit, null)
                .findViewById(R.id.notesCard)
        view.update(NotesCardViewModel(description = "This is a test description"))
        measureView(view, 800f, 200f)
    }

    @Test
    fun testRender() {
        assertRenders(view, "$PATH/render.png")
    }

    @Test
    fun testRenderEmptyDescription() {
        view.update(NotesCardViewModel(description = ""))
        assertThat(view.visibility, equalTo(GONE))
    }
}