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
package org.isoron.uhabits.core.test

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.sqlite.SQLiteHabitList

class HabitGroupFixtures(private val modelFactory: ModelFactory, private val habitList: HabitList, private val habitGroupList: HabitGroupList) {
    private val habitFixtures = HabitFixtures(modelFactory, habitList)

    fun createEmptyHabitGroup(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        id: Long = 0L
    ): HabitGroup {
        val hgr = modelFactory.buildHabitGroup()
        hgr.name = name
        hgr.id = id
        hgr.question = "Did you exercise today?"
        hgr.color = color
        hgr.position = position
        saveIfSQLite(hgr)
        return hgr
    }

    fun createGroupWithEmptyHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createEmptyHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        saveIfSQLite(hgr)
        return hgr
    }

    fun createGroupWithEmptyNumericalHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        targetType: NumericalHabitType = NumericalHabitType.AT_LEAST,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createEmptyNumericalHabit(targetType)
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        saveIfSQLite(hgr)
        return hgr
    }

    fun createGroupWithNumericalHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createNumericalHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        saveIfSQLite(hgr)
        return hgr
    }

    fun createGroupWithLongHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createLongHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        saveIfSQLite(hgr)
        return hgr
    }

    fun createGroupWithShortHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createShortHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        saveIfSQLite(hgr)
        return hgr
    }

    private fun saveIfSQLite(hgr: HabitGroup) {
        if (hgr.habitList !is SQLiteHabitList) return
        habitGroupList.add(hgr)
    }

    private fun addGroupId(h: Habit, hgr: HabitGroup) {
        h.groupId = hgr.id
        h.group = hgr
        h.groupUUID = hgr.uuid
    }
}
