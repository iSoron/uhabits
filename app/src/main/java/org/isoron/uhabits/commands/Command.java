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
}
