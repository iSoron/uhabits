/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.models.sqlite

import org.isoron.uhabits.core.database.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.models.sqlite.records.*
import org.isoron.uhabits.core.utils.*

class SQLiteEntryList(database: Database) : EntryList() {
    val repository = Repository(EntryRecord::class.java, database)
    var habitId: Long? = null
    var isLoaded = false

    private fun loadRecords() {
        if (isLoaded) return
        val habitId = habitId ?: throw IllegalStateException("habitId must be set")
        val records = repository.findAll("where habit = ? order by timestamp",
                                         habitId.toString())
        for (rec in records) super.add(rec.toEntry())
        isLoaded = true
    }

    override fun get(timestamp: Timestamp): Entry {
        loadRecords()
        return super.get(timestamp)
    }

    override fun getByInterval(from: Timestamp, to: Timestamp): List<Entry> {
        loadRecords()
        return super.getByInterval(from, to)
    }

    override fun add(entry: Entry) {
        loadRecords()
        val habitId = habitId ?: throw IllegalStateException("habitId must be set")

        // Remove existing rows
        repository.execSQL("delete from repetitions where habit = ? and timestamp = ?",
                           habitId.toString(),
                           entry.timestamp.unixTime.toString())

        // Add new row
        val record = EntryRecord().apply { copyFrom(entry) }
        record.habitId = habitId
        repository.save(record)

        // Add to memory list
        super.add(entry)
    }

    override fun getKnown(): List<Entry> {
        loadRecords()
        return super.getKnown()
    }

    override fun groupBy(
            original: List<Entry>,
            field: DateUtils.TruncateField,
            firstWeekday: Int,
            isNumerical: Boolean
    ): List<Entry> {
        loadRecords()
        return super.groupBy(original, field, firstWeekday, isNumerical)
    }

    override fun recomputeFrom(originalEntries: EntryList, frequency: Frequency, isNumerical: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        super.clear()
        repository.execSQL("delete from repetitions where habit = ?",
                           habitId.toString())
    }
}
