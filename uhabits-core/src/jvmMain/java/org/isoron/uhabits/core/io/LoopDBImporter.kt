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
package org.isoron.uhabits.core.io

import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.DATABASE_VERSION
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateHabitCommand
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.EditHabitCommand
import org.isoron.uhabits.core.database.DatabaseOpener
import org.isoron.uhabits.core.database.MigrationHelper
import org.isoron.uhabits.core.database.Repository
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.sqlite.records.EntryRecord
import org.isoron.uhabits.core.models.sqlite.records.HabitRecord
import org.isoron.uhabits.core.utils.isSQLite3File
import java.io.File
import javax.inject.Inject

/**
 * Class that imports data from database files exported by Loop Habit Tracker.
 */
class LoopDBImporter
@Inject constructor(
    @AppScope val habitList: HabitList,
    @AppScope val modelFactory: ModelFactory,
    @AppScope val opener: DatabaseOpener,
    @AppScope val runner: CommandRunner,
    @AppScope logging: Logging,
) : AbstractImporter() {

    private val logger = logging.getLogger("LoopDBImporter")

    override fun canHandle(file: File): Boolean {
        if (!file.isSQLite3File()) return false
        val db = opener.open(file)
        var canHandle = true
        val c = db.query("select count(*) from SQLITE_MASTER where name='Habits' or name='Repetitions'")
        if (!c.moveToNext() || c.getInt(0) != 2) {
            logger.error("Cannot handle file: tables not found")
            canHandle = false
        }
        if (db.version > DATABASE_VERSION) {
            logger.error("Cannot handle file: incompatible version: ${db.version} > $DATABASE_VERSION")
            canHandle = false
        }
        c.close()
        db.close()
        return canHandle
    }

    override fun importHabitsFromFile(file: File) {
        val db = opener.open(file)
        val helper = MigrationHelper(db)
        helper.migrateTo(DATABASE_VERSION)

        val habitsRepository = Repository(HabitRecord::class.java, db)
        val entryRepository = Repository(EntryRecord::class.java, db)

        for (habitRecord in habitsRepository.findAll("order by position")) {
            var habit = habitList.getByUUID(habitRecord.uuid)
            val entryRecords = entryRepository.findAll("where habit = ?", habitRecord.id.toString())

            var command: Command
            if (habit == null) {
                habit = modelFactory.buildHabit()
                habitRecord.id = null
                habitRecord.copyTo(habit)
                command = CreateHabitCommand(modelFactory, habitList, habit)
                command.run()
            } else {
                val modified = modelFactory.buildHabit()
                habitRecord.id = habit.id
                habitRecord.copyTo(modified)
                command = EditHabitCommand(habitList, habit.id!!, modified)
                command.run()
            }

            // Reload saved version of the habit
            habit = habitList.getByUUID(habitRecord.uuid)

            for (r in entryRecords) {
                val t = Timestamp(r.timestamp!!)
                val (_, value, notes) = habit!!.originalEntries.get(t)
                val oldNotes = r.notes ?: ""
                if (value != r.value || notes != oldNotes) CreateRepetitionCommand(habitList, habit, t, r.value!!, oldNotes).run()
            }

            runner.notifyListeners(command)
        }

        db.close()
    }
}
