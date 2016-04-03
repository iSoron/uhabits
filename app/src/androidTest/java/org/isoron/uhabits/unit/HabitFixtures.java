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

package org.isoron.uhabits.unit;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.isoron.uhabits.helpers.ColorHelper;
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;
import org.isoron.uhabits.tasks.ExportDBTask;
import org.isoron.uhabits.tasks.ImportDataTask;

import java.io.File;
import java.io.InputStream;
import java.util.Random;

import static org.junit.Assert.fail;

public class HabitFixtures
{
    public static boolean NON_DAILY_HABIT_CHECKS[] = { true, false, false, true, true, true, false,
            false, true, true };

    public static Habit createShortHabit()
    {
        Habit habit = new Habit();
        habit.name = "Wake up early";
        habit.description = "Did you wake up before 6am?";
        habit.freqNum = 2;
        habit.freqDen = 3;
        habit.save();

        long timestamp = DateHelper.getStartOfToday();
        for(boolean c : NON_DAILY_HABIT_CHECKS)
        {
            if(c) habit.repetitions.toggle(timestamp);
            timestamp -= DateHelper.millisecondsInOneDay;
        }

        return habit;
    }

    public static Habit createEmptyHabit()
    {
        Habit habit = new Habit();
        habit.name = "Meditate";
        habit.description = "Did you meditate this morning?";
        habit.color = ColorHelper.palette[3];
        habit.freqNum = 1;
        habit.freqDen = 1;
        habit.save();
        return habit;
    }

    public static Habit createLongHabit()
    {
        Habit habit = createEmptyHabit();
        habit.freqNum = 3;
        habit.freqDen = 7;
        habit.color = ColorHelper.palette[4];
        habit.save();

        long day = DateHelper.millisecondsInOneDay;
        long today = DateHelper.getStartOfToday();
        int marks[] = { 0, 1, 3, 5, 7, 8, 9, 10, 12, 14, 15, 17, 19, 20, 26, 27, 28, 50, 51, 52,
                53, 54, 58, 60, 63, 65, 70, 71, 72, 73, 74, 75, 80, 81, 83, 89, 90, 91, 95,
                102, 103, 108, 109, 120};

        for(int mark : marks)
            habit.repetitions.toggle(today - mark * day);

        return habit;
    }

    public static void generateHugeDataSet() throws Throwable
    {
        final int nHabits = 30;
        final int nYears = 5;

        DatabaseHelper.executeAsTransaction(new DatabaseHelper.Command()
        {
            @Override
            public void execute()
            {
                Random rand = new Random();

                for(int i = 0; i < nHabits; i++)
                {
                    Log.i("HabitFixture", String.format("Creating habit %d / %d", i, nHabits));

                    Habit habit = new Habit();
                    habit.name = String.format("Habit %d", i);
                    habit.save();

                    long today = DateHelper.getStartOfToday();
                    long day = DateHelper.millisecondsInOneDay;


                    for(int j = 0; j < 365 * nYears; j++)
                    {
                        if(rand.nextBoolean())
                            habit.repetitions.toggle(today - j * day);
                    }

                    habit.scores.getTodayValue();
                    habit.streaks.getAll(1);
                }
            }
        });

        ExportDBTask task = new ExportDBTask(null);
        task.setListener(new ExportDBTask.Listener()
        {
            @Override
            public void onExportDBFinished(@Nullable String filename)
            {
                if(filename != null)
                    Log.i("HabitFixture", String.format("Huge data set exported to %s", filename));
                else
                    Log.i("HabitFixture", "Failed to save database");
            }
        });
        task.execute();

        BaseTask.waitForTasks(30000);
    }

    public static void loadHugeDataSet(Context testContext) throws Throwable
    {
        File baseDir = DatabaseHelper.getFilesDir("Backups");
        if(baseDir == null) fail("baseDir should not be null");

        File dst = new File(String.format("%s/%s", baseDir.getPath(), "loopHuge.db"));
        InputStream in = testContext.getAssets().open("fixtures/loopHuge.db");
        DatabaseHelper.copy(in, dst);

        ImportDataTask task = new ImportDataTask(dst, null);
        task.execute();

        BaseTask.waitForTasks(30000);
    }

    public static void purgeHabits()
    {
        for(Habit h : Habit.getAll(true))
            h.cascadeDelete();
    }
}
