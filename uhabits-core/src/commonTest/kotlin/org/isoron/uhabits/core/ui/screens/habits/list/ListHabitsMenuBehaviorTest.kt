/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import dev.mokkery.verify.VerifyMode.Companion.not as notCalled

class ListHabitsMenuBehaviorTest : BaseUnitTest() {
    private lateinit var behavior: ListHabitsMenuBehavior

    private val screen: ListHabitsMenuBehavior.Screen = mock()

    private val adapter: ListHabitsMenuBehavior.Adapter = mock()

    private val prefs: Preferences = mock()

    private val themeSwitcher: ThemeSwitcher = mock()

    private var capturedMatcher: HabitMatcher? = null
    private var capturedOrder: HabitList.Order? = null
    private var capturedSecondaryOrder: HabitList.Order? = null

    @BeforeTest
    override fun setUp() {
        super.setUp()
        every { adapter.setFilter(any()) } returns Unit
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        resetCalls(adapter)
        resetCalls(screen)
    }

    @Test
    fun testInitialFilter() {
        every { prefs.showArchived } returns true
        every { prefs.showCompleted } returns true
        every { adapter.setFilter(any()) } returns Unit
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
        verifyNoMoreCalls(adapter)
        resetCalls(adapter)
        resetCalls(screen)
        every { prefs.showArchived } returns false
        every { prefs.showCompleted } returns false
        every { adapter.setFilter(any()) } returns Unit
        behavior = ListHabitsMenuBehavior(screen, adapter, prefs, themeSwitcher)
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
        verifyNoMoreCalls(adapter)
    }

    @Test
    fun testOnSortByColor() {
        every { adapter.primaryOrder } returns HabitList.Order.BY_POSITION
        behavior.onSortByColor()
        verify { adapter.primaryOrder = HabitList.Order.BY_COLOR_ASC }
        verify { screen.refresh() }
    }

    @Test
    fun testOnSortManually() {
        behavior.onSortByManually()
        verify { adapter.primaryOrder = HabitList.Order.BY_POSITION }
        verify { screen.refresh() }
    }

    @Test
    fun testOnSortScore() {
        every { adapter.primaryOrder } returns HabitList.Order.BY_POSITION
        behavior.onSortByScore()
        verify { adapter.primaryOrder = HabitList.Order.BY_SCORE_DESC }
        verify { screen.refresh() }
    }

    @Test
    fun testOnSortName() {
        every { adapter.primaryOrder } returns HabitList.Order.BY_POSITION
        behavior.onSortByName()
        verify { adapter.primaryOrder = HabitList.Order.BY_NAME_ASC }
        verify { screen.refresh() }
    }

    @Test
    fun testOnSortStatus() {
        every { adapter.primaryOrder } returns HabitList.Order.BY_NAME_ASC
        behavior.onSortByStatus()
        verify { adapter.primaryOrder = HabitList.Order.BY_STATUS_ASC }
        verify { adapter.secondaryOrder = HabitList.Order.BY_NAME_ASC }
        verify { screen.refresh() }
    }

    @Test
    fun testOnSortStatusToggle() {
        every { adapter.primaryOrder } returns HabitList.Order.BY_STATUS_ASC
        behavior.onSortByStatus()
        verify { adapter.primaryOrder = HabitList.Order.BY_STATUS_DESC }
        verify(notCalled) { adapter.secondaryOrder = any() }
        verify { screen.refresh() }
    }

    @Test
    fun testOnToggleShowArchived() {
        every { adapter.setFilter(any()) } returns Unit
        behavior.onToggleShowArchived()
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
        resetCalls(adapter)
        resetCalls(screen)
        every { adapter.setFilter(any()) } returns Unit
        behavior.onToggleShowArchived()
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
    }

    @Test
    fun testOnToggleShowCompleted() {
        every { adapter.setFilter(any()) } returns Unit
        behavior.onToggleShowCompleted()
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
        resetCalls(adapter)
        resetCalls(screen)
        every { adapter.setFilter(any()) } returns Unit
        behavior.onToggleShowCompleted()
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
    }

    @Test
    fun testOnViewAbout() {
        behavior.onViewAbout()
        verify { screen.showAboutScreen() }
    }

    @Test
    fun testOnViewFAQ() {
        behavior.onViewFAQ()
        verify { screen.showFAQScreen() }
    }

    @Test
    fun testOnViewSettings() {
        behavior.onViewSettings()
        verify { screen.showSettingsScreen() }
    }

    @Test
    fun testOnToggleNightMode() {
        behavior.onToggleNightMode()
        verify { themeSwitcher.toggleNightMode() }
        verify { screen.applyTheme() }
    }

    @Test
    fun testOnSearchQueryChanged() {
        every { adapter.setFilter(any()) } calls { args ->
            capturedMatcher = args.arg<HabitMatcher>(0)
            Unit
        }
        behavior.onSearchQueryChanged("yoga")
        verify { adapter.setFilter(any()) }
        verify { screen.refresh() }
        assertEquals("yoga", capturedMatcher?.searchQuery)
    }
}
