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

package org.isoron.uhabits.io;

import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;
import com.opencsv.CSVReader;

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

public class HabitBullCSVImporter extends AbstractImporter
{
    @Override
    public boolean canHandle(@NonNull File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();

        return line.startsWith("HabitName,HabitDescription,HabitCategory");
    }

    @Override
    public void importHabitsFromFile(@NonNull final File file) throws IOException
    {
        ActiveAndroid.beginTransaction();
        try
        {
            parseFile(file);
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            ActiveAndroid.endTransaction();
        }
    }

    private void parseFile(@NonNull File file) throws IOException
    {
        CSVReader reader = new CSVReader(new FileReader(file));
        HashMap<String, Habit> habits = new HashMap<>();

        for(String line[] : reader)
        {
            String name = line[0];
            if(name.equals("HabitName")) continue;

            String description = line[1];
            String dateString[] = line[3].split("-");
            int year = Integer.parseInt(dateString[0]);
            int month = Integer.parseInt(dateString[1]);
            int day = Integer.parseInt(dateString[2]);

            Calendar date = DateHelper.getStartOfTodayCalendar();
            date.set(year, month - 1, day);

            long timestamp = date.getTimeInMillis();

            int value = Integer.parseInt(line[4]);
            if(value != 1) continue;

            Habit h = habits.get(name);

            if(h == null)
            {
                h = new Habit();
                h.name = name;
                h.description = description;
                h.freqNum = h.freqDen = 1;
                h.save();

                habits.put(name, h);
            }

            if(!h.repetitions.contains(timestamp))
                h.repetitions.toggle(timestamp);
        }
    }
}
