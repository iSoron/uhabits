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

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.records.*;
import org.isoron.uhabits.core.test.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.isoron.uhabits.core.models.Entry.*;

public class SQLiteRepetitionListTest extends BaseUnitTest
{
    private Habit habit;

    private Timestamp today;

    private RepetitionList originalCheckmarks;

    private long day;

    private Repository<EntryRecord> repository;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        Database db = buildMemoryDatabase();
        modelFactory = new SQLModelFactory(db);
        habitList = modelFactory.buildHabitList();
        fixtures = new HabitFixtures(modelFactory, habitList);
        repository = new Repository<>(EntryRecord.class, db);
        habit = fixtures.createLongHabit();

        originalCheckmarks = habit.getOriginalEntries();
        today = DateUtils.getToday();
    }

    @Test
    public void testAdd()
    {
        EntryRecord record = getByTimestamp(today.plus(1));
        assertNull(record);

        Entry rep = new Entry(today.plus(1), YES_MANUAL);
        habit.getOriginalEntries().add(rep);

        record = getByTimestamp(today.plus(1));
        assertNotNull(record);
        assertThat(record.value, equalTo(YES_MANUAL));
    }

    @Test
    public void testGetByInterval()
    {
        List<Entry> checks =
            originalCheckmarks.getByInterval(today.minus(10), today);

        assertThat(checks.size(), equalTo(8));
        assertThat(checks.get(0).getTimestamp(), equalTo(today.minus(10)));
        assertThat(checks.get(4).getTimestamp(), equalTo(today.minus(5)));
        assertThat(checks.get(5).getTimestamp(), equalTo(today.minus(3)));
    }

    @Test
    public void testGetByTimestamp()
    {
        Entry rep = originalCheckmarks.getByTimestamp(today);
        assertNotNull(rep);
        assertThat(rep.getTimestamp(), equalTo(today));

        rep = originalCheckmarks.getByTimestamp(today.minus(2));
        assertNull(rep);
    }

    @Test
    public void testGetOldest()
    {
        Entry rep = originalCheckmarks.getOldest();
        assertNotNull(rep);
        assertThat(rep.getTimestamp(), equalTo(today.minus(120)));
    }

    @Test
    public void testGetOldest_withEmptyHabit()
    {
        Habit empty = fixtures.createEmptyHabit();
        Entry rep = empty.getOriginalEntries().getOldest();
        assertNull(rep);
    }

    @Test
    public void testRemove()
    {
        EntryRecord record = getByTimestamp(today);
        assertNotNull(record);

        Entry rep = record.toEntry();
        originalCheckmarks.remove(rep);

        record = getByTimestamp(today);
        assertNull(record);
    }

    @Nullable
    private EntryRecord getByTimestamp(Timestamp timestamp)
    {
        String query = "where habit = ? and timestamp = ?";
        String params[] = {
            Long.toString(habit.getId()), Long.toString(timestamp.getUnixTime())
        };

        return repository.findFirst(query, params);
    }
}
