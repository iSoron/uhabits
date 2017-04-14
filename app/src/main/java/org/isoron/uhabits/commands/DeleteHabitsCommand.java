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

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.json.*;

import java.util.*;

import static org.isoron.uhabits.commands.CommandParser.habitListFromJSON;
import static org.isoron.uhabits.commands.CommandParser.habitListToJSON;

/**
 * Command to delete a list of habits.
 */
public class DeleteHabitsCommand extends Command
{
    HabitList habitList;

    private List<Habit> habits;

    public DeleteHabitsCommand(@NonNull HabitList habitList,
                               @NonNull List<Habit> habits)
    {
        super();
        this.habits = habits;
        this.habitList = habitList;
    }

    public DeleteHabitsCommand(@NonNull String id,
                               @NonNull HabitList habitList,
                               @NonNull List<Habit> habits)
    {
        super(id);
        this.habits = habits;
        this.habitList = habitList;
    }

    @NonNull
    public static Command fromJSON(@NonNull JSONObject json,
                                   @NonNull HabitList habitList)
        throws JSONException
    {
        String id = json.getString("id");
        JSONObject data = (JSONObject) json.get("data");
        JSONArray habitIds = data.getJSONArray("ids");

        LinkedList<Habit> habits = habitListFromJSON(habitList, habitIds);
        return new DeleteHabitsCommand(id, habitList, habits);
    }

    @Override
    public void execute()
    {
        for (Habit h : habits)
            habitList.remove(h);
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_deleted;
    }

    public List<Habit> getHabits()
    {
        return new LinkedList<>(habits);
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_restored;
    }

    @Override
    @NonNull
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("event", "DeleteHabits");
            data.put("ids", habitListToJSON(habits));
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
        throw new UnsupportedOperationException();
    }
}
