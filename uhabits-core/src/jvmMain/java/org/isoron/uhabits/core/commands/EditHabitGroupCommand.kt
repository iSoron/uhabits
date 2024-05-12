package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.HabitNotFoundException

data class EditHabitGroupCommand(
    val habitGroupList: HabitGroupList,
    val habitGroupId: Long,
    val modified: HabitGroup
) : Command {
    override fun run() {
        val habitGroup = habitGroupList.getById(habitGroupId) ?: throw HabitNotFoundException()
        habitGroup.copyFrom(modified)
        habitGroupList.update(habitGroup)
        habitGroup.observable.notifyListeners()
        habitGroup.recompute()
        habitGroupList.resort()
    }
}
