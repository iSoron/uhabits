/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.database;

import android.support.annotation.*;

public interface Cursor extends AutoCloseable
{
    @Override
    void close();

    /**
     * Moves the cursor forward one row from its current position. Returns
     * true if the current position is valid, or false if the cursor is already
     * past the last row. The cursor start at position -1, so this method must
     * be called first.
     */
    boolean moveToNext();

    /**
     * Retrieves the value of the designated column in the current row of this
     * Cursor as an Integer. If the value is null, returns null. The first
     * column has index zero.
     */
    @Nullable
    Integer getInt(int index);

    /**
     * Retrieves the value of the designated column in the current row of this
     * Cursor as a Long. If the value is null, returns null. The first
     * column has index zero.
     */
    @Nullable
    Long getLong(int index);

    /**
     * Retrieves the value of the designated column in the current row of this
     * Cursor as a Double. If the value is null, returns null. The first
     * column has index zero.
     */
    @Nullable
    Double getDouble(int index);

    /**
     * Retrieves the value of the designated column in the current row of this
     * Cursor as a String. If the value is null, returns null. The first
     * column has index zero.
     */
    @Nullable
    String getString(int index);
}
