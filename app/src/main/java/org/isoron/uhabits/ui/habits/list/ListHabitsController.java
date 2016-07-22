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

import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.*;
import org.isoron.uhabits.ui.habits.list.controllers.*;
import org.isoron.uhabits.ui.habits.list.model.*;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

public class ListHabitsController
    implements ImportDataTask.Listener, HabitCardListController.HabitListener
{
    @NonNull
    private final ListHabitsScreen screen;

    @NonNull
    private final BaseSystem system;

    @NonNull
    private final HabitList habitList;

    @NonNull
    private final HabitCardListAdapter adapter;

    @Inject
    Preferences prefs;

    @Inject
    CommandRunner commandRunner;

    public ListHabitsController(@NonNull HabitList habitList,
                                @NonNull ListHabitsScreen screen,
                                @NonNull BaseSystem system,
                                @NonNull HabitCardListAdapter adapter)
    {
        this.screen = screen;
        this.system = system;
        this.habitList = habitList;
        this.adapter = adapter;
        HabitsApplication.getComponent().inject(this);
    }

    public void onExportCSV()
    {
        List<Habit> selected = new LinkedList<>();
        for (Habit h : habitList) selected.add(h);

        ProgressBar progressBar = screen.getProgressBar();
        ExportCSVTask task =
            new ExportCSVTask(habitList, selected, progressBar);

        task.setListener(filename -> {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        });

        task.execute();
    }

    public void onExportDB()
    {
        ExportDBTask task = new ExportDBTask(screen.getProgressBar());
        task.setListener(filename -> {
            if (filename != null) screen.showSendFileScreen(filename);
            else screen.showMessage(R.string.could_not_export);
        });
        task.execute();
    }

    @Override
    public void onHabitClick(@NonNull Habit h)
    {
        screen.showHabitScreen(h);
    }

    @Override
    public void onHabitReorder(@NonNull Habit from, @NonNull Habit to)
    {
        new SimpleTask(() -> habitList.reorder(from, to)).execute();
    }

    public void onImportData(@NonNull File file)
    {
        ProgressBar bar = screen.getProgressBar();
        ImportDataTask task = new ImportDataTask(habitList, file, bar);
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onImportDataFinished(int result)
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
    }

    @Override
    public void onInvalidToggle()
    {
        screen.showMessage(R.string.long_press_to_toggle);
    }

    public void onSendBugReport()
    {
        try
        {
            system.dumpBugReportToFile();
        }
        catch (IOException e)
        {
            // ignored
        }

        try
        {
            String log = "---------- BUG REPORT BEGINS ----------\n";
            log += system.getBugReport();
            log += "---------- BUG REPORT ENDS ------------\n";
            String to = "dev@loophabits.org";
            String subject = "Bug Report - Loop Habit Tracker";
            screen.showSendEmailScreen(log, to, subject);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            screen.showMessage(R.string.bug_report_failed);
        }
    }

    public void onStartup()
    {
        prefs.initialize();
        prefs.incrementLaunchCount();
        prefs.updateLastAppVersion();
        if (prefs.isFirstRun()) onFirstRun();

        new Handler().postDelayed(() -> {
            system.scheduleReminders();
            HabitsApplication.getWidgetUpdater().updateWidgets();
        }, 1000);
    }

    @Override
    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            habit.getId());
    }

    private void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.updateLastHint(-1, DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }
}
