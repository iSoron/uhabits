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

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.isoron.uhabits.tasks.*;

import java.io.*;

import javax.inject.*;

@ActivityScope
public class ListHabitsController
    implements HabitCardListController.HabitListener
{
    @NonNull
    private final ListHabitsBehavior behavior;

    @NonNull
    private final ListHabitsScreen screen;

    @NonNull
    private final HabitCardListAdapter adapter;

    @NonNull
    private final TaskRunner taskRunner;

    private ImportDataTaskFactory importTaskFactory;

    private ExportDBTaskFactory exportDBFactory;

    @Inject
    public ListHabitsController(@NonNull ListHabitsBehavior behavior,
                                @NonNull HabitCardListAdapter adapter,
                                @NonNull ListHabitsScreen screen,
                                @NonNull TaskRunner taskRunner,
                                @NonNull ImportDataTaskFactory importTaskFactory,
                                @NonNull ExportDBTaskFactory exportDBFactory)
    {
        this.behavior = behavior;
        this.adapter = adapter;
        this.screen = screen;
        this.taskRunner = taskRunner;
        this.importTaskFactory = importTaskFactory;
        this.exportDBFactory = exportDBFactory;
    }

    public void onEdit(@NonNull Habit habit, long timestamp)
    {
        behavior.onEdit(habit, timestamp);
    }

    public void onExportCSV()
    {
        behavior.onExportCSV();
    }

    public void onExportDB()
    {
        taskRunner.execute(exportDBFactory.create(filename ->
        {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        }));
    }

    @Override
    public void onHabitClick(@NonNull Habit h)
    {
        behavior.onClickHabit(h);
    }

    @Override
    public void onHabitReorder(@NonNull Habit from, @NonNull Habit to)
    {
        behavior.onReorderHabit(from, to);
    }

    public void onImportData(@NonNull File file,
                             @NonNull OnFinishedListener finishedListener)
    {
        taskRunner.execute(importTaskFactory.create(file, result ->
        {
            switch (result)
            {
                case ImportDataTask.SUCCESS:
                    adapter.refresh();
                    screen.showMessage(R.string.habits_imported);
                    break;

                case ImportDataTask.NOT_RECOGNIZED:
                    screen.showMessage(R.string.file_not_recognized);
                    break;

                default:
                    screen.showMessage(R.string.could_not_import);
                    break;
            }

            finishedListener.onFinish();
        }));
    }

    public void onInvalidEdit()
    {
        screen.showMessage(R.string.long_press_to_edit);
    }

    @Override
    public void onInvalidToggle()
    {
        screen.showMessage(R.string.long_press_to_toggle);
    }

    public void onRepairDB()
    {
        behavior.onRepairDB();
    }

    public void onSendBugReport()
    {
        behavior.onSendBugReport();
    }

    public void onStartup()
    {
        behavior.onStartup();
    }

    @Override
    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        behavior.onToggle(habit, timestamp);
    }

    public interface OnFinishedListener
    {
        void onFinish();
    }
}
