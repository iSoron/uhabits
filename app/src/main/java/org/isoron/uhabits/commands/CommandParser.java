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

import android.support.annotation.NonNull;

import org.isoron.uhabits.models.Habit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CommandParser
{
    public static Command fromJSON(JSONObject json) throws JSONException
    {
        switch(json.getString("event"))
        {
            case "ToggleRepetition":
                return ToggleRepetitionCommand.fromJSON(json);

            case "ArchiveHabits":
                return ArchiveHabitsCommand.fromJSON(json);

            case "UnarchiveHabits":
                return UnarchiveHabitsCommand.fromJSON(json);

            case "ChangeHabitColor":
                return ChangeHabitColorCommand.fromJSON(json);

            case "CreateHabit":
                return CreateHabitCommand.fromJSON(json);

            case "DeleteHabits":
                return DeleteHabitsCommand.fromJSON(json);

            case "EditHabit":
                return EditHabitCommand.fromJSON(json);

//            TODO: Implement this
//            case "ReorderHabit":
//                return ReorderHabitCommand.fromJSON(json);

        }

        return null;
    }

    @NonNull
    public static LinkedList<Habit> habitListFromJSON(JSONArray habitIds) throws JSONException
    {
        LinkedList<Habit> habits = new LinkedList<>();

        for (int i = 0; i < habitIds.length(); i++)
        {
            Long hId = habitIds.getLong(i);
            Habit h = Habit.get(hId);
            if(h == null) continue;

            habits.add(h);
        }

        return habits;
    }

    @NonNull
    protected static JSONArray habitListToJSON(List<Habit> habits)
    {
        JSONArray habitIds = new JSONArray();
        for(Habit h : habits) habitIds.put(h.getId());
        return habitIds;
    }
}
