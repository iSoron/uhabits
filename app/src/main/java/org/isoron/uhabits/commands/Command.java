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

import org.isoron.uhabits.helpers.DatabaseHelper;
import org.json.JSONObject;

public abstract class Command
{
    private final String id;

    public Command()
    {
        id = DatabaseHelper.getRandomId();
    }

    public Command(String id)
    {
        this.id = id;
    }

    public abstract void execute();

    public abstract void undo();

    public Integer getExecuteStringId()
    {
        return null;
    }

    public Integer getUndoStringId()
    {
        return null;
    }

    @Nullable
    public JSONObject toJSON() { return null; }

    public String getId()
    {
        return id;
    }
}
