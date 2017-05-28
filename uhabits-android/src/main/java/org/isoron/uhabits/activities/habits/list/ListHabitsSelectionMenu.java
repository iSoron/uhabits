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

package org.isoron.uhabits.activities.habits.list;

import android.support.annotation.*;
import android.view.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;

import javax.inject.*;

@ActivityScope
public class ListHabitsSelectionMenu extends BaseSelectionMenu
    implements HabitCardListController.SelectionListener
{
    @NonNull
    private final ListHabitsScreen screen;

    @NonNull
    CommandRunner commandRunner;

    private ListHabitsSelectionMenuBehavior behavior;

    @NonNull
    private final HabitCardListAdapter listAdapter;

    @Nullable
    private HabitCardListController listController;

    @Inject
    public ListHabitsSelectionMenu(@NonNull ListHabitsScreen screen,
                                   @NonNull HabitCardListAdapter listAdapter,
                                   @NonNull CommandRunner commandRunner,
                                   @NonNull ListHabitsSelectionMenuBehavior behavior)
    {
        this.screen = screen;
        this.listAdapter = listAdapter;
        this.commandRunner = commandRunner;
        this.behavior = behavior;
    }

    @Override
    public void onFinish()
    {
        if (listController != null) listController.onSelectionFinished();
        super.onFinish();
    }

    @Override
    public boolean onItemClicked(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit_habit:
                behavior.onEditHabits();
                return true;

            case R.id.action_archive_habit:
                behavior.onArchiveHabits();
                return true;

            case R.id.action_unarchive_habit:
                behavior.onUnarchiveHabits();
                return true;

            case R.id.action_delete:
                behavior.onDeleteHabits();
                return true;

            case R.id.action_color:
                behavior.onChangeColor();
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onPrepare(@NonNull Menu menu)
    {
        MenuItem itemEdit = menu.findItem(R.id.action_edit_habit);
        MenuItem itemColor = menu.findItem(R.id.action_color);
        MenuItem itemArchive = menu.findItem(R.id.action_archive_habit);
        MenuItem itemUnarchive = menu.findItem(R.id.action_unarchive_habit);

        itemColor.setVisible(true);
        itemEdit.setVisible(behavior.canEdit());
        itemArchive.setVisible(behavior.canArchive());
        itemUnarchive.setVisible(behavior.canUnarchive());
        setTitle(Integer.toString(listAdapter.getSelected().size()));

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

    public void setListController(HabitCardListController listController)
    {
        this.listController = listController;
    }

    @Override
    protected int getResourceId()
    {
        return R.menu.list_habits_selection;
    }
}
