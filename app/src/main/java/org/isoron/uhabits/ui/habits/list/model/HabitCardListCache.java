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

package org.isoron.uhabits.ui.habits.list.model;

import android.support.annotation.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

import javax.inject.*;

/**
 * A HabitCardListCache fetches and keeps a cache of all the data necessary to
 * render a HabitCardListView.
 * <p>
 * This is needed since performing database lookups during scrolling can make
 * the ListView very slow. It also registers itself as an observer of the
 * models, in order to update itself automatically.
 */
public class HabitCardListCache implements CommandRunner.Listener
{
    boolean includeArchived;

    private int checkmarkCount;

    private BaseTask currentFetchTask;

    @Nullable
    private Listener listener;

    @Nullable
    private Long lastLoadTimestamp;

    @NonNull
    private CacheData data;

    @Inject
    CommandRunner commandRunner;

    @Inject
    HabitList allHabits;

    public HabitCardListCache()
    {
        data = new CacheData();
        HabitsApplication.getComponent().inject(this);
    }

    public int[] getCheckmarks(long habitId)
    {
        return data.checkmarks.get(habitId);
    }

    /**
     * Returns the habits that occupies a certain position on the list.
     *
     * @param position the position of the habit
     * @return the habit at given position
     * @throws IndexOutOfBoundsException if position is not valid
     */
    @NonNull
    public Habit getHabitByPosition(int position)
    {
        return data.habitsList.get(position);
    }

    public int getHabitCount()
    {
        return data.habits.size();
    }

    @Nullable
    public Long getLastLoadTimestamp()
    {
        return lastLoadTimestamp;
    }

    public int getScore(long habitId)
    {
        return data.scores.get(habitId);
    }

    public boolean getIncludeArchived()
    {
        return includeArchived;
    }

    public void onAttached()
    {
        refreshAllHabits(true);
        if (lastLoadTimestamp == null) refreshAllHabits(true);
        commandRunner.addListener(this);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if (refreshKey == null) refreshAllHabits(true);
        else refreshHabit(refreshKey);
    }

    public void onDetached()
    {
        commandRunner.removeListener(this);
    }

    public void refreshAllHabits(final boolean refreshScoresAndCheckmarks)
    {
        Log.d("HabitCardListCache", "Refreshing all habits");
        if (currentFetchTask != null) currentFetchTask.cancel(true);
        currentFetchTask = new RefreshAllHabitsTask(refreshScoresAndCheckmarks);
        currentFetchTask.execute();
    }

    public void refreshHabit(final Long id)
    {
        new RefreshHabitTask(id).execute();
    }

    public void reorder(int from, int to)
    {
        Habit fromHabit = data.habitsList.get(from);
        Habit toHabit = data.habitsList.get(to);

        data.habitsList.remove(from);
        data.habitsList.add(to, fromHabit);

        allHabits.reorder(fromHabit, toHabit);
    }

    public void setCheckmarkCount(int checkmarkCount)
    {
        this.checkmarkCount = checkmarkCount;
    }

    public void setIncludeArchived(boolean includeArchived)
    {
        this.includeArchived = includeArchived;
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the data on the
     * cache has been modified.
     */
    public interface Listener
    {
        /**
         * Called when the data on the cache has been modified.
         */
        void onCacheRefresh();
    }

    private class CacheData
    {
        @NonNull
        public HashMap<Long, Habit> habits;

        @NonNull
        public List<Habit> habitsList;

        @NonNull
        public HashMap<Long, int[]> checkmarks;

        @NonNull
        public HashMap<Long, Integer> scores;

        /**
         * Creates a new CacheData without any content.
         */
        public CacheData()
        {
            habits = new HashMap<>();
            habitsList = new LinkedList<>();
            checkmarks = new HashMap<>();
            scores = new HashMap<>();
        }

        public void copyCheckmarksFrom(@NonNull CacheData oldData)
        {
            int[] empty = new int[checkmarkCount];

            for (Long id : habits.keySet())
            {
                if (oldData.checkmarks.containsKey(id))
                    checkmarks.put(id, oldData.checkmarks.get(id));
                else checkmarks.put(id, empty);
            }
        }

        public void copyScoresFrom(@NonNull CacheData oldData)
        {
            for (Long id : habits.keySet())
            {
                if (oldData.scores.containsKey(id))
                    scores.put(id, oldData.scores.get(id));
                else scores.put(id, 0);
            }
        }

        public void fetchHabits()
        {
            habitsList = allHabits.getAll(includeArchived);
            for (Habit h : habitsList)
                habits.put(h.getId(), h);
        }
    }

    private class RefreshAllHabitsTask extends BaseTask
    {
        @NonNull
        private CacheData newData;

        private final boolean refreshScoresAndCheckmarks;

        public RefreshAllHabitsTask(boolean refreshScoresAndCheckmarks)
        {
            this.refreshScoresAndCheckmarks = refreshScoresAndCheckmarks;
            newData = new CacheData();
        }

        private void commit()
        {
            data = newData;
        }

        @Override
        protected void doInBackground()
        {
            newData.fetchHabits();
            newData.copyScoresFrom(data);
            newData.copyCheckmarksFrom(data);

            commit();

            if (!refreshScoresAndCheckmarks) return;

            long dateTo = DateUtils.getStartOfDay(DateUtils.getLocalTime());
            long dateFrom =
                dateTo - (checkmarkCount - 1) * DateUtils.millisecondsInOneDay;

            int current = 0;
            for (Habit h : newData.habitsList)
            {
                if (isCancelled()) return;

                Long id = h.getId();
                newData.scores.put(id, h.getScores().getTodayValue());
                newData.checkmarks.put(id,
                    h.getCheckmarks().getValues(dateFrom, dateTo));

                publishProgress(current++, newData.habits.size());
            }
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (isCancelled()) return;

            lastLoadTimestamp = DateUtils.getStartOfToday();
            currentFetchTask = null;

            if (listener != null) listener.onCacheRefresh();
            super.onPostExecute(null);
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            if (listener != null) listener.onCacheRefresh();
        }

    }

    private class RefreshHabitTask extends BaseTask
    {
        private final Long id;

        public RefreshHabitTask(Long id)
        {
            this.id = id;
        }

        @Override
        protected void doInBackground()
        {
            long dateTo = DateUtils.getStartOfDay(DateUtils.getLocalTime());
            long dateFrom =
                dateTo - (checkmarkCount - 1) * DateUtils.millisecondsInOneDay;

            Habit h = allHabits.getById(id);
            if (h == null) return;

            data.habits.put(id, h);
            data.scores.put(id, h.getScores().getTodayValue());
            data.checkmarks.put(id, h.getCheckmarks().getValues(dateFrom, dateTo));
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (listener != null) listener.onCacheRefresh();
            super.onPostExecute(null);
        }
    }
}
