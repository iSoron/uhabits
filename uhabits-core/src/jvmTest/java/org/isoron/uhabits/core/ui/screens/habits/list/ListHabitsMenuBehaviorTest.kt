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

import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito

class ListHabitsMenuBehaviorTest : BaseUnitTest() {
    private var behavior: ListHabitsMenuBehavior? = null

    @Mock
    private val screen: ListHabitsMenuBehavior.Screen? = null

    @Mock
    private val adapter: ListHabitsMenuBehavior.Adapter? = null

    @Mock
    private val prefs: Preferences? = null

    @Mock
    private val themeSwitcher: ThemeSwitcher? = null

    @Captor
    private val matcherCaptor: ArgumentCaptor<HabitMatcher>? = null

    @Captor
    private val orderCaptor: ArgumentCaptor<HabitList.Order>? = null

    @Captor
    private val secondaryOrderCaptor: ArgumentCaptor<HabitList.Order>? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        behavior = ListHabitsMenuBehavior(screen!!, adapter!!, prefs!!, themeSwitcher!!)
        Mockito.clearInvocations(adapter)
    }

    @Test
    fun testInitialFilter() {
        Mockito.`when`(prefs!!.showArchived).thenReturn(true)
        Mockito.`when`(prefs.showCompleted).thenReturn(true)
        behavior = ListHabitsMenuBehavior(screen!!, adapter!!, prefs, themeSwitcher!!)
        Mockito.verify(adapter).setFilter(
            matcherCaptor!!.capture()
        )
        Mockito.verify(adapter).refresh()
        Mockito.verifyNoMoreInteractions(adapter)
        Mockito.clearInvocations(adapter)
        TestCase.assertTrue(matcherCaptor.value.isArchivedAllowed)
        TestCase.assertTrue(matcherCaptor.value.isCompletedAllowed)
        Mockito.`when`(prefs.showArchived).thenReturn(false)
        Mockito.`when`(prefs.showCompleted).thenReturn(false)
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        Mockito.verify(adapter).setFilter(
            matcherCaptor.capture()
        )
        Mockito.verify(adapter).refresh()
        Mockito.verifyNoMoreInteractions(adapter)
        TestCase.assertFalse(matcherCaptor.value.isArchivedAllowed)
        TestCase.assertFalse(matcherCaptor.value.isCompletedAllowed)
    }

    //    @Test
    //    public void testOnCreateHabit()
    //    {
    //        behavior.onCreateHabit();
    //        verify(screen).showCreateHabitScreen();
    //    }
    @Test
    fun testOnSortByColor() {
        behavior!!.onSortByColor()
        Mockito.verify(adapter)!!.primaryOrder = orderCaptor!!.capture()
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_COLOR_ASC)
        )
    }

    @Test
    fun testOnSortManually() {
        behavior!!.onSortByManually()
        Mockito.verify(adapter)!!.primaryOrder = orderCaptor!!.capture()
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_POSITION)
        )
    }

    @Test
    fun testOnSortScore() {
        behavior!!.onSortByScore()
        Mockito.verify(adapter)!!.primaryOrder = orderCaptor!!.capture()
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_SCORE_DESC)
        )
    }

    @Test
    fun testOnSortName() {
        behavior!!.onSortByName()
        Mockito.verify(adapter)!!.primaryOrder = orderCaptor!!.capture()
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_NAME_ASC)
        )
    }

    @Test
    fun testOnSortStatus() {
        Mockito.`when`(adapter!!.primaryOrder).thenReturn(HabitList.Order.BY_NAME_ASC)
        behavior!!.onSortByStatus()
        Mockito.verify(adapter).primaryOrder = orderCaptor!!.capture()
        Mockito.verify(adapter).setSecondaryOrder(
            secondaryOrderCaptor!!.capture()
        )
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_STATUS_ASC)
        )
        assertThat(
            secondaryOrderCaptor.value,
            equalTo(HabitList.Order.BY_NAME_ASC)
        )
    }

    @Test
    fun testOnSortStatusToggle() {
        Mockito.`when`(adapter!!.primaryOrder).thenReturn(HabitList.Order.BY_STATUS_ASC)
        behavior!!.onSortByStatus()
        Mockito.verify(adapter).primaryOrder = orderCaptor!!.capture()
        Mockito.verify(adapter, Mockito.never()).setSecondaryOrder(ArgumentMatchers.any())
        assertThat(
            orderCaptor.value,
            equalTo(HabitList.Order.BY_STATUS_DESC)
        )
    }

    @Test
    fun testOnToggleShowArchived() {
        behavior!!.onToggleShowArchived()
        Mockito.verify(adapter)!!.setFilter(
            matcherCaptor!!.capture()
        )
        TestCase.assertTrue(matcherCaptor.value.isArchivedAllowed)
        Mockito.clearInvocations(adapter)
        behavior!!.onToggleShowArchived()
        Mockito.verify(adapter)!!.setFilter(
            matcherCaptor.capture()
        )
        TestCase.assertFalse(matcherCaptor.value.isArchivedAllowed)
    }

    @Test
    fun testOnToggleShowCompleted() {
        behavior!!.onToggleShowCompleted()
        Mockito.verify(adapter)!!.setFilter(
            matcherCaptor!!.capture()
        )
        TestCase.assertTrue(matcherCaptor.value.isCompletedAllowed)
        Mockito.clearInvocations(adapter)
        behavior!!.onToggleShowCompleted()
        Mockito.verify(adapter)!!.setFilter(
            matcherCaptor.capture()
        )
        TestCase.assertFalse(matcherCaptor.value.isCompletedAllowed)
    }

    @Test
    fun testOnViewAbout() {
        behavior!!.onViewAbout()
        Mockito.verify(screen)!!.showAboutScreen()
    }

    @Test
    fun testOnViewFAQ() {
        behavior!!.onViewFAQ()
        Mockito.verify(screen)!!.showFAQScreen()
    }

    @Test
    fun testOnViewSettings() {
        behavior!!.onViewSettings()
        Mockito.verify(screen)!!.showSettingsScreen()
    }

    @Test
    fun testOnToggleNightMode() {
        behavior!!.onToggleNightMode()
        Mockito.verify(themeSwitcher)!!.toggleNightMode()
        Mockito.verify(screen)!!.applyTheme()
    }
}
