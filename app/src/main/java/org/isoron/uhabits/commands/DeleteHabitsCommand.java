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

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class DeleteHabitsCommand extends Command
{
    private List<Habit> habits;

    public DeleteHabitsCommand(String id, List<Habit> habits)
    {
        super(id);
        this.habits = habits;
    }

    public DeleteHabitsCommand(List<Habit> habits)
    {
        this.habits = habits;
    }

    @Override
    public void execute()
    {
        for(Habit h : habits)
            h.cascadeDelete();

        Habit.rebuildOrder();
    }

    @Override
    public void undo()
    {
        throw new UnsupportedOperationException();
    }

    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_deleted;
    }

    public Integer getUndoStringId()
    {
        return R.string.toast_habit_restored;
    }

    @Override
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("command", "DeleteHabits");
            data.put("ids", CommandParser.habitListToJSON(habits));
            return root;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Command fromJSON(JSONObject json) throws JSONException
    {
        String id = json.getString("id");
        JSONObject data = (JSONObject) json.get("data");
        JSONArray habitIds = data.getJSONArray("ids");

        LinkedList<Habit> habits = CommandParser.habitListFromJSON(habitIds);
        return new DeleteHabitsCommand(id, habits);
    }
}
