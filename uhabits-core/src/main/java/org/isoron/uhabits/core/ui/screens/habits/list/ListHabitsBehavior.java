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
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

public class ListHabitsBehavior
{
    @NonNull
    private final HabitList habitList;

    @NonNull
    private final DirFinder dirFinder;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private final Screen screen;

    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final Preferences prefs;

    @NonNull
    private final BugReporter bugReporter;

    @Inject
    public ListHabitsBehavior(@NonNull HabitList habitList,
                              @NonNull DirFinder dirFinder,
                              @NonNull TaskRunner taskRunner,
                              @NonNull Screen screen,
                              @NonNull CommandRunner commandRunner,
                              @NonNull Preferences prefs,
                              @NonNull BugReporter bugReporter)
    {
        this.habitList = habitList;
        this.dirFinder = dirFinder;
        this.taskRunner = taskRunner;
        this.screen = screen;
        this.commandRunner = commandRunner;
        this.prefs = prefs;
        this.bugReporter = bugReporter;
    }

    public void onClickHabit(@NonNull Habit h)
    {
        screen.showHabitScreen(h);
    }

    public void onEdit(@NonNull Habit habit, long timestamp)
    {
        CheckmarkList checkmarks = habit.getCheckmarks();
        double oldValue = checkmarks.getValues(timestamp, timestamp)[0];

        screen.showNumberPicker(oldValue / 1000, habit.getUnit(), newValue ->
        {
            newValue = Math.round(newValue * 1000);
            commandRunner.execute(
                new CreateRepetitionCommand(habit, timestamp, (int) newValue),
                habit.getId());
        });
    }

    public void onExportCSV()
    {
        List<Habit> selected = new LinkedList<>();
        for (Habit h : habitList) selected.add(h);
        File outputDir = dirFinder.getCSVOutputDir();

        taskRunner.execute(
            new ExportCSVTask(habitList, selected, outputDir, filename ->
            {
                if (filename != null) screen.showSendFileScreen(filename);
                else screen.showMessage(Message.COULD_NOT_EXPORT);
            }));
    }

    public void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.updateLastHint(-1, DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }

    public void onReorderHabit(@NonNull Habit from, @NonNull Habit to)
    {
        taskRunner.execute(() -> habitList.reorder(from, to));
    }

    public void onRepairDB()
    {
        taskRunner.execute(() ->
        {
            habitList.repair();
            screen.showMessage(Message.DATABASE_REPAIRED);
        });
    }

    public void onSendBugReport()
    {
        bugReporter.dumpBugReportToFile();

        try
        {
            String log = bugReporter.getBugReport();
            screen.showSendBugReportToDeveloperScreen(log);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            screen.showMessage(Message.COULD_NOT_GENERATE_BUG_REPORT);
        }
    }

    public void onStartup()
    {
        prefs.incrementLaunchCount();
        if (prefs.isFirstRun()) onFirstRun();
    }

    public void onToggle(@NonNull Habit habit, long timestamp)
    {
        commandRunner.execute(
            new ToggleRepetitionCommand(habitList, habit, timestamp),
            habit.getId());
    }

    public enum Message
    {
        COULD_NOT_EXPORT, IMPORT_SUCCESSFUL, IMPORT_FAILED, DATABASE_REPAIRED,
        COULD_NOT_GENERATE_BUG_REPORT, FILE_NOT_RECOGNIZED
    }

    public interface BugReporter
    {
        void dumpBugReportToFile();

        String getBugReport() throws IOException;
    }

    public interface DirFinder
    {
        File getCSVOutputDir();
    }

    public interface NumberPickerCallback
    {
        void onNumberPicked(double newValue);
    }

    public interface Screen
    {
        void showHabitScreen(@NonNull Habit h);

        void showIntroScreen();

        void showMessage(@NonNull Message m);

        void showNumberPicker(double value,
                              @NonNull String unit,
                              @NonNull NumberPickerCallback callback);

        void showSendBugReportToDeveloperScreen(String log);

        void showSendFileScreen(@NonNull String filename);
    }
}
