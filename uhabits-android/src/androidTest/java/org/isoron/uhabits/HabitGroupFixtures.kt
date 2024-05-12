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
package org.isoron.uhabits

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.models.PaletteColor

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
        habitGroupList.add(hgr)
        hgr.recompute()
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
            val h = habitFixtures.createEmptyHabit(hgr.habitList)
            h.id = id + i
            addGroupId(h, hgr)
        }
        habitGroupList.update(hgr)
        hgr.recompute()
        return hgr
    }

    fun createGroupWithLongNumericalHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createLongNumericalHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        hgr.recompute()
        habitGroupList.update(hgr)
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
            val h = habitFixtures.createLongHabit(hgr.habitList)
            h.id = id + i
            addGroupId(h, hgr)
        }
        habitGroupList.update(hgr)
        hgr.recompute()
        return hgr
    }

    fun createGroupWithVeryLongHabits(
        name: String = "Exercise",
        color: PaletteColor = PaletteColor(3),
        position: Int = 0,
        numHabits: Int = 1,
        id: Long = 0L
    ): HabitGroup {
        val hgr = createEmptyHabitGroup(name, color, position, id)
        for (i in 1..numHabits) {
            val h = habitFixtures.createVeryLongHabit()
            h.id = id + i
            addGroupId(h, hgr)
            hgr.habitList.add(h)
        }
        habitGroupList.update(hgr)
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
            val h = habitFixtures.createShortHabit(hgr.habitList)
            h.id = id + i
            addGroupId(h, hgr)
        }
        habitGroupList.update(hgr)
        return hgr
    }

    @Synchronized
    fun purgeHabitGroups(habitGroupList: HabitGroupList) {
        habitGroupList.removeAll()
    }

    private fun addGroupId(h: Habit, hgr: HabitGroup) {
        h.groupId = hgr.id
        h.group = hgr
        h.groupUUID = hgr.uuid
    }
}
