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

package org.isoron.uhabits.intents

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.activities.habits.list.ListHabitsActivity
import org.isoron.uhabits.activities.habits.show.ShowHabitActivity
import org.isoron.uhabits.activities.habits.show.ShowHabitGroupActivity
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.inject.AppContext
import org.isoron.uhabits.receivers.ReminderReceiver
import org.isoron.uhabits.receivers.WidgetReceiver

@Inject
@AppScope
class PendingIntentFactory(
    @AppContext private val context: Context,
    private val intentFactory: IntentFactory
) {

    fun addCheckmark(habit: Habit, date: LocalDate?): PendingIntent =
        getBroadcast(
            context,
            1,
            Intent(context, WidgetReceiver::class.java).apply {
                data = Uri.parse(habit.uriString)
                action = WidgetReceiver.ACTION_ADD_REPETITION
                if (date != null) putExtra("timestamp", date.unixTime)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun dismissNotification(habit: Habit): PendingIntent =
        getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                action = WidgetReceiver.ACTION_DISMISS_REMINDER
                data = Uri.parse(habit.uriString)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun dismissNotification(habitGroup: HabitGroup): PendingIntent =
        getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                action = WidgetReceiver.ACTION_DISMISS_REMINDER
                data = Uri.parse(habitGroup.uriString)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun removeRepetition(habit: Habit, date: LocalDate?): PendingIntent =
        getBroadcast(
            context,
            3,
            Intent(context, WidgetReceiver::class.java).apply {
                action = WidgetReceiver.ACTION_REMOVE_REPETITION
                data = Uri.parse(habit.uriString)
                if (date != null) putExtra("timestamp", date.unixTime)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun showHabit(habit: Habit): PendingIntent =
        androidx.core.app.TaskStackBuilder
            .create(context)
            .addNextIntentWithParentStack(
                intentFactory.startShowHabitActivity(
                    context,
                    habit
                )
            )
            .getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)!!

    fun showHabitGroup(habitGroup: HabitGroup): PendingIntent =
        androidx.core.app.TaskStackBuilder
            .create(context)
            .addNextIntentWithParentStack(
                intentFactory.startShowHabitGroupActivity(
                    context,
                    habitGroup
                )
            )
            .getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)!!

    fun showHabitTemplate(): PendingIntent {
        return getActivity(
            context,
            0,
            Intent(context, ShowHabitActivity::class.java),
            getIntentTemplateFlags()
        )
    }

    fun showHabitGroupTemplate(): PendingIntent {
        return getActivity(
            context,
            0,
            Intent(context, ShowHabitGroupActivity::class.java),
            getIntentTemplateFlags()
        )
    }

    fun showHabitFillIn(habit: Habit) =
        Intent().apply {
            data = Uri.parse(habit.uriString)
        }

    fun showReminder(
        habit: Habit,
        reminderTime: Long?,
        timestamp: Long
    ): PendingIntent =
        getBroadcast(
            context,
            (habit.id!! % Integer.MAX_VALUE).toInt() + 1,
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SHOW_REMINDER
                data = Uri.parse(habit.uriString)
                putExtra("timestamp", timestamp)
                putExtra("reminderTime", reminderTime)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun showReminder(
        habitGroup: HabitGroup,
        reminderTime: Long?,
        timestamp: Long
    ): PendingIntent =
        getBroadcast(
            context,
            (habitGroup.id!! % Integer.MAX_VALUE).toInt() + 1,
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SHOW_REMINDER
                data = Uri.parse(habitGroup.uriString)
                putExtra("timestamp", timestamp)
                putExtra("reminderTime", reminderTime)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun snoozeNotification(habit: Habit): PendingIntent =
        getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                data = Uri.parse(habit.uriString)
                action = ReminderReceiver.ACTION_SNOOZE_REMINDER
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun snoozeNotification(habitGroup: HabitGroup): PendingIntent =
        getBroadcast(
            context,
            0,
            Intent(context, ReminderReceiver::class.java).apply {
                data = Uri.parse(habitGroup.uriString)
                action = ReminderReceiver.ACTION_SNOOZE_REMINDER
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun toggleCheckmark(habit: Habit, timestamp: Long?): PendingIntent =
        getBroadcast(
            context,
            2,
            Intent(context, WidgetReceiver::class.java).apply {
                data = Uri.parse(habit.uriString)
                action = WidgetReceiver.ACTION_TOGGLE_REPETITION
                if (timestamp != null) putExtra("timestamp", timestamp)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun updateWidgets(): PendingIntent =
        getBroadcast(
            context,
            0,
            Intent(context, WidgetReceiver::class.java).apply {
                action = WidgetReceiver.ACTION_UPDATE_WIDGETS_VALUE
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

    fun showNumberPicker(habit: Habit, date: LocalDate): PendingIntent? {
        return getActivity(
            context,
            (habit.id!! % Integer.MAX_VALUE).toInt() + 1,
            Intent(context, ListHabitsActivity::class.java).apply {
                action = ListHabitsActivity.ACTION_EDIT
                putExtra("habit", habit.id)
                putExtra("timestamp", date.unixTime)
            },
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
    }

    fun showNumberPickerTemplate(): PendingIntent {
        return getActivity(
            context,
            1,
            Intent(context, ListHabitsActivity::class.java).apply {
                action = ListHabitsActivity.ACTION_EDIT
            },
            getIntentTemplateFlags()
        )
    }

    fun showNumberPickerFillIn(habit: Habit, date: LocalDate) = Intent().apply {
        putExtra("habit", habit.id)
        putExtra("timestamp", date.unixTime)
    }

    fun showHabitList(): PendingIntent {
        return getActivity(
            context,
            1,
            Intent(context, ListHabitsActivity::class.java),
            getIntentTemplateFlags()
        )
    }

    fun showHabitListAndScrollToGroup(habitGroup: HabitGroup): PendingIntent {
        val id = habitGroup.id ?: 0L
        return getActivity(
            context,
            (id % Integer.MAX_VALUE).toInt() + 1,
            Intent(context, ListHabitsActivity::class.java).apply {
                putExtra("SCROLL_TO_HABIT_GROUP_ID", id)
                data = Uri.parse("content://org.isoron.uhabits/scroll/habitgroup/$id")
            },
            getIntentTemplateFlags() or FLAG_UPDATE_CURRENT
        )
    }

    fun showHabitListWithNotificationClear(id: Long): PendingIntent {
        return getActivity(
            context,
            0,
            Intent(context, ListHabitsActivity::class.java).apply {
                putExtra("CLEAR_NOTIFICATION_HABIT_ID", id)
            },
            getIntentTemplateFlags()
        )
    }

    private fun getIntentTemplateFlags(): Int {
        var flags = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags or FLAG_MUTABLE
        }
        return flags
    }

    fun toggleCheckmarkTemplate(): PendingIntent =
        getBroadcast(
            context,
            2,
            Intent(context, WidgetReceiver::class.java).apply {
                action = WidgetReceiver.ACTION_TOGGLE_REPETITION
            },
            getIntentTemplateFlags()
        )

    fun toggleCheckmarkFillIn(habit: Habit, date: LocalDate) = Intent().apply {
        data = Uri.parse(habit.uriString)
        putExtra("timestamp", date.unixTime)
    }
}
