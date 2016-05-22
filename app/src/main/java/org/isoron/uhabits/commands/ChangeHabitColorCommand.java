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
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.models.Habit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChangeHabitColorCommand extends Command
{
    List<Habit> habits;
    List<Integer> originalColors;
    Integer newColor;

    public ChangeHabitColorCommand(String id, List<Habit> habits, Integer newColor)
    {
        super(id);
        init(habits, newColor);
    }

    public ChangeHabitColorCommand(List<Habit> habits, Integer newColor)
    {
        init(habits, newColor);
    }

    private void init(List<Habit> habits, Integer newColor)
    {
        this.habits = habits;
        this.newColor = newColor;
        this.originalColors = new ArrayList<>(habits.size());

        for(Habit h : habits)
            originalColors.add(h.color);
    }

    @Override
    public void execute()
    {
        Habit.setColor(habits, newColor);
    }

    @Override
    public void undo()
    {
        DatabaseHelper.executeAsTransaction(new DatabaseHelper.Command()
        {
            @Override
            public void execute()
            {
                int k = 0;
                for(Habit h : habits)
                {
                    h.color = originalColors.get(k++);
                    h.save();
                }
            }
        });
    }

    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_changed;
    }

    public Integer getUndoStringId()
    {
        return R.string.toast_habit_changed;
    }

    @Override
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("event", "ChangeHabitColor");
            data.put("ids", CommandParser.habitListToJSON(habits));
            data.put("color", newColor);
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
        int newColor = data.getInt("color");

        LinkedList<Habit> habits = CommandParser.habitListFromJSON(habitIds);
        return new ChangeHabitColorCommand(id, habits, newColor);
    }
}
