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

package org.isoron.uhabits.models;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

public class StreakListTest extends BaseUnitTest
{
    private Habit habit;

    private StreakList streaks;

    private long day;

    private long today;

    private ModelObservable.Listener listener;

    @Override
    public void setUp()
    {
        super.setUp();
        habit = fixtures.createLongHabit();
        habit.setFrequency(Frequency.DAILY);

        streaks = habit.getStreaks();
        streaks.rebuild();

        listener = mock(ModelObservable.Listener.class);
        streaks.getObservable().addListener(listener);

        today = DateUtils.getStartOfToday();
        day = DateUtils.millisecondsInOneDay;
    }

    @Test
    public void testFindBeginning_withEmptyHistory()
    {
        Habit habit2 = fixtures.createEmptyHabit();
        Long beginning = habit2.getStreaks().findBeginning();
        assertThat(beginning, is(nullValue()));
    }

    @Test
    public void testFindBeginning_withLongHistory()
    {
        streaks.rebuild();
        assertThat(streaks.findBeginning(), equalTo(today - day));

        streaks.invalidateNewerThan(today - 20 * day);
        assertThat(streaks.findBeginning(), equalTo(today - 28 * day));

        streaks.invalidateNewerThan(0);
        assertThat(streaks.findBeginning(), equalTo(today - 120 * day));
    }

    @Test
    public void testGetAll() throws Exception
    {
        List<Streak> all = streaks.getAll();

        assertThat(all.size(), equalTo(22));

        assertThat(all.get(3).getEnd(), equalTo(today - 7 * day));
        assertThat(all.get(3).getStart(), equalTo(today - 10 * day));

        assertThat(all.get(17).getEnd(), equalTo(today - 89 * day));
        assertThat(all.get(17).getStart(), equalTo(today - 91 * day));
    }

    @Test
    public void testGetBest() throws Exception
    {
        List<Streak> best = streaks.getBest(4);
        assertThat(best.size(), equalTo(4));

        assertThat(best.get(0).getLength(), equalTo(4L));
        assertThat(best.get(1).getLength(), equalTo(3L));
        assertThat(best.get(2).getLength(), equalTo(5L));
        assertThat(best.get(3).getLength(), equalTo(6L));

        best = streaks.getBest(2);
        assertThat(best.size(), equalTo(2));

        assertThat(best.get(0).getLength(), equalTo(5L));
        assertThat(best.get(1).getLength(), equalTo(6L));
    }

    @Test
    public void testGetNewestComputed() throws Exception
    {
        Streak s = streaks.getNewestComputed();
        assertThat(s.getEnd(), equalTo(today));
        assertThat(s.getStart(), equalTo(today - day));

        streaks.invalidateNewerThan(today - 8 * day);

        s = streaks.getNewestComputed();
        assertThat(s.getEnd(), equalTo(today - 12 * day));
        assertThat(s.getStart(), equalTo(today - 12 * day));
    }

    @Test
    public void testInvalidateNewer()
    {
        Streak s = streaks.getNewestComputed();
        assertThat(s.getEnd(), equalTo(today));

        streaks.invalidateNewerThan(today - 8 * day);
        verify(listener).onModelChange();

        s = streaks.getNewestComputed();
        assertThat(s.getEnd(), equalTo(today - 12 * day));
    }
}