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

package org.isoron.uhabits.models

import org.isoron.uhabits.utils.Database
import org.isoron.uhabits.utils.StepResult

class HabitList(var db: Database) {
    var habits = mutableListOf<Habit>()

    init {
        loadHabitsFromDatabase()
    }

    fun getActive(): List<Habit> {
        return habits.filter { h -> !h.isArchived }
    }

    private fun loadHabitsFromDatabase() {
        val stmt = db.prepareStatement(
                "select id, name, description, freq_num, freq_den, color, " +
                        "archived, position, unit, target_value, type " +
                        "from habits")

        while (stmt.step() == StepResult.ROW) {
            habits.add(Habit(id = stmt.getInt(0),
                             name = stmt.getText(1),
                             description = stmt.getText(2),
                             frequency = Frequency(stmt.getInt(3),
                                                   stmt.getInt(4)),
                             color = Color(stmt.getInt(5)),
                             isArchived = (stmt.getInt(6) != 0),
                             position = stmt.getInt(7),
                             unit = stmt.getText(8),
                             target = 0,
                             type = HabitType.YES_NO_HABIT))
        }
        stmt.finalize()
    }
}