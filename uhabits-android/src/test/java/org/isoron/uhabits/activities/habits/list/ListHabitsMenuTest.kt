/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.habits.list

import android.view.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*

class ListHabitsMenuTest : BaseAndroidJVMTest() {
    @Mock lateinit var activity: BaseActivity
    @Mock lateinit var preferences: Preferences
    @Mock lateinit var themeSwitcher: ThemeSwitcher
    @Mock lateinit var behavior: ListHabitsMenuBehavior
    lateinit var menu: ListHabitsMenu

    @Before
    override fun setUp() {
        super.setUp()
        menu = ListHabitsMenu(activity, preferences,
                              themeSwitcher, behavior)
    }

    @Test
    fun testOnCreate() {
        val nightModeItem = mock(MenuItem::class.java)
        val hideArchivedItem = mock(MenuItem::class.java)
        val hideCompletedItem = mock(MenuItem::class.java)
        val androidMenu = mock(Menu::class.java)
        `when`(androidMenu.findItem(R.id.actionToggleNightMode)).thenReturn(
                nightModeItem)
        `when`(androidMenu.findItem(R.id.actionHideArchived)).thenReturn(
                hideArchivedItem)
        `when`(androidMenu.findItem(R.id.actionHideCompleted)).thenReturn(
                hideCompletedItem)

        `when`(preferences.showArchived).thenReturn(false)
        `when`(preferences.showCompleted).thenReturn(false)
        `when`(themeSwitcher.isNightMode).thenReturn(false)

        menu.onCreate(androidMenu)

        verify(nightModeItem).isChecked = false
        verify(hideArchivedItem).isChecked = true
        verify(hideCompletedItem).isChecked = true
        reset(nightModeItem, hideArchivedItem, hideCompletedItem)

        `when`(preferences.showArchived).thenReturn(true)
        `when`(preferences.showCompleted).thenReturn(true)
        `when`(themeSwitcher.isNightMode).thenReturn(true)

        menu.onCreate(androidMenu)

        verify(nightModeItem).isChecked = true
        verify(hideArchivedItem).isChecked = false
        verify(hideCompletedItem).isChecked = false
    }

    @Test
    fun testOnSelected_about() {
        onItemSelected(R.id.actionAbout)
        verify(behavior).onViewAbout()
    }

    @Test
    fun testOnSelected_add() {
        onItemSelected(R.id.actionAdd)
        verify(behavior).onCreateHabit()
    }

    @Test
    fun testOnSelected_faq() {
        onItemSelected(R.id.actionFAQ)
        verify(behavior).onViewFAQ()
    }

    @Test
    fun testOnSelected_nightMode() {
        onItemSelected(R.id.actionToggleNightMode)
        verify(behavior).onToggleNightMode()
    }

    @Test
    fun testOnSelected_settings() {
        onItemSelected(R.id.actionSettings)
        verify(behavior).onViewSettings()
    }

    @Test
    fun testOnSelected_showArchived() {
        onItemSelected(R.id.actionHideArchived)
        verify(behavior).onToggleShowArchived()
    }

    @Test
    fun testOnSelected_showCompleted() {
        onItemSelected(R.id.actionHideCompleted)
        verify(behavior).onToggleShowCompleted()
    }

    private fun onItemSelected(actionId: Int) {
        val item = mock(MenuItem::class.java)
        `when`(item.itemId).thenReturn(actionId)
        menu.onItemSelected(item)
    }
}