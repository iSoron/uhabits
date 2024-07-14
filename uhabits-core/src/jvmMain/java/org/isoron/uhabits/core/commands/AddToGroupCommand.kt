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
package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitList

data class AddToGroupCommand(
    val habitList: HabitList,
    val hgr: HabitGroup,
    val selected: List<Habit>
) : Command {
    override fun run() {
        for (habit in selected) {
            val entries = habit.originalEntries.getKnown()
            val oldGroup = habit.group
            (oldGroup?.habitList ?: habitList).remove(habit)
            habit.groupId = hgr.id
            habit.groupUUID = hgr.uuid
            habit.group = hgr
            hgr.habitList.add(habit)
            entries.forEach { habit.originalEntries.add(it) }
        }
    }
}
