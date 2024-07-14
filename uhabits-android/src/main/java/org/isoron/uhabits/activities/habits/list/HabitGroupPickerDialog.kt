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

package org.isoron.uhabits.activities.habits.list

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.commands.AddToGroupCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.inject.HabitsApplicationComponent

class HabitGroupPickerDialog : Activity() {

    private lateinit var habitGroupList: HabitGroupList
    private lateinit var habitList: HabitList
    private lateinit var selected: List<Habit>
    private lateinit var component: HabitsApplicationComponent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component = (applicationContext as HabitsApplication).component
        AndroidThemeSwitcher(this, component.preferences).apply()
        habitList = component.habitList
        habitGroupList = component.habitGroupList

        val selectedIds = intent.getLongArrayExtra("selected")!!
        selected = selectedIds.map { id ->
            habitList.getById(id) ?: habitGroupList.getHabitByID(id)!!
        }

        val groupIds = ArrayList<Long>()
        val groupNames = ArrayList<String>()

        for (hgr in habitGroupList) {
            if (hgr.isArchived) continue
            groupIds.add(hgr.id!!)
            groupNames.add(hgr.name)
        }

        if (groupNames.isEmpty()) {
            setContentView(R.layout.widget_empty_activity)
            findViewById<TextView>(R.id.message).setText(R.string.no_habit_groups)
            return
        }

        setContentView(R.layout.widget_configure_activity)
        val listView = findViewById<ListView>(R.id.listView)

        with(listView) {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1,
                groupNames
            )
            setOnItemClickListener { parent, view, position, id ->
                performTransfer(groupIds[position])
            }
        }
    }
    private fun performTransfer(toGroupId: Long) {
        val hgr = habitGroupList.getById(toGroupId)!!
        selected = selected.filter { it.groupId != hgr.id }
        component.commandRunner.run(
            AddToGroupCommand(habitList, hgr, selected)
        )
        finish()
    }
}
