package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.ModelFactory

data class CreateHabitGroupCommand(
    val modelFactory: ModelFactory,
    val habitGroupList: HabitGroupList,
    val model: HabitGroup
) : Command {
    override fun run() {
        val habitGroup = modelFactory.buildHabitGroup()
        habitGroup.copyFrom(model)
        habitGroupList.add(habitGroup)
        habitGroup.recompute()
    }
}
