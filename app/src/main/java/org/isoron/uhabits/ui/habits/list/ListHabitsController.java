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

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.CommandRunner;
import org.isoron.uhabits.commands.ToggleRepetitionCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ExportCSVTask;
import org.isoron.uhabits.tasks.ExportDBTask;
import org.isoron.uhabits.tasks.ImportDataTask;
import org.isoron.uhabits.ui.BaseSystem;
import org.isoron.uhabits.ui.habits.list.controllers.HabitCardListController;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.Preferences;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

public class ListHabitsController
    implements ImportDataTask.Listener, HabitCardListController.HabitListener
{
    @NonNull
    private final ListHabitsScreen screen;

    @NonNull
    private final BaseSystem system;

    @Inject
    Preferences prefs;

    @Inject
    CommandRunner commandRunner;

    public ListHabitsController(@NonNull ListHabitsScreen screen,
                                @NonNull BaseSystem system)
    {
        this.screen = screen;
        this.system = system;
        HabitsApplication.getComponent().inject(this);
    }

    public void onExportCSV()
    {
        ExportCSVTask task =
            new ExportCSVTask(Habit.getAll(true), screen.getProgressBar());
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
        Habit.reorder(from, to);
    }

    public void onImportData(File file)
    {
        ImportDataTask task = new ImportDataTask(file, screen.getProgressBar());
        task.setListener(this);
        task.execute();
    }

    @Override
    public void onImportDataFinished(int result)
    {
        switch (result)
        {
            case ImportDataTask.SUCCESS:
                screen.invalidate();
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
        } catch (IOException e)
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
        } catch (IOException e)
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

        system.updateWidgets();
        system.scheduleReminders();
    }

    @Override
    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            null);
    }

    private void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.updateLastHint(-1, DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }
}
