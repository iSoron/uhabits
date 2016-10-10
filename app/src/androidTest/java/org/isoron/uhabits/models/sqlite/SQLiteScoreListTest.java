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

@SuppressWarnings("JavaDoc")
@RunWith(AndroidJUnit4.class)
@MediumTest
public class SQLiteScoreListTest extends BaseAndroidTest
{
    private Habit habit;

    private ScoreList scores;

    private long today;

    private long day;

    @Override
    public void setUp()
    {
        super.setUp();

        habit = fixtures.createLongHabit();
        scores = habit.getScores();

        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;
    }

    @Test
    public void testGetAll()
    {
        List<Score> list = scores.toList();
        assertThat(list.size(), equalTo(121));
        assertThat(list.get(0).getTimestamp(), equalTo(today));
        assertThat(list.get(10).getTimestamp(), equalTo(today - 10 * day));
    }

    @Test
    public void testInvalidateNewerThan()
    {
        scores.getTodayValue(); // force recompute
        List<ScoreRecord> records = getAllRecords();
        assertThat(records.size(), equalTo(121));

        scores.invalidateNewerThan(today - 10 * day);

        records = getAllRecords();
        assertThat(records.size(), equalTo(110));
        assertThat(records.get(0).timestamp, equalTo(today - 11 * day));
    }

    @Test
    public void testAdd()
    {
        new Delete().from(ScoreRecord.class).execute();

        List<Score> list = new LinkedList<>();
        list.add(new Score(today, 0));
        list.add(new Score(today - day, 0));
        list.add(new Score(today - 2 * day, 0));

        scores.add(list);

        List<ScoreRecord> records = getAllRecords();
        assertThat(records.size(), equalTo(3));
        assertThat(records.get(0).timestamp, equalTo(today));
    }

    @Test
    public void testGetByInterval()
    {
        long from = today - 10 * day;
        long to = today - 3 * day;

        List<Score> list = scores.getByInterval(from, to);
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

        List<Score> list = scores.getByInterval(from, to);
        assertThat(list.size(), equalTo(201));
    }

    private List<ScoreRecord> getAllRecords()
    {
        return new Select()
            .from(ScoreRecord.class)
            .where("habit = ?", habit.getId())
            .orderBy("timestamp desc")
            .execute();
    }
}
