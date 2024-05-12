package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList

data class DeleteHabitGroupsCommand(
    val habitGroupList: HabitGroupList,
    val selected: List<HabitGroup>
) : Command {
    override fun run() {
        for (selectedGroup in selected) {
            // Fetch the true original group by ID to bypass the filtered clone
            val habitGroup = habitGroupList.getById(selectedGroup.id!!) ?: continue

            val habitsToDelete = habitGroup.habitList.toList()
            for (habit in habitsToDelete) {
                habitGroup.habitList.remove(habit)
            }

            habitGroupList.remove(habitGroup)
        }
    }
}
