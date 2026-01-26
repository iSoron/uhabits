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
package org.isoron.uhabits.activities.habits.progress

import android.content.Context
import org.isoron.uhabits.BaseAndroidJVMTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.memory.MemoryModelFactory
import org.isoron.uhabits.core.test.HabitFixtures
import org.isoron.uhabits.core.ui.views.LightTheme
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for ProgressPresenter focusing on empty/edge case scenarios.
 *
 * Note: Tests that call buildState() with active habits require Android framework
 * (Color.parseColor) and are in the androidTest folder. Core aggregation logic
 * is thoroughly tested in AggregateScoreCalculatorTest.
 */
@RunWith(MockitoJUnitRunner::class)
class ProgressPresenterTest : BaseAndroidJVMTest() {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var habitList: HabitList
    private lateinit var presenter: ProgressPresenter
    private lateinit var testFixtures: HabitFixtures
    private val theme = LightTheme()
    private val firstWeekday = 1 // Sunday

    @Before
    override fun setUp() {
        super.setUp()
        val modelFactory = MemoryModelFactory()
        habitList = modelFactory.buildHabitList()
        testFixtures = HabitFixtures(modelFactory, habitList)
        presenter = ProgressPresenter(mockContext, habitList, theme, firstWeekday)
    }

    // ==================== Empty state tests ====================

    @Test
    fun testBuildState_withNoHabits_returnsEmptyState() {
        val state = presenter.buildState()

        assertTrue(state.isEmpty)
        assertNull(state.scoreCard)
        assertNull(state.barCard)
        assertNull(state.historyCard)
        assertNull(state.streakCard)
        assertNull(state.frequencyCard)
    }

    @Test
    fun testBuildState_withOnlyArchivedHabits_returnsEmptyState() {
        val habit = testFixtures.createShortHabit()
        habit.isArchived = true
        habitList.add(habit)

        val state = presenter.buildState()

        assertTrue(state.isEmpty)
    }

    @Test
    fun testPresenter_initialization_withValidParameters() {
        val newPresenter = ProgressPresenter(mockContext, habitList, theme, firstWeekday)
        val state = newPresenter.buildState()

        // Empty habit list should produce empty state
        assertTrue(state.isEmpty)
    }

    @Test
    fun testPresenter_initialization_withDifferentFirstWeekday() {
        // Monday = 2
        val mondayPresenter = ProgressPresenter(mockContext, habitList, theme, 2)
        val state = mondayPresenter.buildState()

        assertTrue(state.isEmpty)
    }
}
