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
package org.isoron.uhabits.core.ui.screens.habits.list

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.junit.Test

class ListHabitsMenuBehaviorTest : BaseUnitTest() {
    private lateinit var behavior: ListHabitsMenuBehavior

    private val screen: ListHabitsMenuBehavior.Screen = mock()

    private val adapter: ListHabitsMenuBehavior.Adapter = mock()

    private val prefs: Preferences = mock()

    private val themeSwitcher: ThemeSwitcher = mock()

    private val matcherCaptor: KArgumentCaptor<HabitMatcher> = argumentCaptor()

    private val orderCaptor: KArgumentCaptor<HabitList.Order> = argumentCaptor()

    private val secondaryOrderCaptor: KArgumentCaptor<HabitList.Order> = argumentCaptor()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        clearInvocations(adapter)
    }

    @Test
    fun testInitialFilter() {
        whenever(prefs.showArchived).thenReturn(true)
        whenever(prefs.showCompleted).thenReturn(true)
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        verify(adapter).setFilter(matcherCaptor.capture())
        verify(adapter).refresh()
        verifyNoMoreInteractions(adapter)
        clearInvocations(adapter)
        assertTrue(matcherCaptor.lastValue.isArchivedAllowed)
        assertTrue(matcherCaptor.lastValue.isCompletedAllowed)
        whenever(prefs.showArchived).thenReturn(false)
        whenever(prefs.showCompleted).thenReturn(false)
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        verify(adapter).setFilter(matcherCaptor.capture())
        verify(adapter).refresh()
        verifyNoMoreInteractions(adapter)
        assertFalse(matcherCaptor.lastValue.isArchivedAllowed)
        assertFalse(matcherCaptor.lastValue.isCompletedAllowed)
    }

    @Test
    fun testOnSortByColor() {
        behavior.onSortByColor()
        verify(adapter).primaryOrder = orderCaptor.capture()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_COLOR_ASC))
    }

    @Test
    fun testOnSortManually() {
        behavior.onSortByManually()
        verify(adapter).primaryOrder = orderCaptor.capture()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_POSITION))
    }

    @Test
    fun testOnSortScore() {
        behavior.onSortByScore()
        verify(adapter).primaryOrder = orderCaptor.capture()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_SCORE_DESC))
    }

    @Test
    fun testOnSortName() {
        behavior.onSortByName()
        verify(adapter).primaryOrder = orderCaptor.capture()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_NAME_ASC))
    }

    @Test
    fun testOnSortStatus() {
        whenever(adapter.primaryOrder).thenReturn(HabitList.Order.BY_NAME_ASC)
        behavior.onSortByStatus()
        verify(adapter).primaryOrder = orderCaptor.capture()
        verify(adapter).secondaryOrder = secondaryOrderCaptor.capture()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_STATUS_ASC))
        assertThat(secondaryOrderCaptor.lastValue, equalTo(HabitList.Order.BY_NAME_ASC))
    }

    @Test
    fun testOnSortStatusToggle() {
        whenever(adapter.primaryOrder).thenReturn(HabitList.Order.BY_STATUS_ASC)
        behavior.onSortByStatus()
        verify(adapter).primaryOrder = orderCaptor.capture()
        verify(adapter, never()).secondaryOrder = any()
        assertThat(orderCaptor.lastValue, equalTo(HabitList.Order.BY_STATUS_DESC))
    }

    @Test
    fun testOnToggleShowArchived() {
        behavior.onToggleShowArchived()
        verify(adapter).setFilter(matcherCaptor.capture())
        assertTrue(matcherCaptor.lastValue.isArchivedAllowed)
        clearInvocations(adapter)
        behavior.onToggleShowArchived()
        verify(adapter).setFilter(matcherCaptor.capture())
        assertFalse(matcherCaptor.lastValue.isArchivedAllowed)
    }

    @Test
    fun testOnToggleShowCompleted() {
        behavior.onToggleShowCompleted()
        verify(adapter).setFilter(matcherCaptor.capture())
        assertTrue(matcherCaptor.lastValue.isCompletedAllowed)
        clearInvocations(adapter)
        behavior.onToggleShowCompleted()
        verify(adapter).setFilter(matcherCaptor.capture())
        assertFalse(matcherCaptor.lastValue.isCompletedAllowed)
    }

    @Test
    fun testOnViewAbout() {
        behavior.onViewAbout()
        verify(screen).showAboutScreen()
    }

    @Test
    fun testOnViewFAQ() {
        behavior.onViewFAQ()
        verify(screen).showFAQScreen()
    }

    @Test
    fun testOnViewSettings() {
        behavior.onViewSettings()
        verify(screen).showSettingsScreen()
    }

    @Test
    fun testOnToggleNightMode() {
        behavior.onToggleNightMode()
        verify(themeSwitcher).toggleNightMode()
        verify(screen).applyTheme()
    }
}
