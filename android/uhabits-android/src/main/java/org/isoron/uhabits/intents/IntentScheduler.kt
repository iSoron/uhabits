/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.intents

import android.app.*
import android.app.AlarmManager.*
import android.content.*
import android.content.Context.*
import android.os.Build.VERSION.*
import android.os.Build.VERSION_CODES.*
import android.util.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.reminders.ReminderScheduler.*
import org.isoron.uhabits.core.utils.*
import org.isoron.uhabits.inject.*
import java.util.*
import javax.inject.*

@AppScope
class IntentScheduler
@Inject constructor(
        @AppContext context: Context,
        private val pendingIntents: PendingIntentFactory
) : SystemScheduler {

    private val manager =
            context.getSystemService(ALARM_SERVICE) as AlarmManager

    private fun schedule(timestamp: Long, intent: PendingIntent, alarmType: Int): SchedulerResult {
        val now = System.currentTimeMillis()
        Log.d("IntentScheduler", "timestamp=$timestamp now=$now")
        if (timestamp < now) {
            Log.e("IntentScheduler",
                  "Ignoring attempt to schedule intent in the past.")
            return SchedulerResult.IGNORED
        }
        if (SDK_INT >= M)
            manager.setExactAndAllowWhileIdle(alarmType, timestamp, intent)
        else
            manager.setExact(alarmType, timestamp, intent)
        return SchedulerResult.OK
    }

    override fun scheduleShowReminder(reminderTime: Long,
                                      habit: Habit,
                                      timestamp: Long): SchedulerResult {
        val intent = pendingIntents.showReminder(habit, reminderTime, timestamp)
        logReminderScheduled(habit, reminderTime)
        return schedule(reminderTime, intent, RTC_WAKEUP)
    }

    override fun scheduleWidgetUpdate(updateTime: Long): SchedulerResult {
        val intent = pendingIntents.updateWidgets()
        return schedule(updateTime, intent, RTC)
    }

    override fun log(componentName: String, msg: String) {
        Log.d(componentName, msg)
    }

    private fun logReminderScheduled(habit: Habit, reminderTime: Long) {
        val min = Math.min(5, habit.name.length)
        val name = habit.name.substring(0, min)
        val df = DateFormats.getBackupDateFormat()
        val time = df.format(Date(reminderTime))
        Log.i("ReminderHelper",
              String.format("Setting alarm (%s): %s", time, name))
    }
}
