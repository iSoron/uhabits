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
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.utils.PaletteUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class EntryButtonViewTest : BaseViewTest() {

    private val PATH = "habits/list/CheckmarkButtonView"
    lateinit var view: CheckmarkButtonView

    var toggled = false
    var edited = false

    @Before
    override fun setUp() {
        super.setUp()
        view = component.getEntryButtonViewFactory().create().apply {
            value = Entry.NO
            color = PaletteUtils.getAndroidTestColor(5)
            onToggle = { _, _, _ -> toggled = true }
            onEdit = { _ -> edited = true }
        }
        measureView(view, dpToPixels(48), dpToPixels(48))
    }

    @Test
    fun testRender_explicitCheck() {
        view.value = Entry.YES_MANUAL
        assertRendersCheckedExplicitly()
    }

    @Test
    fun testRender_implicitCheck() {
        view.value = Entry.YES_AUTO
        assertRendersCheckedImplicitly()
    }

    @Test
    fun testRender_unchecked() {
        view.value = Entry.NO
        assertRendersUnchecked()
    }

    @Test
    fun testClick_withShortToggleDisabled() {
        prefs.isShortToggleEnabled = false
        view.performClick()
        assertTrue(!toggled and edited)
    }

    @Test
    fun testClick_withShortToggleEnabled() {
        prefs.isShortToggleEnabled = true
        view.performClick()
        assertTrue(toggled and !edited)
    }

    @Test
    fun testLongClick_withShortToggleDisabled() {
        prefs.isShortToggleEnabled = false
        view.performLongClick()
        assertTrue(toggled and !edited)
    }

    @Test
    fun testLongClick_withShortToggleEnabled() {
        prefs.isShortToggleEnabled = true
        view.performLongClick()
        assertTrue(!toggled and edited)
    }

    private fun assertRendersCheckedExplicitly() {
        assertRenders(view, "$PATH/render_explicit_check.png")
    }

    private fun assertRendersCheckedImplicitly() {
        assertRenders(view, "$PATH/render_implicit_check.png")
    }

    private fun assertRendersUnchecked() {
        assertRenders(view, "$PATH/render_unchecked.png")
    }
}
