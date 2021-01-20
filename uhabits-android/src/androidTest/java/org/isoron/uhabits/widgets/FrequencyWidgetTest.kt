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
package org.isoron.uhabits.widgets

import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
@MediumTest
class FrequencyWidgetTest : BaseViewTest() {
    private lateinit var habit: Habit
    private lateinit var view: FrameLayout
    override fun setUp() {
        super.setUp()
        setTheme(R.style.WidgetTheme)
        prefs.widgetOpacity = 255
        habit = fixtures.createVeryLongHabit()
        val widget = FrequencyWidget(targetContext, 0, habit, Calendar.SUNDAY)
        view = convertToView(widget, 400, 400)
    }

    @Test
    fun testIsInstalled() {
        assertWidgetProviderIsInstalled(FrequencyWidgetProvider::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }

    companion object {
        private const val PATH = "widgets/FrequencyWidget/"
    }
}
