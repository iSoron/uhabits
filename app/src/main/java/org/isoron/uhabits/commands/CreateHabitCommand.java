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

import com.google.auto.factory.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.json.*;

/**
 * Command to create a habit.
 */
@AutoFactory
public class CreateHabitCommand extends Command
{
    private ModelFactory modelFactory;

    HabitList habitList;

    @NonNull
    private Habit model;

    @Nullable
    private Long savedId;

    public CreateHabitCommand(@Provided @NonNull ModelFactory modelFactory,
                              @NonNull HabitList habitList,
                              @NonNull Habit model)
    {
        super();
        this.modelFactory = modelFactory;
        this.habitList = habitList;
        this.model = model;
    }

    public CreateHabitCommand(@Provided @NonNull ModelFactory modelFactory,
                              @NonNull String commandId,
                              @NonNull HabitList habitList,
                              @NonNull Habit model,
                              @Nullable Long savedId)
    {
        super(commandId);
        this.modelFactory = modelFactory;
        this.habitList = habitList;
        this.model = model;
        this.savedId = savedId;
    }

    @NonNull
    public static Command fromJSON(@NonNull JSONObject root,
                                   @NonNull HabitList habitList,
                                   @NonNull ModelFactory modelFactory)
        throws JSONException
    {
        String commandId = root.getString("id");
        JSONObject data = (JSONObject) root.get("data");
        Habit model = Habit.fromJSON(data.getJSONObject("habit"), modelFactory);
        Long savedId = data.getLong("id");

        return new CreateHabitCommand(modelFactory, commandId, habitList, model,
            savedId);
    }

    @Override
    public void execute()
    {
        Habit savedHabit = modelFactory.buildHabit();
        savedHabit.copyFrom(model);
        savedHabit.setId(savedId);

        habitList.add(savedHabit);
        savedId = savedHabit.getId();
    }

    @Override
    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_created;
    }

    @Override
    public Integer getUndoStringId()
    {
        return R.string.toast_habit_deleted;
    }

    @Override
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("event", "CreateHabit");
            data.put("habit", model.toJSON());
            data.put("id", savedId);
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
        Habit habit = habitList.getById(savedId);
        if (habit == null) throw new RuntimeException("Habit not found");

        habitList.remove(habit);
    }
}