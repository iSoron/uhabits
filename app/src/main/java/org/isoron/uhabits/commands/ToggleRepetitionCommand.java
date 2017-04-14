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

package org.isoron.uhabits.commands;

import android.support.annotation.*;

import org.isoron.uhabits.models.*;
import org.json.*;

/**
 * Command to toggle a repetition.
 */
public class ToggleRepetitionCommand extends Command
{
    private Long timestamp;

    private Habit habit;

    public ToggleRepetitionCommand(@NonNull Habit habit, long timestamp)
    {
        super();
        this.timestamp = timestamp;
        this.habit = habit;
    }

    public ToggleRepetitionCommand(@NonNull String id,
                                   @NonNull Habit habit,
                                   long timestamp)
    {
        super(id);
        this.timestamp = timestamp;
        this.habit = habit;
    }

    @NonNull
    public static Command fromJSON(@NonNull JSONObject json,
                                   @NonNull HabitList habitList)
        throws JSONException
    {
        String id = json.getString("id");
        JSONObject data = (JSONObject) json.get("data");
        Long habitId = data.getLong("habit");
        Long timestamp = data.getLong("timestamp");

        Habit habit = habitList.getById(habitId);
        if (habit == null) throw new HabitNotFoundException();

        return new ToggleRepetitionCommand(id, habit, timestamp);
    }

    @Override
    public void execute()
    {
        habit.getRepetitions().toggleTimestamp(timestamp);
    }

    public Habit getHabit()
    {
        return habit;
    }

    @Override
    @NonNull
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("event", "ToggleRepetition");
            data.put("habit", habit.getId());
            data.put("timestamp", timestamp);
            return root;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void undo()
    {
        execute();
    }
}