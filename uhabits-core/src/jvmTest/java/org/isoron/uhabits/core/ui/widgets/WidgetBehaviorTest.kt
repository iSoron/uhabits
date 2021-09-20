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
package org.isoron.uhabits.core.ui.widgets

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.nextToggleValue
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.NotificationTray
import org.isoron.uhabits.core.utils.DateUtils.Companion.getTodayWithOffset
import org.junit.Before
import org.junit.Test

class WidgetBehaviorTest : BaseUnitTest() {
    private lateinit var notificationTray: NotificationTray
    private lateinit var preferences: Preferences
    private lateinit var behavior: WidgetBehavior
    private lateinit var habit: Habit
    private lateinit var today: Timestamp

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyHabit()
        commandRunner = mock()
        notificationTray = mock()
        preferences = mock()
        behavior = WidgetBehavior(habitList, commandRunner, notificationTray, preferences)
        today = getTodayWithOffset()
    }

    @Test
    fun testOnAddRepetition() {
        behavior.onAddRepetition(habit, today)
        verify(commandRunner).run(
            CreateRepetitionCommand(habitList, habit, today, Entry.YES_MANUAL, "")
        )
        verify(notificationTray).cancel(habit)
        verifyZeroInteractions(preferences)
    }

    @Test
    fun testOnRemoveRepetition() {
        behavior.onRemoveRepetition(habit, today)
        verify(commandRunner).run(
            CreateRepetitionCommand(habitList, habit, today, Entry.NO, "")
        )
        verify(notificationTray).cancel(habit)
        verifyZeroInteractions(preferences)
    }

    @Test
    fun testOnToggleRepetition() {
        for (skipEnabled in listOf(true, false)) for (
            currentValue in listOf(
                Entry.NO,
                Entry.YES_MANUAL,
                Entry.YES_AUTO,
                Entry.SKIP,
            )
        ) {
            whenever(preferences.isSkipEnabled).thenReturn(skipEnabled)
            val nextValue: Int = nextToggleValue(
                currentValue,
                isSkipEnabled = skipEnabled,
                areQuestionMarksEnabled = false
            )
            habit.originalEntries.add(Entry(today, currentValue))
            behavior.onToggleRepetition(habit, today)
            verify(preferences).isSkipEnabled
            verify(commandRunner).run(
                CreateRepetitionCommand(habitList, habit, today, nextValue, "")
            )
            verify(notificationTray).cancel(
                habit
            )
            reset(preferences, commandRunner, notificationTray)
        }
    }

    @Test
    fun testOnIncrement() {
        habit = fixtures.createNumericalHabit()
        habit.originalEntries.add(Entry(today, 500))
        habit.recompute()
        behavior.onIncrement(habit, today, 100)
        verify(commandRunner).run(
            CreateRepetitionCommand(habitList, habit, today, 600, "")
        )
        verify(notificationTray).cancel(habit)
        verifyZeroInteractions(preferences)
    }

    @Test
    fun testOnDecrement() {
        habit = fixtures.createNumericalHabit()
        habit.originalEntries.add(Entry(today, 500))
        habit.recompute()
        behavior.onDecrement(habit, today, 100)
        verify(commandRunner).run(
            CreateRepetitionCommand(habitList, habit, today, 400, "")
        )
        verify(notificationTray).cancel(habit)
        verifyZeroInteractions(preferences)
    }
}
