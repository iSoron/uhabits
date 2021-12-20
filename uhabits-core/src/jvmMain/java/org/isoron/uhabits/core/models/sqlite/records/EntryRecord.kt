/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.models.sqlite.records

import org.isoron.uhabits.core.database.Column
import org.isoron.uhabits.core.database.Table
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Timestamp

/**
 * The SQLite database record corresponding to a [Entry].
 */
@Table(name = "Repetitions")
class EntryRecord {
    var habit: HabitRecord? = null

    @field:Column(name = "habit")
    var habitId: Long? = null

    @field:Column
    var timestamp: Long? = null

    @field:Column
    var value: Int? = null

    @field:Column
    var id: Long? = null

    @field:Column
    var notes: String? = null
    fun copyFrom(entry: Entry) {
        timestamp = entry.timestamp.unixTime
        value = entry.value
        notes = entry.notes
    }

    fun toEntry(): Entry {
        val notes = notes ?: ""
        return Entry(Timestamp(timestamp!!), value!!, notes)
    }
}
