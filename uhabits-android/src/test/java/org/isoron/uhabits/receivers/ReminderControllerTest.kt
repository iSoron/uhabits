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
package org.isoron.uhabits.receivers

import org.isoron.uhabits.BaseAndroidJVMTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.reminders.ReminderScheduler
import org.isoron.uhabits.core.ui.NotificationTray
import org.junit.Test
import org.mockito.Mockito

class ReminderControllerTest : BaseAndroidJVMTest() {
    private lateinit var controller: ReminderController
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var notificationTray: NotificationTray
    private lateinit var preferences: Preferences
    override fun setUp() {
        super.setUp()
        reminderScheduler = Mockito.mock(ReminderScheduler::class.java)
        notificationTray = Mockito.mock(NotificationTray::class.java)
        preferences = Mockito.mock(Preferences::class.java)
        controller = ReminderController(
            reminderScheduler,
            notificationTray,
            preferences
        )
    }

    @Test
    @Throws(Exception::class)
    fun testOnDismiss() {
        Mockito.verifyNoMoreInteractions(reminderScheduler)
        Mockito.verifyNoMoreInteractions(notificationTray)
        Mockito.verifyNoMoreInteractions(preferences)
    }

    @Test
    @Throws(Exception::class)
    fun testOnShowReminder() {
        val habit = Mockito.mock(Habit::class.java)
        controller.onShowReminder(habit, Timestamp.ZERO.plus(100), 456)
        Mockito.verify(notificationTray).show(habit, Timestamp.ZERO.plus(100), 456)
        Mockito.verify(reminderScheduler).scheduleAll()
    }

    @Test
    @Throws(Exception::class)
    fun testOnBootCompleted() {
        controller.onBootCompleted()
        Mockito.verify(reminderScheduler).scheduleAll()
    }
}
