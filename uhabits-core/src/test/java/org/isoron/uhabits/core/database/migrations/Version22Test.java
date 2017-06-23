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

package org.isoron.uhabits.core.database.migrations;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.database.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.models.sqlite.*;
import org.isoron.uhabits.core.test.*;
import org.junit.*;
import org.junit.rules.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class Version22Test extends BaseUnitTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Database db;

    private MigrationHelper helper;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        db = openDatabaseResource("/databases/021.db");
        helper = new MigrationHelper(db);
        modelFactory = new SQLModelFactory(db);
        habitList = modelFactory.buildHabitList();
        fixtures = new HabitFixtures(modelFactory, habitList);
    }

    @Test
    public void testKeepValidReps() throws Exception
    {
        db.query("select count(*) from repetitions",
            (c) -> assertThat(c.getInt(0), equalTo(3)));

        helper.migrateTo(22);

        db.query("select count(*) from repetitions",
            (c) -> assertThat(c.getInt(0), equalTo(3)));
    }

    @Test
    public void testRemoveRepsWithInvalidId() throws Exception
    {
        db.execute("insert into Repetitions(habit, timestamp, value) " +
                   "values (99999, 100, 2)");
        db.query("select count(*) from repetitions where habit = 99999",
            (c) -> assertThat(c.getInt(0), equalTo(1)));

        helper.migrateTo(22);

        db.query("select count(*) from repetitions where habit = 99999",
            (c) -> assertThat(c.getInt(0), equalTo(0)));
    }

    @Test
    public void testDisallowNewRepsWithInvalidRef() throws Exception
    {
        helper.migrateTo(22);
        exception.expectMessage(containsString("FOREIGNKEY"));
        db.execute("insert into Repetitions(habit, timestamp, value) " +
                   "values (99999, 100, 2)");
    }

    @Test
    public void testRemoveRepetitionsWithNullTimestamp() throws Exception
    {
        db.execute("insert into repetitions(habit, value) values (0, 2)");
        db.query("select count(*) from repetitions where timestamp is null",
            (c) -> assertThat(c.getInt(0), equalTo(1)));

        helper.migrateTo(22);

        db.query("select count(*) from repetitions where timestamp is null",
            (c) -> assertThat(c.getInt(0), equalTo(0)));
    }

    @Test
    public void testDisallowNullTimestamp() throws Exception
    {
        helper.migrateTo(22);
        exception.expectMessage(containsString("SQLITE_CONSTRAINT_NOTNULL"));
        db.execute("insert into Repetitions(habit, value) " + "values (0, 2)");
    }

    @Test
    public void testRemoveRepetitionsWithNullHabit() throws Exception
    {
        db.execute("insert into repetitions(timestamp, value) values (0, 2)");
        db.query("select count(*) from repetitions where habit is null",
            (c) -> assertThat(c.getInt(0), equalTo(1)));

        helper.migrateTo(22);

        db.query("select count(*) from repetitions where habit is null",
            (c) -> assertThat(c.getInt(0), equalTo(0)));
    }

    @Test
    public void testDisallowNullHabit() throws Exception
    {
        helper.migrateTo(22);
        exception.expectMessage(containsString("SQLITE_CONSTRAINT_NOTNULL"));
        db.execute(
            "insert into Repetitions(timestamp, value) " + "values (5, 2)");
    }

    @Test
    public void testRemoveDuplicateRepetitions() throws Exception
    {
        db.execute("insert into repetitions(habit, timestamp, value)" +
                   "values (0, 100, 2)");
        db.execute("insert into repetitions(habit, timestamp, value)" +
                   "values (0, 100, 5)");
        db.execute("insert into repetitions(habit, timestamp, value)" +
                   "values (0, 100, 10)");
        db.query(
            "select count(*) from repetitions where timestamp=100 and habit=0",
            (c) -> assertThat(c.getInt(0), equalTo(3)));

        helper.migrateTo(22);

        db.query(
            "select count(*) from repetitions where timestamp=100 and habit=0",
            (c) -> assertThat(c.getInt(0), equalTo(1)));
    }

    @Test
    public void testDisallowNewDuplicateTimestamps() throws Exception
    {
        helper.migrateTo(22);
        db.execute("insert into repetitions(habit, timestamp, value)" +
                   "values (0, 100, 2)");
        exception.expectMessage(containsString("SQLITE_CONSTRAINT_UNIQUE"));
        db.execute("insert into repetitions(habit, timestamp, value)" +
                   "values (0, 100, 5)");
    }

    @Test
    public void testKeepHabitsUnchanged() throws Exception
    {
        Habit original = fixtures.createLongHabit();
        Reminder reminder = new Reminder(8, 30, new WeekdayList(100));
        original.setReminder(reminder);
        habitList.update(original);

        helper.migrateTo(22);

        ((SQLiteHabitList) habitList).reload();
        Habit modified = habitList.getById(original.getId());
        assertNotNull(modified);

        assertThat(original.getData(), equalTo(modified.getData()));
    }
}
