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

package org.isoron.uhabits.core.reminders;

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;

import java.util.*;

import javax.inject.*;

import static org.isoron.uhabits.core.utils.DateUtils.*;

@AppScope
public class ReminderScheduler implements CommandRunner.Listener
{
    private final WidgetPreferences widgetPreferences;

    private CommandRunner commandRunner;

    private HabitList habitList;

    private SystemScheduler sys;

    @Inject
    public ReminderScheduler(@NonNull CommandRunner commandRunner,
                             @NonNull HabitList habitList,
                             @NonNull SystemScheduler sys,
                             @NonNull WidgetPreferences widgetPreferences)
    {
        this.commandRunner = commandRunner;
        this.habitList = habitList;
        this.sys = sys;
        this.widgetPreferences = widgetPreferences;
    }

    @Override
    public synchronized void onCommandExecuted(@NonNull Command command,
                                               @Nullable Long refreshKey)
    {
        if (command instanceof ToggleRepetitionCommand) return;
        if (command instanceof ChangeHabitColorCommand) return;
        scheduleAll();
    }

    public synchronized void schedule(@NonNull Habit habit)
    {
        if (habit.getId() == null)
        {
            sys.log("ReminderScheduler", "Habit has null id. Returning.");
            return;
        }

        if (!habit.hasReminder())
        {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.");
            return;
        }

        long reminderTime = habit.getReminder().getTimeInMillis();
        long snoozeReminderTime = widgetPreferences.getSnoozeTime(habit.getId());

        if (snoozeReminderTime != 0)
        {
            long now = applyTimezone(getLocalTime());
            sys.log("ReminderScheduler", String.format(
                    Locale.US,
                    "Habit %d has been snoozed until %d",
                    habit.getId(),
                    snoozeReminderTime));

            if (snoozeReminderTime > now)
            {
                sys.log("ReminderScheduler", "Snooze time is in the future. Accepting.");
                reminderTime = snoozeReminderTime;
            }
            else
            {
                sys.log("ReminderScheduler", "Snooze time is in the past. Discarding.");
                widgetPreferences.removeSnoozeTime(habit.getId());
            }
        }
        scheduleAtTime(habit, reminderTime);

    }

    public synchronized void scheduleAtTime(@NonNull Habit habit, long reminderTime)
    {
        sys.log("ReminderScheduler", "Scheduling alarm for habit=" + habit.id);

        if (!habit.hasReminder())
        {
            sys.log("ReminderScheduler", "habit=" + habit.id + " has no reminder. Skipping.");
            return;
        }

        if (habit.isArchived())
        {
            sys.log("ReminderScheduler", "habit=" + habit.id + " is archived. Skipping.");
            return;
        }

        long timestamp = getStartOfDay(removeTimezone(reminderTime));
        sys.log("ReminderScheduler",
                String.format(
                        Locale.US,
                        "reminderTime=%d removeTimezone=%d timestamp=%d",
                        reminderTime,
                        removeTimezone(reminderTime),
                        timestamp));

        sys.scheduleShowReminder(reminderTime, habit, timestamp);
    }

    public synchronized void scheduleAll()
    {
        sys.log("ReminderScheduler", "Scheduling all alarms");
        HabitList reminderHabits =
                habitList.getFiltered(HabitMatcher.WITH_ALARM);
        for (Habit habit : reminderHabits)
            schedule(habit);
    }

    public synchronized void startListening()
    {
        commandRunner.addListener(this);
    }

    public synchronized void stopListening()
    {
        commandRunner.removeListener(this);
    }

    public synchronized void snoozeReminder(Habit habit, long minutes)
    {
        long now = applyTimezone(getLocalTime());
        long snoozedUntil = now + minutes * 60 * 1000;
        widgetPreferences.setSnoozeTime(habit.getId(), snoozedUntil);
        schedule(habit);
    }

    public interface SystemScheduler
    {
        void scheduleShowReminder(long reminderTime, Habit habit, long timestamp);

        void scheduleWidgetUpdate(long updateTime);

        void log(String componentName, String msg);
    }
}
