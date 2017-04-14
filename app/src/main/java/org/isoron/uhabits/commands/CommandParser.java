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

import java.util.*;

public class CommandParser
{

    private HabitList habitList;

    private ModelFactory modelFactory;

    public CommandParser(@NonNull HabitList habitList,
                         @NonNull ModelFactory modelFactory)
    {
        this.habitList = habitList;
        this.modelFactory = modelFactory;
    }

    @NonNull
    public static LinkedList<Habit> habitListFromJSON(
        @NonNull HabitList habitList, @NonNull JSONArray habitIds)
        throws JSONException
    {
        LinkedList<Habit> habits = new LinkedList<>();

        for (int i = 0; i < habitIds.length(); i++)
        {
            Long hId = habitIds.getLong(i);
            Habit h = habitList.getById(hId);
            if (h == null) continue;

            habits.add(h);
        }

        return habits;
    }

    @NonNull
    protected static JSONArray habitListToJSON(List<Habit> habits)
    {
        JSONArray habitIds = new JSONArray();
        for (Habit h : habits) habitIds.put(h.getId());
        return habitIds;
    }

    @NonNull
    public Command fromJSON(@NonNull JSONObject json) throws JSONException
    {
        switch (json.getString("event"))
        {
            case "ToggleRepetition":
                return ToggleRepetitionCommand.fromJSON(json, habitList);

            case "ArchiveHabits":
                return ArchiveHabitsCommand.fromJSON(json, habitList);

            case "UnarchiveHabits":
                return UnarchiveHabitsCommand.fromJSON(json, habitList);

            case "ChangeHabitColor":
                return ChangeHabitColorCommand.fromJSON(json, habitList);

            case "CreateHabit":
                return CreateHabitCommand.fromJSON(json, habitList,
                    modelFactory);

            case "DeleteHabits":
                return DeleteHabitsCommand.fromJSON(json, habitList);

            case "EditHabit":
                return EditHabitCommand.fromJSON(json, habitList, modelFactory);

//            TODO: Implement this
//            case "ReorderHabit":
//                return ReorderHabitCommand.fromJSON(json);

        }

        return null;
    }
}
