package org.isoron.uhabits.core.ui.widgets;

import android.support.annotation.NonNull;

import org.isoron.uhabits.core.commands.CommandRunner;
import org.isoron.uhabits.core.models.HabitList;

public class BaseWidgetBehavior {
    protected HabitList getHabitList() {
        return habitList;
    }

    @NonNull
    protected CommandRunner getCommandRunner() {
        return commandRunner;
    }

    private HabitList habitList;

    @NonNull
    private final CommandRunner commandRunner;

    public BaseWidgetBehavior(HabitList habitList, @NonNull CommandRunner commandRunner) {
        this.habitList = habitList;
        this.commandRunner = commandRunner;
    }
}
