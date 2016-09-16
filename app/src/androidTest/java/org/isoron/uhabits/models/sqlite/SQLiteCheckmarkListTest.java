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

package org.isoron.uhabits.models.sqlite;

import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import com.activeandroid.query.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class SQLiteCheckmarkListTest extends BaseAndroidTest
{
    private Habit habit;

    private CheckmarkList checkmarks;

    private long today;

    private long day;

    @Override
    public void setUp()
    {
        super.setUp();

        habit = fixtures.createLongHabit();
        checkmarks = habit.getCheckmarks();
        checkmarks.getToday(); // compute checkmarks

        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;
    }

    @Test
    public void testAdd()
    {
        checkmarks.invalidateNewerThan(0);

        List<Checkmark> list = new LinkedList<>();
        list.add(new Checkmark(0, 0));
        list.add(new Checkmark(1, 1));
        list.add(new Checkmark(2, 2));

        checkmarks.add(list);

        List<CheckmarkRecord> records = getAllRecords();
        assertThat(records.size(), equalTo(3));
        assertThat(records.get(0).timestamp, equalTo(2L));
    }

    @Test
    public void testGetByInterval()
    {
        long from = today - 10 * day;
        long to = today - 3 * day;

        List<Checkmark> list = checkmarks.getByInterval(from, to);
        assertThat(list.size(), equalTo(8));

        assertThat(list.get(0).getTimestamp(), equalTo(today - 3 * day));
        assertThat(list.get(3).getTimestamp(), equalTo(today - 6 * day));
        assertThat(list.get(7).getTimestamp(), equalTo(today - 10 * day));
    }

    @Test
    public void testGetByInterval_withLongInterval()
    {
        long from = today - 200 * day;
        long to = today;

        List<Checkmark> list = checkmarks.getByInterval(from, to);
        assertThat(list.size(), equalTo(201));
    }

    @Test
    public void testInvalidateNewerThan()
    {
        List<CheckmarkRecord> records = getAllRecords();
        assertThat(records.size(), equalTo(121));

        checkmarks.invalidateNewerThan(today - 20 * day);

        records = getAllRecords();
        assertThat(records.size(), equalTo(100));
        assertThat(records.get(0).timestamp, equalTo(today - 21 * day));
    }

    private List<CheckmarkRecord> getAllRecords()
    {
        return new Select()
            .from(CheckmarkRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("timestamp desc")
            .execute();
    }

}
