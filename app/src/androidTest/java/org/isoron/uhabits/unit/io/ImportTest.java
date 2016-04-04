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

package org.isoron.uhabits.unit.io;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.isoron.uhabits.BaseTest;
import org.isoron.uhabits.helpers.DatabaseHelper;
import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.io.GenericImporter;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.unit.HabitFixtures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImportTest extends BaseTest
{
    private File baseDir;
    private Context context;

    @Before
    public void setup()
    {
        super.setup();
        DateHelper.setFixedLocalTime(null);

        HabitFixtures.purgeHabits();
        context = InstrumentationRegistry.getInstrumentation().getContext();
        baseDir = DatabaseHelper.getFilesDir("Backups");
        if(baseDir == null) fail("baseDir should not be null");
    }

    private void copyAssetToFile(String assetPath, File dst) throws IOException
    {
        InputStream in = context.getAssets().open(assetPath);
        DatabaseHelper.copy(in, dst);
    }

    private void importFromFile(String assetFilename) throws IOException
    {
        File file = new File(String.format("%s/%s", baseDir.getPath(), assetFilename));
        copyAssetToFile(assetFilename, file);
        assertTrue(file.exists());
        assertTrue(file.canRead());

        GenericImporter importer = new GenericImporter();
        assertThat(importer.canHandle(file), is(true));

        importer.importHabitsFromFile(file);
    }

    private boolean containsRepetition(Habit h, int year, int month, int day)
    {
        GregorianCalendar date = DateHelper.getStartOfTodayCalendar();
        date.set(year, month - 1, day);
        return h.repetitions.contains(date.getTimeInMillis());
    }

    @Test
    public void testTickmateDB() throws IOException
    {
        importFromFile("tickmate.db");

        List<Habit> habits = Habit.getAll(true);
        assertThat(habits.size(), equalTo(3));

        Habit h = habits.get(0);
        assertThat(h.name, equalTo("Vegan"));
        assertTrue(containsRepetition(h, 2016, 1, 24));
        assertTrue(containsRepetition(h, 2016, 2, 5));
        assertTrue(containsRepetition(h, 2016, 3, 18));
        assertFalse(containsRepetition(h, 2016, 3, 14));
    }

    @Test
    public void testRewireDB() throws IOException
    {
        importFromFile("rewire.db");

        List<Habit> habits = Habit.getAll(true);
        assertThat(habits.size(), equalTo(3));

        Habit habit = habits.get(0);
        assertThat(habit.name, equalTo("Wake up early"));
        assertThat(habit.freqNum, equalTo(3));
        assertThat(habit.freqDen, equalTo(7));
        assertFalse(habit.hasReminder());
        assertFalse(containsRepetition(habit, 2015, 12, 31));
        assertTrue(containsRepetition(habit, 2016, 1, 18));
        assertTrue(containsRepetition(habit, 2016, 1, 28));
        assertFalse(containsRepetition(habit, 2016, 3, 10));

        habit = habits.get(1);
        assertThat(habit.name, equalTo("brush teeth"));
        assertThat(habit.freqNum, equalTo(3));
        assertThat(habit.freqDen, equalTo(7));
        assertThat(habit.reminderHour, equalTo(8));
        assertThat(habit.reminderMin, equalTo(0));
        boolean[] reminderDays = {false, true, true, true, true, true, false};
        assertThat(habit.reminderDays, equalTo(DateHelper.packWeekdayList(reminderDays)));
    }

    @Test
    public void testHabitBullCSV() throws IOException
    {
        importFromFile("habitbull.csv");

        List<Habit> habits = Habit.getAll(true);
        assertThat(habits.size(), equalTo(4));

        Habit habit = habits.get(0);
        assertThat(habit.name, equalTo("Breed dragons"));
        assertThat(habit.description, equalTo("with love and fire"));
        assertThat(habit.freqNum, equalTo(1));
        assertThat(habit.freqDen, equalTo(1));
        assertTrue(containsRepetition(habit, 2016, 3, 18));
        assertTrue(containsRepetition(habit, 2016, 3, 19));
        assertFalse(containsRepetition(habit, 2016, 3, 20));
    }

    @Test
    public void testLoopDB() throws IOException
    {
        importFromFile("loop.db");

        List<Habit> habits = Habit.getAll(true);
        assertThat(habits.size(), equalTo(9));

        Habit habit = habits.get(0);
        assertThat(habit.name, equalTo("Wake up early"));
        assertThat(habit.freqNum, equalTo(3));
        assertThat(habit.freqDen, equalTo(7));
        assertTrue(containsRepetition(habit, 2016, 3, 14));
        assertTrue(containsRepetition(habit, 2016, 3, 16));
        assertFalse(containsRepetition(habit, 2016, 3, 17));
    }
}
