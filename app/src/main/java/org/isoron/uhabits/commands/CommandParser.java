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

import com.google.gson.*;

import org.isoron.uhabits.models.*;
import org.json.*;

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
    public Command parse(@NonNull JSONObject json) throws JSONException
    {
        String event = json.getString("event");
        Gson gson = new GsonBuilder().create();

        if (event.equals("Archive")) return gson
            .fromJson(json.toString(), ArchiveHabitsCommand.Record.class)
            .toCommand(habitList);

        if (event.equals("ChangeColor")) return gson
            .fromJson(json.toString(), ChangeHabitColorCommand.Record.class)
            .toCommand(habitList);

        if (event.equals("CreateHabit")) return gson
            .fromJson(json.toString(), CreateHabitCommand.Record.class)
            .toCommand(modelFactory, habitList);

        if (event.equals("CreateRep")) return gson
            .fromJson(json.toString(), CreateRepetitionCommand.Record.class)
            .toCommand(habitList);

        if (event.equals("DeleteHabit")) return gson
            .fromJson(json.toString(), DeleteHabitsCommand.Record.class)
            .toCommand(habitList);

        if (event.equals("EditHabit")) return gson
            .fromJson(json.toString(), EditHabitCommand.Record.class)
            .toCommand(modelFactory, habitList);

        if (event.equals("Toggle")) return gson
            .fromJson(json.toString(), ToggleRepetitionCommand.Record.class)
            .toCommand(habitList);

        if (event.equals("Unarchive")) return gson
            .fromJson(json.toString(), UnarchiveHabitsCommand.Record.class)
            .toCommand(habitList);

        throw new IllegalStateException("Unknown command");
    }
}
