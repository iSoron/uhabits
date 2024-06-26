package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.models.PaletteColor

data class ChangeHabitGroupColorCommand(
    val habitGroupList: HabitGroupList,
    val selected: List<HabitGroup>,
    val newColor: PaletteColor
) : Command {
    override fun run() {
        for (hgr in selected) hgr.color = newColor
        habitGroupList.update(selected)
    }
}
