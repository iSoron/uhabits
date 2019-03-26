/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits

import org.isoron.uhabits.gui.*
import org.isoron.uhabits.models.*
import org.isoron.uhabits.utils.*


class Backend(databaseOpener: DatabaseOpener,
              fileOpener: FileOpener,
              log: Log) {

    private var database: Database
    private var habitsRepository: HabitRepository
    private var habits = mutableMapOf<Int, Habit>()
    var theme: Theme = LightTheme()

    init {
        val dbFile = fileOpener.openUserFile("uhabits.db")
        database = databaseOpener.open(dbFile)
        database.migrateTo(LOOP_DATABASE_VERSION, fileOpener, log)
        habitsRepository = HabitRepository(database)
        habits = habitsRepository.findAll()
    }

    fun getHabitList(): List<Map<String, *>> {
        return habits.values
                .filter { h -> !h.isArchived }
                .sortedBy { h -> h.position }
                .map { h ->
                    mapOf("key" to h.id.toString(),
                          "name" to h.name,
                          "color" to h.color.index)
                }
    }

    fun createHabit(name: String) {
        val id = habitsRepository.nextId()
        val habit = Habit(id = id,
                          name = name,
                          description = "",
                          frequency = Frequency(1, 1),
                          color = PaletteColor(3),
                          isArchived = false,
                          position = habits.size,
                          unit = "",
                          target = 0.0,
                          type = HabitType.YES_NO_HABIT)
        habitsRepository.insert(habit)
        habits[id] = habit
    }

    fun deleteHabit(id: Int) {
        val habit = habits[id]!!
        habitsRepository.delete(habit)
        habits.remove(id)
    }

    fun updateHabit(id: Int, name: String) {
        val habit = habits[id]!!
        habit.name = name
        habitsRepository.update(habit)
    }
}
