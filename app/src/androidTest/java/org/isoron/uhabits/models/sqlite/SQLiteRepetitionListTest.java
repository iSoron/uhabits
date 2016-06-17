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

import android.support.annotation.*;
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
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class SQLiteRepetitionListTest extends BaseAndroidTest
{
    private Habit habit;

    private long today;

    private RepetitionList repetitions;

    private long day;

    @Override
    public void setUp()
    {
        super.setUp();

        habit = fixtures.createLongHabit();
        repetitions = habit.getRepetitions();
        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;
    }

    @Test
    public void testAdd()
    {
        RepetitionRecord record = getByTimestamp(today + day);
        assertThat(record, is(nullValue()));

        Repetition rep = new Repetition(today + day);
        habit.getRepetitions().add(rep);

        record = getByTimestamp(today + day);
        assertThat(record, is(not(nullValue())));
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
        assertThat(rep, is(not(nullValue())));
        assertThat(rep.getTimestamp(), equalTo(today));

        rep = repetitions.getByTimestamp(today - 2 * day);
        assertThat(rep, is(nullValue()));
    }

    @Test
    public void testGetOldest()
    {
        Repetition rep = repetitions.getOldest();
        assertThat(rep, is(not(nullValue())));
        assertThat(rep.getTimestamp(), equalTo(today - 120 * day));
    }

    @Test
    public void testGetOldest_withEmptyHabit()
    {
        Habit empty = fixtures.createEmptyHabit();
        Repetition rep = empty.getRepetitions().getOldest();
        assertThat(rep, is(nullValue()));
    }

    @Test
    public void testRemove()
    {
        RepetitionRecord record = getByTimestamp(today);
        assertThat(record, is(not(nullValue())));

        Repetition rep = record.toRepetition();
        repetitions.remove(rep);

        record = getByTimestamp(today);
        assertThat(record, is(nullValue()));
    }

    @Nullable
    private RepetitionRecord getByTimestamp(long timestamp)
    {
        return selectByTimestamp(timestamp).executeSingle();
    }

    @NonNull
    private From selectByTimestamp(long timestamp)
    {
        return new Select()
            .from(RepetitionRecord.class)
            .where("habit = ?", habit.getId())
            .and("timestamp = ?", timestamp);
    }
}
