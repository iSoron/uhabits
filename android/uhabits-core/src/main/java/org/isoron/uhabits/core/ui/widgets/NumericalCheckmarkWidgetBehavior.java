package org.isoron.uhabits.core.ui.widgets;

import android.support.annotation.NonNull;

import org.isoron.uhabits.core.commands.CommandRunner;
import org.isoron.uhabits.core.commands.CreateRepetitionCommand;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.Timestamp;

import javax.inject.Inject;

public class NumericalCheckmarkWidgetBehavior extends BaseWidgetBehavior{
    @Inject
    public NumericalCheckmarkWidgetBehavior(@NonNull HabitList habitList,
                                            @NonNull CommandRunner commandRunner){
        super(habitList, commandRunner);
    }

    public void setNumericValue(@NonNull Habit habit, Timestamp timestamp, int newValue) {
        getCommandRunner().execute(
                new CreateRepetitionCommand(habit, timestamp, newValue),
                habit.getId());
    }
}
