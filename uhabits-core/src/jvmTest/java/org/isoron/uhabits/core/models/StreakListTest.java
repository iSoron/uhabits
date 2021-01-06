/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.core.models;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.utils.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.mockito.Mockito.*;

public class StreakListTest extends BaseUnitTest
{
    private Habit habit;

    private StreakList streaks;

    private Timestamp today;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        habit = fixtures.createLongHabit();
        habit.setFrequency(Frequency.DAILY);
        habit.recompute();

        streaks = habit.getStreaks();
        today = DateUtils.getToday();
    }

    @Test
    public void testGetBest() throws Exception
    {
        List<Streak> best = streaks.getBest(4);
        assertThat(best.size(), equalTo(4));
        assertThat(best.get(0).getLength(), equalTo(4));
        assertThat(best.get(1).getLength(), equalTo(3));
        assertThat(best.get(2).getLength(), equalTo(5));
        assertThat(best.get(3).getLength(), equalTo(6));

        best = streaks.getBest(2);
        assertThat(best.size(), equalTo(2));
        assertThat(best.get(0).getLength(), equalTo(5));
        assertThat(best.get(1).getLength(), equalTo(6));
    }

    @Test
    public void testGetBest_withUnknowns()
    {
        habit.getOriginalEntries().clear();
        habit.getOriginalEntries().add(new Entry(today, Entry.YES_MANUAL));
        habit.getOriginalEntries().add(new Entry(today.minus(5), Entry.NO));
        habit.recompute();

        List<Streak> best = streaks.getBest(5);
        assertThat(best.size(), equalTo(1));
        assertThat(best.get(0).getLength(), equalTo(1));
    }
}