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

package org.isoron.uhabits.activities.habits.list.model;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;

import java.util.*;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.mockito.Mockito.*;

public class HabitCardListCacheTest extends BaseUnitTest
{
    private HabitCardListCache cache;

    private HabitCardListCache.Listener listener;

    private CommandRunner commandRunner;

    @Override
    public void setUp()
    {
        super.setUp();

        for (int i = 0; i < 10; i++)
        {
            if (i == 3) habitList.add(fixtures.createLongHabit());
            else habitList.add(fixtures.createShortHabit());
        }

        SingleThreadTaskRunner taskRunner = new SingleThreadTaskRunner();
        commandRunner = new CommandRunner(taskRunner);

        cache = new HabitCardListCache(habitList, commandRunner, taskRunner);
        cache.setCheckmarkCount(10);
        cache.refreshAllHabits();
        cache.onAttached();

        listener = mock(HabitCardListCache.Listener.class);
        cache.setListener(listener);
    }

    @Override
    public void tearDown()
    {
        cache.onDetached();
        super.tearDown();
    }

    @Test
    public void testCommandListener_all()
    {
        assertThat(cache.getHabitCount(), equalTo(10));

        Habit h = habitList.getByPosition(0);
        commandRunner.execute(
            new DeleteHabitsCommand(habitList, Collections.singletonList(h)),
            null);

        verify(listener).onItemRemoved(0);
        verify(listener).onRefreshFinished();
        assertThat(cache.getHabitCount(), equalTo(9));
    }

    @Test
    public void testCommandListener_single()
    {
        Habit h2 = habitList.getByPosition(2);
        long today = DateUtils.getStartOfToday();
        commandRunner.execute(new ToggleRepetitionCommand(h2, today),
            h2.getId());

        verify(listener).onItemChanged(2);
        verify(listener).onRefreshFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testGet()
    {
        assertThat(cache.getHabitCount(), equalTo(10));

        Habit h = habitList.getByPosition(3);
        assertNotNull(h.getId());
        int score = h.getScores().getTodayValue();

        assertThat(cache.getHabitByPosition(3), equalTo(h));
        assertThat(cache.getScore(h.getId()), equalTo(score));

        long today = DateUtils.getStartOfToday();
        long day = DateUtils.millisecondsInOneDay;

        int[] actualCheckmarks = cache.getCheckmarks(h.getId());
        int[] expectedCheckmarks =
            h.getCheckmarks().getValues(today - 9 * day, today);

        assertThat(actualCheckmarks, equalTo(expectedCheckmarks));
    }

    @Test
    public void testRemoval()
    {
        removeHabitAt(0);
        removeHabitAt(3);

        cache.refreshAllHabits();
        verify(listener).onItemRemoved(0);
        verify(listener).onItemRemoved(3);
        verify(listener).onRefreshFinished();
        assertThat(cache.getHabitCount(), equalTo(8));
    }

    @Test
    public void testRefreshWithNoChanges()
    {
        cache.refreshAllHabits();
        verify(listener).onRefreshFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testReorder_onCache()
    {
        Habit h2 = cache.getHabitByPosition(2);
        Habit h3 = cache.getHabitByPosition(3);
        Habit h7 = cache.getHabitByPosition(7);

        cache.reorder(2, 7);

        assertThat(cache.getHabitByPosition(2), equalTo(h3));
        assertThat(cache.getHabitByPosition(7), equalTo(h2));
        assertThat(cache.getHabitByPosition(6), equalTo(h7));
        verify(listener).onItemMoved(2, 7);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testReorder_onList()
    {
        Habit h2 = habitList.getByPosition(2);
        Habit h3 = habitList.getByPosition(3);
        Habit h7 = habitList.getByPosition(7);

        assertThat(cache.getHabitByPosition(2), equalTo(h2));
        assertThat(cache.getHabitByPosition(7), equalTo(h7));
        reset(listener);

        habitList.reorder(h2, h7);
        cache.refreshAllHabits();

        assertThat(cache.getHabitByPosition(2), equalTo(h3));
        assertThat(cache.getHabitByPosition(7), equalTo(h2));
        assertThat(cache.getHabitByPosition(6), equalTo(h7));

        verify(listener).onItemMoved(3, 2);
        verify(listener).onItemMoved(4, 3);
        verify(listener).onItemMoved(5, 4);
        verify(listener).onItemMoved(6, 5);
        verify(listener).onItemMoved(7, 6);
        verify(listener).onRefreshFinished();
        verifyNoMoreInteractions(listener);
    }

    protected void removeHabitAt(int position)
    {
        Habit h = habitList.getByPosition(position);
        assertNotNull(h);
        habitList.remove(h);
    }

}