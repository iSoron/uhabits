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

package org.isoron.uhabits.core.ui.screens.habits.show;

import android.support.annotation.*;

import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;

import java.io.*;
import java.util.*;

import javax.inject.*;

public class ShowHabitMenuBehavior
{
    private HabitList habitList;

    @NonNull
    private final Habit habit;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private Screen screen;

    @NonNull
    private System system;

    @Inject
    public ShowHabitMenuBehavior(@NonNull HabitList habitList,
                                 @NonNull Habit habit,
                                 @NonNull TaskRunner taskRunner,
                                 @NonNull Screen screen,
                                 @NonNull System system)
    {
        this.habitList = habitList;
        this.habit = habit;
        this.taskRunner = taskRunner;
        this.screen = screen;
        this.system = system;
    }

    public void onEditHabit()
    {
        screen.showEditHabitScreen(habit);
    }

    public void onExportCSV()
    {
        List<Habit> selected = Collections.singletonList(habit);
        File outputDir = system.getCSVOutputDir();

        taskRunner.execute(
            new ExportCSVTask(habitList, selected, outputDir, filename ->
            {
                if (filename != null) screen.showSendFileScreen(filename);
                else screen.showMessage(Message.COULD_NOT_EXPORT);
            }));
    }

    public enum Message
    {
        COULD_NOT_EXPORT
    }

    public interface Screen
    {
        void showEditHabitScreen(@NonNull Habit habit);

        void showMessage(Message m);

        void showSendFileScreen(String filename);
    }

    public interface System
    {
        File getCSVOutputDir();
    }
}
