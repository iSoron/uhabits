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

package org.isoron.uhabits.receivers;

import android.content.*;
import android.support.annotation.*;
import android.util.*;

import com.getpebble.android.kit.*;
import com.getpebble.android.kit.PebbleKit.*;
import com.getpebble.android.kit.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

public class PebbleReceiver extends PebbleDataReceiver
{
    public static final UUID WATCHAPP_UUID =
        UUID.fromString("82629d99-8ea6-4631-a022-9ca77a12a058");

    @NonNull
    private Context context;

    private HabitList allHabits;

    private CommandRunner commandRunner;

    private TaskRunner taskRunner;

    private HabitList filteredHabits;

    public PebbleReceiver()
    {
        super(WATCHAPP_UUID);
    }

    @Override
    public void receiveData(@Nullable Context context,
                            int transactionId,
                            @Nullable PebbleDictionary data)
    {
        if (context == null) throw new RuntimeException("context is null");
        if (data == null) throw new RuntimeException("data is null");

        this.context = context;

        HabitsApplication app =
            (HabitsApplication) context.getApplicationContext();

        commandRunner = app.getComponent().getCommandRunner();
        taskRunner = app.getComponent().getTaskRunner();
        allHabits = app.getComponent().getHabitList();

        HabitMatcher build = new HabitMatcherBuilder()
            .setArchivedAllowed(false)
            .setCompletedAllowed(false)
            .build();

        filteredHabits = allHabits.getFiltered(build);

        PebbleKit.sendAckToPebble(context, transactionId);
        Log.d("PebbleReceiver", "<-- " + data.getString(0));

        taskRunner.execute(() -> {
            switch (data.getString(0))
            {
                case "COUNT":
                    sendCount();
                    break;

                case "FETCH":
                    processFetch(data);
                    break;

                case "TOGGLE":
                    processToggle(data);
                    break;
            }
        });
    }

    private void processFetch(@NonNull PebbleDictionary dict)
    {
        Long position = dict.getInteger(1);
        if (position == null) return;
        if (position < 0 || position >= filteredHabits.size()) return;

        Habit habit = filteredHabits.getByPosition(position.intValue());
        if (habit == null) return;

        sendHabit(habit);
    }

    private void processToggle(@NonNull PebbleDictionary dict)
    {
        Long habitId = dict.getInteger(1);
        if (habitId == null) return;

        Habit habit = allHabits.getById(habitId);
        if (habit == null) return;

        long today = DateUtils.getStartOfToday();
        commandRunner.execute(new ToggleRepetitionCommand(habit, today),
            habitId);

        sendOK();
    }

    private void sendCount()
    {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "COUNT");
        dict.addInt32(1, filteredHabits.size());
        sendDict(dict);

        Log.d("PebbleReceiver",
            String.format("--> COUNT %d", filteredHabits.size()));
    }

    private void sendDict(@NonNull PebbleDictionary dict)
    {
        PebbleKit.sendDataToPebble(context,
            PebbleReceiver.WATCHAPP_UUID, dict);
    }

    private void sendHabit(@NonNull Habit habit)
    {
        if (habit.getId() == null) return;

        PebbleDictionary response = new PebbleDictionary();
        response.addString(0, "HABIT");
        response.addInt32(1, habit.getId().intValue());
        response.addString(2, habit.getName());
        response.addInt32(3, habit.getCheckmarks().getTodayValue());
        sendDict(response);
    }

    private void sendOK()
    {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "OK");
        sendDict(dict);
    }
}
