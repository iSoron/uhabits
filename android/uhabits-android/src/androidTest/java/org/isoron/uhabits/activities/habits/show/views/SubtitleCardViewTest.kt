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
import androidx.test.ext.junit.runners.*
import androidx.test.filters.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.junit.*
import org.junit.runner.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class SubtitleCardViewTest : BaseViewTest() {
    val PATH = "habits/show/SubtitleCard/"
    private lateinit var view: SubtitleCardView

    @Before
    override fun setUp() {
        super.setUp()
        view = LayoutInflater
                .from(targetContext)
                .inflate(R.layout.show_habit, null)
                .findViewById(R.id.subtitleCard)
        view.update(SubtitleCardViewModel(
                color = PaletteColor(7),
                frequencyText = "3 times in 7 days",
                isNumerical = false,
                question = "Did you meditate this morning?",
                reminderText = "8:30 AM",
                targetText = "",
        ))
        measureView(view, 800f, 200f)
    }

    @Test
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }
}