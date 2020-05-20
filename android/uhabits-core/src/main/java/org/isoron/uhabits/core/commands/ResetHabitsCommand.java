package org.isoron.uhabits.core.commands;

import androidx.annotation.NonNull;

import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.models.RepetitionList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ResetHabitsCommand extends Command
{
    @NonNull
    final HabitList habitList;

    @NonNull
    final List<Habit> selected;

    public ResetHabitsCommand(@NonNull HabitList habitList, @NonNull List<Habit> selected) {
        this.habitList = habitList;
        this.selected = new LinkedList<>(selected);
    }

    @Override
    public void execute()
    {
        for (Habit h : selected) {
            RepetitionList repetitionList = h.getRepetitions();
            repetitionList.removeAll();
        }
    }
    public List<Habit> getSelected()
    {
        return Collections.unmodifiableList(selected);
    }

    @NonNull
    @Override
    public Object toRecord() { return null;}

    @Override
    public void undo() { throw new UnsupportedOperationException();}
}
