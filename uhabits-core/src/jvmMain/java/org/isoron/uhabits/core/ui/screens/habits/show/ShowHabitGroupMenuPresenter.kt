package org.isoron.uhabits.core.ui.screens.habits.show

import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.DeleteHabitGroupsCommand
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import java.io.File

class ShowHabitGroupMenuPresenter(
    private val commandRunner: CommandRunner,
    private val habitGroup: HabitGroup,
    private val habitGroupList: HabitGroupList,
    private val screen: Screen
) {
    fun onEditHabitGroup() {
        screen.showEditHabitGroupScreen(habitGroup)
    }

    fun onDeleteHabitGroup() {
        screen.showDeleteConfirmationScreen {
            commandRunner.run(DeleteHabitGroupsCommand(habitGroupList, listOf(habitGroup)))
            screen.close()
        }
    }

    enum class Message {
        COULD_NOT_EXPORT
    }

    interface Screen {
        fun showEditHabitGroupScreen(habitGroup: HabitGroup)
        fun showMessage(m: Message?)
        fun showSendFileScreen(filename: String)
        fun showDeleteConfirmationScreen(callback: OnConfirmedCallback)
        fun close()
        fun refresh()
    }

    interface System {
        fun getCSVOutputDir(): File
    }
}
