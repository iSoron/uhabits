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

package org.isoron.uhabits.loaders;

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.BaseTask;

import java.util.HashMap;
import java.util.List;

public class HabitListLoader
{
    public interface Listener
    {
        void onLoadFinished();
    }

    private BaseTask currentFetchTask;
    private int checkmarkCount;

    private Listener listener;
    private Long lastLoadTimestamp;

    public HashMap<Long, Habit> habits;
    public List<Habit> habitsList;
    public HashMap<Long, int[]> checkmarks;
    public HashMap<Long, Integer> scores;

    boolean includeArchived;

    public void setIncludeArchived(boolean includeArchived)
    {
        this.includeArchived = includeArchived;
    }

    public void setCheckmarkCount(int checkmarkCount)
    {
        this.checkmarkCount = checkmarkCount;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public Long getLastLoadTimestamp()
    {
        return lastLoadTimestamp;
    }

    public HabitListLoader()
    {
        habits = new HashMap<>();
        checkmarks = new HashMap<>();
        scores = new HashMap<>();
    }

    public void reorder(int from, int to)
    {
        Habit fromHabit = habitsList.get(from);
        Habit toHabit = habitsList.get(to);

        habitsList.remove(from);
        habitsList.add(to, fromHabit);

        Habit.reorder(fromHabit, toHabit);
    }

    public void updateAllHabits(final boolean updateScoresAndCheckmarks)
    {
        if (currentFetchTask != null) currentFetchTask.cancel(true);

        currentFetchTask = new BaseTask()
        {
            public HashMap<Long, Habit> newHabits;
            public HashMap<Long, int[]> newCheckmarks;
            public HashMap<Long, Integer> newScores;
            public List<Habit> newHabitList;

            @Override
            protected void doInBackground()
            {
                newHabits = new HashMap<>();
                newCheckmarks = new HashMap<>();
                newScores = new HashMap<>();
                newHabitList = Habit.getAll(includeArchived);

                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (checkmarkCount - 1) * DateHelper.millisecondsInOneDay;
                int[] empty = new int[checkmarkCount];

                for(Habit h : newHabitList)
                {
                    Long id = h.getId();

                    newHabits.put(id, h);

                    if(checkmarks.containsKey(id))
                        newCheckmarks.put(id, checkmarks.get(id));
                    else
                        newCheckmarks.put(id, empty);

                    if(scores.containsKey(id))
                        newScores.put(id, scores.get(id));
                    else
                        newScores.put(id, 0);
                }

                commit();

                if(!updateScoresAndCheckmarks) return;

                int current = 0;
                for (Habit h : newHabitList)
                {
                    if (isCancelled()) return;

                    Long id = h.getId();
                    newScores.put(id, h.scores.getTodayValue());
                    newCheckmarks.put(id, h.checkmarks.getValues(dateFrom, dateTo));

                    publishProgress(current++, newHabits.size());
                }
            }

            private void commit()
            {
                habits = newHabits;
                scores = newScores;
                checkmarks = newCheckmarks;
                habitsList = newHabitList;
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                if(listener != null) listener.onLoadFinished();
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if (isCancelled()) return;

                lastLoadTimestamp = DateHelper.getStartOfToday();
                currentFetchTask = null;

                if(listener != null) listener.onLoadFinished();
            }

        };

        currentFetchTask.execute();
    }

    public void updateHabit(final Long id)
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (checkmarkCount - 1) * DateHelper.millisecondsInOneDay;

                Habit h = Habit.get(id);
                if(h == null) return;

                habits.put(id, h);
                scores.put(id, h.scores.getTodayValue());
                checkmarks.put(id, h.checkmarks.getValues(dateFrom, dateTo));
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if(listener != null)
                    listener.onLoadFinished();
            }
        }.execute();
    }
}
