/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.io

import me.tatarka.inject.annotations.Inject
import org.isoron.platform.io.Database
import org.isoron.platform.io.DatabaseOpener
import org.isoron.platform.io.UserFile
import org.isoron.platform.io.begin
import org.isoron.platform.io.commit
import org.isoron.platform.io.query
import org.isoron.platform.io.querySingle
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Frequency
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.utils.isSQLite3File

/**
 * Class that imports data from database files exported by Tickmate.
 */
@Inject
class TickmateDBImporter(
    private val habitList: HabitList,
    private val modelFactory: ModelFactory,
    private val opener: DatabaseOpener
) : AbstractImporter() {

    override suspend fun canHandle(file: UserFile): Boolean {
        if (!isSQLite3File(file)) return false
        val db = opener.open(file.pathString)
        val count = db.querySingle(
            "select count(*) from SQLITE_MASTER where name='tracks' or name='track2groups'"
        ) { it.getInt(0) }
        db.close()
        return count == 2
    }

    override suspend fun importHabitsFromFile(file: UserFile) {
        val db = opener.open(file.pathString)
        db.begin()
        createHabits(db)
        db.commit()
        db.close()
    }

    private fun createCheckmarks(db: Database, habit: Habit, tickmateTrackId: Int) {
        db.query(
            "select distinct year, month, day from ticks where _track_id=?",
            tickmateTrackId.toString()
        ) { stmt ->
            val year = stmt.getInt(0)
            val month = stmt.getInt(1)
            val day = stmt.getInt(2)
            habit.originalEntries.add(Entry(LocalDate(year, month + 1, day), Entry.YES_MANUAL))
        }
    }

    private fun createHabits(db: Database) {
        db.query("select _id, name, description from tracks") { stmt ->
            val id = stmt.getInt(0)
            val name = stmt.getText(1)
            val description = stmt.getTextOrNull(2) ?: ""
            val habit = modelFactory.buildHabit()
            habit.name = name
            habit.description = description
            habit.category = firstGroupName(db, id)
            habit.frequency = Frequency.DAILY
            habitList.add(habit)
            createCheckmarks(db, habit, id)
        }
    }

    private fun firstGroupName(db: Database, trackId: Int): String {
        return db.querySingle(
            "select g.name from groups as g " +
                "join track2groups as t2g on t2g._group_id = g._id " +
                "where t2g._track_id = ? " +
                "order by g.\"order\" asc, g._id asc limit 1",
            trackId.toString()
        ) { it.getText(0) } ?: ""
    }
}
