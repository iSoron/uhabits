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
package org.isoron.uhabits.core.ui

import dev.mokkery.mock
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.ArchiveHabitsCommand
import org.isoron.uhabits.core.commands.EditHabitCommand
import org.isoron.uhabits.core.commands.UnarchiveHabitsCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.Preferences
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationTrayTest : BaseUnitTest() {
    private lateinit var systemTray: FakeSystemTray
    private lateinit var tray: NotificationTray
    private lateinit var habit: Habit
    private lateinit var otherHabit: Habit
    private lateinit var today: LocalDate

    @BeforeTest
    override fun setUp() {
        super.setUp()
        today = getToday()
        systemTray = FakeSystemTray()
        val preferences: Preferences = mock()
        tray = NotificationTray(taskRunner, commandRunner, preferences, systemTray)

        habit = fixtures.createEmptyHabit(name = "Meditate")
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        habitList.add(habit)

        otherHabit = fixtures.createEmptyHabit(name = "Run")
        otherHabit.reminder = Reminder(9, 0, WeekdayList.EVERY_DAY)
        habitList.add(otherHabit)

        tray.show(habit, today, 0)
        tray.show(otherHabit, today, 0)
        assertEquals(listOf(habit.id, otherHabit.id), systemTray.shown)
    }

    @Test
    fun testArchivingHabitRemovesItsNotification() {
        val command = ArchiveHabitsCommand(habitList, listOf(habit))
        command.run()
        tray.onCommandFinished(command)
        assertEquals(listOf(habit.id), systemTray.removed)
    }

    @Test
    fun testRemovingReminderRemovesNotification() {
        val modified = fixtures.createEmptyHabit()
        modified.copyFrom(habit)
        modified.reminder = null
        val command = EditHabitCommand(habitList, habit.id!!, modified)
        command.run()
        tray.onCommandFinished(command)
        assertEquals(listOf(habit.id), systemTray.removed)
    }

    @Test
    fun testEditingNameKeepsNotification() {
        val modified = fixtures.createEmptyHabit()
        modified.copyFrom(habit)
        modified.name = "Meditate for 10 minutes"
        val command = EditHabitCommand(habitList, habit.id!!, modified)
        command.run()
        tray.onCommandFinished(command)
        assertTrue(systemTray.removed.isEmpty())
    }

    @Test
    fun testCancelledNotificationIsNotReshownAfterUnarchiving() {
        val archive = ArchiveHabitsCommand(habitList, listOf(habit))
        archive.run()
        tray.onCommandFinished(archive)
        val unarchive = UnarchiveHabitsCommand(habitList, listOf(habit))
        unarchive.run()
        tray.onCommandFinished(unarchive)
        systemTray.shown.clear()

        tray.onNotificationsChanged()

        assertEquals(listOf(otherHabit.id), systemTray.shown)
    }

    private class FakeSystemTray : NotificationTray.SystemTray {
        val shown = mutableListOf<Long?>()
        val removed = mutableListOf<Long?>()

        override fun removeNotification(notificationId: Int) {
            removed.add(notificationId.toLong())
        }

        override fun showNotification(
            habit: Habit,
            notificationId: Int,
            date: LocalDate,
            reminderTime: Long
        ) {
            shown.add(habit.id)
        }

        override fun log(msg: String) {}
    }
}
