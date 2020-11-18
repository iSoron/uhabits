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

package org.isoron.uhabits.core.io;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Frequency.*;
import static org.junit.Assert.assertTrue;

public class ImportTest extends BaseUnitTest
{
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        DateUtils.setFixedLocalTime(null);
    }

    @Test
    public void testHabitBullCSV() throws IOException
    {
        importFromFile("habitbull.csv");

        assertThat(habitList.size(), equalTo(4));

        Habit habit = habitList.getByPosition(0);
        assertThat(habit.getName(), equalTo("Breed dragons"));
        assertThat(habit.getDescription(), equalTo("with love and fire"));
        assertThat(habit.getFrequency(), equalTo(Frequency.DAILY));
        assertTrue(containsRepetition(habit, 2016, 3, 18));
        assertTrue(containsRepetition(habit, 2016, 3, 19));
        assertFalse(containsRepetition(habit, 2016, 3, 20));
    }

    @Test
    public void testLoopDB() throws IOException
    {
        importFromFile("loop.db");

        assertThat(habitList.size(), equalTo(9));

        Habit habit = habitList.getByPosition(0);
        assertThat(habit.getName(), equalTo("Wake up early"));
        assertThat(habit.getFrequency(), equalTo(THREE_TIMES_PER_WEEK));
        assertTrue(containsRepetition(habit, 2016, 3, 14));
        assertTrue(containsRepetition(habit, 2016, 3, 16));
        assertFalse(containsRepetition(habit, 2016, 3, 17));
    }

    @Test
    public void testRewireDB() throws IOException
    {
        importFromFile("rewire.db");

        assertThat(habitList.size(), equalTo(3));

        Habit habit = habitList.getByPosition(1);
        assertThat(habit.getName(), equalTo("Wake up early"));
        assertThat(habit.getFrequency(), equalTo(THREE_TIMES_PER_WEEK));
        assertFalse(habit.hasReminder());
        assertFalse(containsRepetition(habit, 2015, 12, 31));
        assertTrue(containsRepetition(habit, 2016, 1, 18));
        assertTrue(containsRepetition(habit, 2016, 1, 28));
        assertFalse(containsRepetition(habit, 2016, 3, 10));

        habit = habitList.getByPosition(2);
        assertThat(habit.getName(), equalTo("brush teeth"));
        assertThat(habit.getFrequency(), equalTo(THREE_TIMES_PER_WEEK));
        assertThat(habit.hasReminder(), equalTo(true));

        Reminder reminder = habit.getReminder();
        assertThat(reminder.getHour(), equalTo(8));
        assertThat(reminder.getMinute(), equalTo(0));
        boolean[] reminderDays = { false, true, true, true, true, true, false };
        assertThat(reminder.getDays().toArray(), equalTo(reminderDays));
    }

    @Test
    public void testTickmateDB() throws IOException
    {
        importFromFile("tickmate.db");

        assertThat(habitList.size(), equalTo(3));

        Habit h = habitList.getByPosition(2);
        assertThat(h.getName(), equalTo("Vegan"));
        assertTrue(containsRepetition(h, 2016, 1, 24));
        assertTrue(containsRepetition(h, 2016, 2, 5));
        assertTrue(containsRepetition(h, 2016, 3, 18));
        assertFalse(containsRepetition(h, 2016, 3, 14));
    }

    private boolean containsRepetition(Habit h, int year, int month, int day)
    {
        GregorianCalendar date = DateUtils.getStartOfTodayCalendar();
        date.set(year, month - 1, day);
        return h.getRepetitions().containsTimestamp(new Timestamp(date));
    }

    private void importFromFile(String assetFilename) throws IOException
    {
        File file = File.createTempFile("asset", "");
        copyAssetToFile(assetFilename, file);
        assertTrue(file.exists());
        assertTrue(file.canRead());

        GenericImporter importer = new GenericImporter(habitList,
            new LoopDBImporter(habitList, modelFactory, databaseOpener),
            new RewireDBImporter(habitList, modelFactory, databaseOpener),
            new TickmateDBImporter(habitList, modelFactory, databaseOpener),
            new HabitBullCSVImporter(habitList, modelFactory));

        assertTrue(importer.canHandle(file));
        importer.importHabitsFromFile(file);

        file.delete();
    }
}
