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

import org.isoron.uhabits.utils.*;
import org.json.*;

/**
 * A Command represents a desired set of changes that should be performed on the
 * models.
 * <p>
 * A command can be executed and undone. Each of these operations also provide
 * an string that should be displayed to the user upon their completion.
 * <p>
 * In general, commands should always be executed by a {@link CommandRunner}.
 */
public abstract class Command
{
    private final String id;

    public Command()
    {
        id = DatabaseUtils.getRandomId();
    }

    public Command(String id)
    {
        this.id = id;
    }

    public abstract void execute();

    public Integer getExecuteStringId()
    {
        return null;
    }

    public Integer getUndoStringId()
    {
        return null;
    }

    public abstract void undo();

    public JSONObject toJSON()
    {
        try
        {
            JSONObject root = new JSONObject();
            JSONObject data = new JSONObject();
            root.put("id", getId());
            root.put("data", data);
            return root;
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getId()
    {
        return id;
    }
}
