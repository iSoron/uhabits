/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.ui.habits.list;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.ArchiveHabitsCommand;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.commands.CommandRunner;
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.BaseSelectionMenu;
import org.isoron.uhabits.ui.habits.list.controllers.HabitCardListController;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListAdapter;

import java.util.List;

import javax.inject.Inject;

public class ListHabitsSelectionMenu extends BaseSelectionMenu
    implements HabitCardListController.SelectionListener
{
    @NonNull
    private final ListHabitsScreen screen;

    @Inject
    CommandRunner commandRunner;

    @Nullable
    private HabitCardListAdapter adapter;

    public ListHabitsSelectionMenu(@NonNull ListHabitsScreen screen)
    {
        this.screen = screen;
        HabitsApplication.getComponent().inject(this);
    }

    @Override
    public void onFinish()
    {
        if (adapter != null) adapter.clearSelection();
        super.onFinish();
    }

    @Override
    public boolean onItemClicked(@NonNull MenuItem item)
    {
        if (adapter == null) return false;

        List<Habit> selected = adapter.getSelected();
        if (selected.isEmpty()) return false;

        Habit firstHabit = selected.get(0);

        switch (item.getItemId())
        {
            case R.id.action_edit_habit:
                edit(firstHabit);
                finish();
                return true;

            case R.id.action_archive_habit:
                archive(selected);
                finish();
                return true;

            case R.id.action_unarchive_habit:
                unarchive(selected);
                finish();
                return true;

            case R.id.action_delete:
                delete(selected);
                return true;

            case R.id.action_color:
                showColorPicker(selected, firstHabit);
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onPrepare(@NonNull Menu menu)
    {
        if (adapter == null) return false;
        List<Habit> selected = adapter.getSelected();

        boolean showEdit = (selected.size() == 1);
        boolean showArchive = true;
        boolean showUnarchive = true;
        for (Habit h : selected)
        {
            if (h.isArchived()) showArchive = false;
            else showUnarchive = false;
        }

        MenuItem itemEdit = menu.findItem(R.id.action_edit_habit);
        MenuItem itemColor = menu.findItem(R.id.action_color);
        MenuItem itemArchive = menu.findItem(R.id.action_archive_habit);
        MenuItem itemUnarchive = menu.findItem(R.id.action_unarchive_habit);

        itemColor.setVisible(true);
        itemEdit.setVisible(showEdit);
        itemArchive.setVisible(showArchive);
        itemUnarchive.setVisible(showUnarchive);

        setTitle(Integer.toString(selected.size()));

        return true;
    }

    @Override
    public void onSelectionChange()
    {
        invalidate();
    }

    @Override
    public void onSelectionFinish()
    {
        finish();
    }

    @Override
    public void onSelectionStart()
    {
        screen.startSelection();
    }

    public void setAdapter(@Nullable HabitCardListAdapter adapter)
    {
        if (adapter == null) return;
        this.adapter = adapter;
    }

    private void archive(@NonNull List<Habit> selected)
    {
        commandRunner.execute(new ArchiveHabitsCommand(selected), null);
    }

    private void delete(@NonNull List<Habit> selected)
    {
        screen.showDeleteConfirmationScreen(() -> {
            commandRunner.execute(new DeleteHabitsCommand(selected), null);
            finish();
        });
    }

    private void edit(@NonNull Habit firstHabit)
    {
        screen.showEditHabitScreen(firstHabit);
    }

    @Override
    protected int getResourceId()
    {
        return R.menu.list_habits_selection;
    }

    private void showColorPicker(@NonNull List<Habit> selected,
                                 @NonNull Habit firstHabit)
    {
        screen.showColorPicker(firstHabit, color -> {
            commandRunner.execute(new ChangeHabitColorCommand(selected, color),
                null);
            finish();
        });
    }

    private void unarchive(@NonNull List<Habit> selected)
    {
        commandRunner.execute(new UnarchiveHabitsCommand(selected), null);
    }
}
