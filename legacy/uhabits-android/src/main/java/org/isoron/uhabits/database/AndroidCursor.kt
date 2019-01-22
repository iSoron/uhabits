/*
 * Copyright (C) 2015-2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.database

import org.isoron.uhabits.core.database.*

class AndroidCursor(private val cursor: android.database.Cursor) : Cursor {

    override fun close() = cursor.close()
    override fun moveToNext() = cursor.moveToNext()

    override fun getInt(index: Int): Int? {
        if (cursor.isNull(index)) return null
        else return cursor.getInt(index)
    }

    override fun getLong(index: Int): Long? {
        if (cursor.isNull(index)) return null
        else return cursor.getLong(index)
    }

    override fun getDouble(index: Int): Double? {
        if (cursor.isNull(index)) return null
        else return cursor.getDouble(index)
    }

    override fun getString(index: Int): String? {
        if (cursor.isNull(index)) return null
        else return cursor.getString(index)
    }
}
