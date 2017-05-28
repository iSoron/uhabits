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

package org.isoron.uhabits.ui.screens.habits.list;

import android.support.annotation.*;

import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.preferences.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

public class ListHabitsBehavior
{
    private HabitList habitList;

    private System system;

    private TaskRunner taskRunner;

    private Screen screen;

    private CommandRunner commandRunner;

    private Preferences prefs;

    @Inject
    public ListHabitsBehavior(@NonNull HabitList habitList,
                              @NonNull System system,
                              @NonNull TaskRunner taskRunner,
                              @NonNull Screen screen,
                              @NonNull CommandRunner commandRunner,
                              @NonNull Preferences prefs)
    {
        this.habitList = habitList;
        this.system = system;
        this.taskRunner = taskRunner;
        this.screen = screen;
        this.commandRunner = commandRunner;
        this.prefs = prefs;
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
        File outputDir = system.getCSVOutputDir();

        taskRunner.execute(
            new ExportCSVTask(habitList, selected, outputDir, filename ->
            {
                if (filename != null) screen.showSendFileScreen(filename);
                else screen.showMessage(Message.COULD_NOT_EXPORT);
            }));
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
        system.dumpBugReportToFile();

        try
        {
            String log = system.getBugReport();
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
        commandRunner.execute(new ToggleRepetitionCommand(habit, timestamp),
            habit.getId());
    }

    public void onFirstRun()
    {
        prefs.setFirstRun(false);
        prefs.updateLastHint(-1, DateUtils.getStartOfToday());
        screen.showIntroScreen();
    }

    public enum Message
    {
        COULD_NOT_EXPORT, IMPORT_SUCCESSFUL, IMPORT_FAILED, DATABASE_REPAIRED,
        COULD_NOT_GENERATE_BUG_REPORT, FILE_NOT_RECOGNIZED
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

    public interface System
    {
        void dumpBugReportToFile();

        String getBugReport() throws IOException;

        File getCSVOutputDir();
    }
}
