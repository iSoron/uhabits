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

import android.support.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import javax.inject.*;

import static org.isoron.uhabits.core.utils.DateUtils.*;

@AppScope
public class ReminderScheduler implements CommandRunner.Listener
{
    private CommandRunner commandRunner;

    private HabitList habitList;

    private SystemScheduler sys;

    private Map< Long, Long > customReminders; // Habit id, Timestamp

    @Inject
    public ReminderScheduler(@NonNull CommandRunner commandRunner,
                             @NonNull HabitList habitList,
                             @NonNull SystemScheduler sys)
    {
        this.commandRunner = commandRunner;
        this.habitList = habitList;
        this.sys = sys;
        customReminders = new HashMap< Long, Long >();
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if(command instanceof ToggleRepetitionCommand) return;
        if(command instanceof ChangeHabitColorCommand) return;
        if(command instanceof EditHabitCommand)
        {
            Long habitId = ((EditHabitCommand)command).getHabitId();
            Habit habit = habitList.getById( habitId );
            customReminders.remove( habitId );
            scheduleHabit( habit );
            return;
        }
        if(command instanceof DeleteHabitsCommand)
        {
            List<Long> habitIds = ((DeleteHabitsCommand)command).getHabitIds();
            for(Long id : habitIds)
                customReminders.remove( id );
            return;
        }
        scheduleAll();
    }

    public void scheduleHabit(@NonNull Habit habit)
    {
        Long reminderTime = null;
        if( customReminders.containsKey( habit.getId()))
            reminderTime = customReminders.get( habit.getId());
        scheduleInternal(habit, reminderTime);
    }

    public void scheduleHabitAtReminder(@NonNull Habit habit)
    {
        customReminders.remove( habit.getId());
        scheduleInternal( habit, null );
    }

    public void scheduleHabitAtCustom(@NonNull Habit habit, @NonNull Long reminderTime)
    {
        customReminders.put( habit.getId(), reminderTime );
        scheduleInternal(habit, reminderTime);
    }

    private void scheduleInternal(@NonNull Habit habit, @Nullable Long reminderTime)
    {
        if (!habit.hasReminder()) return;
        if (habit.isArchived()) return;
        Reminder reminder = habit.getReminder();
        if (reminderTime == null) reminderTime = getReminderTime(reminder);
        long timestamp = getStartOfDay(removeTimezone(reminderTime));

        sys.scheduleShowReminder(reminderTime, habit, timestamp);
    }

    public synchronized void scheduleAll()
    {
        HabitList reminderHabits =
            habitList.getFiltered(HabitMatcher.WITH_ALARM);
        for (Habit habit : reminderHabits)
            scheduleHabit(habit);
    }

    public void startListening()
    {
        commandRunner.addListener(this);
    }

    public void stopListening()
    {
        commandRunner.removeListener(this);
    }

    @NonNull
    private Long getReminderTime(@NonNull Reminder reminder)
    {
        Calendar calendar = DateUtils.getStartOfTodayCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);
        Long time = calendar.getTimeInMillis();

        if (DateUtils.getLocalTime() > time)
            time += DateUtils.DAY_LENGTH;

        return applyTimezone(time);
    }

    public interface SystemScheduler
    {
        void scheduleShowReminder(long reminderTime, Habit habit, long timestamp);
    }
}
