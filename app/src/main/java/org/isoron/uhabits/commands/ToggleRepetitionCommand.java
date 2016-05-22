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

import android.support.annotation.Nullable;

import org.isoron.uhabits.models.Habit;
import org.json.JSONException;
import org.json.JSONObject;

public class ToggleRepetitionCommand extends Command
{
    private final Long timestamp;
    private final Habit habit;

    public ToggleRepetitionCommand(String id, Habit habit, long timestamp)
    {
        super(id);
        this.timestamp = timestamp;
        this.habit = habit;
    }

    public ToggleRepetitionCommand(Habit habit, long timestamp)
    {
        super();
        this.timestamp = timestamp;
        this.habit = habit;
    }

    @Override
    public void execute()
    {
        habit.repetitions.toggle(timestamp);
    }

    @Override
    public void undo()
    {
        execute();
    }

    @Override
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

    @Nullable
    public static Command fromJSON(JSONObject json) throws JSONException
    {
        String id = json.getString("id");
        JSONObject data = (JSONObject) json.get("data");
        Long habitId = data.getLong("habit");
        Long timestamp = data.getLong("timestamp");

        Habit habit = Habit.get(habitId);
        if(habit == null) return null;

        return new ToggleRepetitionCommand(id, habit, timestamp);
    }
}