/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.screens.habits.list;

import android.support.annotation.*;

import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.callbacks.*;

import java.util.*;

import javax.inject.*;

public class ListHabitsSelectionMenuBehavior
{
    @NonNull
    private final Screen screen;

    @NonNull
    CommandRunner commandRunner;

    @NonNull
    private final Adapter adapter;

    @NonNull
    private final HabitList habitList;

    @Inject
    public ListHabitsSelectionMenuBehavior(@NonNull HabitList habitList,
                                           @NonNull Screen screen,
                                           @NonNull Adapter adapter,
                                           @NonNull CommandRunner commandRunner)
    {
        this.habitList = habitList;
        this.screen = screen;
        this.adapter = adapter;
        this.commandRunner = commandRunner;
    }

    public boolean canArchive()
    {
        for (Habit h : adapter.getSelected())
            if (h.isArchived()) return false;

        return true;
    }

    public boolean canEdit()
    {
        return (adapter.getSelected().size() == 1);
    }

    public boolean canUnarchive()
    {
        for (Habit h : adapter.getSelected())
            if (!h.isArchived()) return false;

        return true;
    }

    public void onArchiveHabits()
    {
        commandRunner.execute(
            new ArchiveHabitsCommand(habitList, adapter.getSelected()), null);
        adapter.clearSelection();
    }

    public void onChangeColor()
    {
        List<Habit> selected = adapter.getSelected();
        Habit first = selected.get(0);

        screen.showColorPicker(first.getColor(), selectedColor ->
        {
            commandRunner.execute(
                new ChangeHabitColorCommand(habitList, selected, selectedColor),
                null);
            adapter.clearSelection();
        });
    }

    public void onDeleteHabits()
    {
        List<Habit> selected = adapter.getSelected();
        screen.showDeleteConfirmationScreen(() ->
        {
            adapter.performRemove(selected);
            commandRunner.execute(new DeleteHabitsCommand(habitList, selected),
                null);
            adapter.clearSelection();
        });
    }

    public void onEditHabits()
    {
        screen.showEditHabitsScreen(adapter.getSelected());
    }

    public void onUnarchiveHabits()
    {
        commandRunner.execute(
            new UnarchiveHabitsCommand(habitList, adapter.getSelected()), null);
        adapter.clearSelection();
    }

    public interface Adapter
    {
        void clearSelection();

        List<Habit> getSelected();

        void performRemove(List<Habit> selected);
    }

    public interface Screen
    {
        void showColorPicker(int defaultColor,
                             @NonNull OnColorPickedCallback callback);

        void showDeleteConfirmationScreen(
            @NonNull OnConfirmedCallback callback);

        void showEditHabitsScreen(@NonNull List<Habit> selected);
    }
}
