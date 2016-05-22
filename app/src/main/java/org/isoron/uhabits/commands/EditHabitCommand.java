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

public class EditHabitCommand extends Command
{
    private Habit original;
    private Habit modified;
    private long savedId;
    private boolean hasIntervalChanged;

    public EditHabitCommand(String id, Habit original, Habit modified)
    {
        super(id);
        init(original, modified);
    }

    public EditHabitCommand(Habit original, Habit modified)
    {
        init(original, modified);
    }

    private void init(Habit original, Habit modified)
    {
        this.savedId = original.getId();
        this.modified = new Habit(modified);
        this.original = new Habit(original);

        hasIntervalChanged = (!this.original.freqDen.equals(this.modified.freqDen) ||
                !this.original.freqNum.equals(this.modified.freqNum));
    }

    @Override
    public void execute()
    {
        copyAttributes(this.modified);
    }

    @Override
    public void undo()
    {
        copyAttributes(this.original);
    }

    private void copyAttributes(Habit model)
    {
        Habit habit = Habit.get(savedId);
        if(habit == null) throw new RuntimeException("Habit not found");

        habit.copyAttributes(model);
        habit.save();

        invalidateIfNeeded(habit);
    }

    private void invalidateIfNeeded(Habit habit)
    {
        if (hasIntervalChanged)
        {
            habit.checkmarks.deleteNewerThan(0);
            habit.streaks.deleteNewerThan(0);
            habit.scores.invalidateNewerThan(0);
        }
    }

    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_changed;
    }

    public Integer getUndoStringId()
    {
        return R.string.toast_habit_changed_back;
    }

    @Override
    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = super.toJSON();
            JSONObject data = root.getJSONObject("data");
            root.put("event", "EditHabit");
            data.put("id", savedId);
            data.put("params", modified.toJSON());
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
        Habit original = Habit.get(data.getLong("id"));
        if(original == null) return null;

        Habit modified = Habit.fromJSON(data.getJSONObject("params"));

        return new EditHabitCommand(commandId, original, modified);
    }
}