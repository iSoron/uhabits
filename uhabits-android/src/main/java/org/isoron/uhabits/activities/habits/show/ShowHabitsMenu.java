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

package org.isoron.uhabits.activities.habits.show;

import android.support.annotation.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;

import java.util.*;

import javax.inject.*;

@ActivityScope
public class ShowHabitsMenu extends BaseMenu
{
    @NonNull
    private final ShowHabitScreen screen;

    @NonNull
    private final Habit habit;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private ExportCSVTaskFactory exportCSVFactory;

    @Inject
    public ShowHabitsMenu(@NonNull BaseActivity activity,
                          @NonNull ShowHabitScreen screen,
                          @NonNull Habit habit,
                          @NonNull ExportCSVTaskFactory exportCSVFactory,
                          @NonNull TaskRunner taskRunner)
    {
        super(activity);
        this.screen = screen;
        this.habit = habit;
        this.taskRunner = taskRunner;
        this.exportCSVFactory = exportCSVFactory;
    }

    public void exportHabit()
    {
        List<Habit> selected = new LinkedList<>();
        selected.add(habit);
        ExportCSVTask task = exportCSVFactory.create(selected, filename -> {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        });
        taskRunner.execute(task);
    }

    @Override
    public boolean onItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit_habit:
                screen.showEditHabitDialog();
                return true;

            case R.id.export:
                this.exportHabit();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected int getMenuResourceId()
    {
        return R.menu.show_habit;
    }
}
