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

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.EntryList
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class CheckmarkWidgetTest : BaseViewTest() {
    private lateinit var habit: Habit
    private lateinit var entries: EntryList
    private lateinit var view: FrameLayout
    private val today = getTodayWithOffset()
    override fun setUp() {
        super.setUp()
        setTheme(R.style.WidgetTheme)
        prefs.widgetOpacity = 255
        prefs.isSkipEnabled = true
        habit = fixtures.createVeryLongHabit()
        entries = habit.computedEntries
        val widget = CheckmarkWidget(targetContext, 0, habit)
        view = convertToView(widget, 150, 200)
        assertThat(entries.get(today).value, equalTo(Entry.YES_MANUAL))
    }

    @Test
    @Throws(Exception::class)
    fun testClick() {
        val button = view.findViewById<View>(R.id.button) as Button
        assertThat(
            button,
            `is`(CoreMatchers.not(CoreMatchers.nullValue()))
        )

        // A better test would be to capture the intent, but it doesn't seem
        // possible to capture intents sent to BroadcastReceivers.
        button.performClick()
        sleep(1000)
        assertThat(entries.get(today).value, equalTo(Entry.SKIP))
        button.performClick()
        sleep(1000)
        assertThat(entries.get(today).value, equalTo(Entry.NO))
    }

    @Test
    fun testIsInstalled() {
        assertWidgetProviderIsInstalled(CheckmarkWidgetProvider::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }

    companion object {
        private const val PATH = "widgets/CheckmarkWidget/"
    }
}
