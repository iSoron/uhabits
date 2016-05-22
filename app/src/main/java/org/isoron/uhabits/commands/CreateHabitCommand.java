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

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateHabitCommand extends Command
{
    private Habit model;
    private Long savedId;

    public CreateHabitCommand(String id, Habit model, Long savedId)
    {
        super(id);
        this.model = model;
        this.savedId = savedId;
    }

    public CreateHabitCommand(Habit model)
    {
        this.model = model;
    }

    @Override
    public void execute()
    {
        Habit savedHabit = new Habit(model);
        if (savedId == null)
        {
            savedHabit.save();
            savedId = savedHabit.getId();
        }
        else
        {
            savedHabit.save(savedId);
        }
    }

    @Override
    public void undo()
    {
        Habit habit = Habit.get(savedId);
        if(habit == null) throw new RuntimeException("Habit not found");

        habit.cascadeDelete();
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

    @Nullable
    public static Command fromJSON(JSONObject root) throws JSONException
    {
        String commandId = root.getString("id");
        JSONObject data = (JSONObject) root.get("data");
        Habit model = Habit.fromJSON(data.getJSONObject("habit"));
        Long savedId = data.getLong("id");

        return new CreateHabitCommand(commandId, model, savedId);
    }
}