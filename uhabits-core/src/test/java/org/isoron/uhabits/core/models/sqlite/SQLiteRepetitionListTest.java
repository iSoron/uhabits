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

package org.isoron.uhabits.core.models.sqlite;

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.records.*;
import org.isoron.uhabits.core.test.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Checkmark.CHECKED_EXPLICITLY;

public class SQLiteRepetitionListTest extends BaseUnitTest
{
    private Habit habit;

    private long today;

    private RepetitionList repetitions;

    private long day;

    private Repository<RepetitionRecord> repository;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        Database db = buildMemoryDatabase();
        modelFactory = new SQLModelFactory(db);
        fixtures = new HabitFixtures(modelFactory);
        habitList = modelFactory.buildHabitList();
        repository = new Repository<>(RepetitionRecord.class, db);
        habit = fixtures.createLongHabit();

        repetitions = habit.getRepetitions();
        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;
    }

    @Test
    public void testAdd()
    {
        RepetitionRecord record = getByTimestamp(today + day);
        assertNull(record);

        Repetition rep = new Repetition(today + day, CHECKED_EXPLICITLY);
        habit.getRepetitions().add(rep);

        record = getByTimestamp(today + day);
        assertNotNull(record);
        assertThat(record.value, equalTo(CHECKED_EXPLICITLY));
    }

    @Test
    public void testGetByInterval()
    {
        List<Repetition> reps =
            repetitions.getByInterval(today - 10 * day, today);

        assertThat(reps.size(), equalTo(8));
        assertThat(reps.get(0).getTimestamp(), equalTo(today - 10 * day));
        assertThat(reps.get(4).getTimestamp(), equalTo(today - 5 * day));
        assertThat(reps.get(5).getTimestamp(), equalTo(today - 3 * day));
    }

    @Test
    public void testGetByTimestamp()
    {
        Repetition rep = repetitions.getByTimestamp(today);
        assertNotNull(rep);
        assertThat(rep.getTimestamp(), equalTo(today));

        rep = repetitions.getByTimestamp(today - 2 * day);
        assertNull(rep);
    }

    @Test
    public void testGetOldest()
    {
        Repetition rep = repetitions.getOldest();
        assertNotNull(rep);
        assertThat(rep.getTimestamp(), equalTo(today - 120 * day));
    }

    @Test
    public void testGetOldest_withEmptyHabit()
    {
        Habit empty = fixtures.createEmptyHabit();
        Repetition rep = empty.getRepetitions().getOldest();
        assertNull(rep);
    }

    @Test
    public void testRemove()
    {
        RepetitionRecord record = getByTimestamp(today);
        assertNotNull(record);

        Repetition rep = record.toRepetition();
        repetitions.remove(rep);

        record = getByTimestamp(today);
        assertNull(record);
    }

    @Nullable
    private RepetitionRecord getByTimestamp(long timestamp)
    {
        String query = "where habit = ? and timestamp = ?";
        String params[] = {
            Long.toString(habit.getId()), Long.toString(timestamp)
        };

        return repository.findFirst(query, params);
    }
}
