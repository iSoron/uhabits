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
package org.isoron.uhabits.core.ui.widgets

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.nextToggleValue
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.NotificationTray
import kotlin.test.BeforeTest
import kotlin.test.Test
import dev.mokkery.verify.VerifyMode.Companion.not as notCalled

class WidgetBehaviorTest : BaseUnitTest() {
    private lateinit var notificationTray: NotificationTray
    private lateinit var preferences: Preferences
    private lateinit var behavior: WidgetBehavior
    private lateinit var habit: Habit
    private lateinit var today: LocalDate

    @BeforeTest
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyHabit()
        commandRunner = mock()
        notificationTray = mock()
        preferences = mock()
        behavior = WidgetBehavior(habitList, commandRunner, notificationTray, preferences)
        today = getToday()
    }

    @Test
    fun testOnAddRepetition() {
        behavior.onAddRepetition(habit, today)
        verify {
            commandRunner.run(
                CreateRepetitionCommand(habitList, habit, today, Entry.YES_MANUAL, "")
            )
        }
        verify { notificationTray.cancel(habit) }
        verify(notCalled) { preferences.isSkipEnabled }
    }

    @Test
    fun testOnRemoveRepetition() {
        behavior.onRemoveRepetition(habit, today)
        verify {
            commandRunner.run(
                CreateRepetitionCommand(habitList, habit, today, Entry.NO, "")
            )
        }
        verify { notificationTray.cancel(habit) }
        verify(notCalled) { preferences.isSkipEnabled }
    }

    @Test
    fun testOnToggleRepetition() {
        for (skipEnabled in listOf(true, false)) for (
            currentValue in listOf(
                Entry.NO,
                Entry.YES_MANUAL,
                Entry.YES_AUTO,
                Entry.SKIP
            )
            ) {
                every { preferences.isSkipEnabled } returns skipEnabled
                val nextValue: Int = nextToggleValue(
                    currentValue,
                    isSkipEnabled = skipEnabled,
                    areQuestionMarksEnabled = false
                )
                habit.originalEntries.add(Entry(today, currentValue))
                behavior.onToggleRepetition(habit, today)
                verify { preferences.isSkipEnabled }
                verify {
                    commandRunner.run(
                        CreateRepetitionCommand(habitList, habit, today, nextValue, "")
                    )
                }
                verify {
                    notificationTray.cancel(
                        habit
                    )
                }
                resetCalls(preferences, commandRunner, notificationTray)
            }
    }

    @Test
    fun testOnIncrement() {
        habit = fixtures.createNumericalHabit()
        habit.originalEntries.add(Entry(today, 500))
        habit.recompute()
        behavior.onIncrement(habit, today, 100)
        verify {
            commandRunner.run(
                CreateRepetitionCommand(habitList, habit, today, 600, "")
            )
        }
        verify { notificationTray.cancel(habit) }
        verify(notCalled) { preferences.isSkipEnabled }
    }

    @Test
    fun testOnDecrement() {
        habit = fixtures.createNumericalHabit()
        habit.originalEntries.add(Entry(today, 500))
        habit.recompute()
        behavior.onDecrement(habit, today, 100)
        verify {
            commandRunner.run(
                CreateRepetitionCommand(habitList, habit, today, 400, "")
            )
        }
        verify { notificationTray.cancel(habit) }
        verify(notCalled) { preferences.isSkipEnabled }
    }
}
