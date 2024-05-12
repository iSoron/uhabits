package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList

data class UnarchiveHabitGroupsCommand(
    val habitGroupList: HabitGroupList,
    val selected: List<HabitGroup>
) : Command {
    override fun run() {
        for (hgr in selected) {
            hgr.isArchived = false
            for (h in hgr.habitList) {
                h.isArchived = false
            }
            val parentHgr = habitGroupList.getById(hgr.id!!) ?: continue
            parentHgr.isArchived = false
            for (h in parentHgr.habitList) {
                h.isArchived = false
            }
        }
        habitGroupList.update(selected)
    }
}
