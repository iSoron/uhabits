/*
 * Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.isoron.uhabits.loaders;

import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.models.Habit;

import java.util.HashMap;

public class HabitListLoader
{
    public interface Listener
    {
        void onLoadFinished();
    }

    private AsyncTask<Void, Integer, Void> currentFetchTask;
    private int checkmarkCount;
    private ProgressBar progressBar;

    private Listener listener;
    private Long lastLoadTimestamp;

    public HashMap<Long, Habit> habits;
    public HashMap<Integer, Habit> positionToHabit;
    public HashMap<Long, int[]> checkmarks;
    public HashMap<Long, Integer> scores;

    public void setProgressBar(ProgressBar progressBar)
    {
        this.progressBar = progressBar;
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
        positionToHabit = new HashMap<>();
        checkmarks = new HashMap<>();
        scores = new HashMap<>();
    }

    private void resetData()
    {
        habits.clear();
        positionToHabit.clear();
        checkmarks.clear();
        scores.clear();
    }

    public void updateAllHabits()
    {
        if (currentFetchTask != null) currentFetchTask.cancel(true);

        currentFetchTask = new AsyncTask<Void, Integer, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                resetData();

                habits = Habit.getAll();

                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (checkmarkCount - 1) * DateHelper.millisecondsInOneDay;
                int[] empty = new int[checkmarkCount];

                for (Habit h : habits.values())
                {
                    scores.put(h.getId(), 0);
                    positionToHabit.put(h.position, h);
                    checkmarks.put(h.getId(), empty);
                }

                int current = 0;
                for (int i = 0; i < habits.size(); i++)
                {
                    if (isCancelled()) return null;

                    Habit h = positionToHabit.get(i);
                    scores.put(h.getId(), h.getScore());
                    checkmarks.put(h.getId(), h.getCheckmarks(dateFrom, dateTo));

                    publishProgress(current++, habits.size());
                }

                return null;
            }

            @Override
            protected void onPreExecute()
            {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                progressBar.setMax(values[1]);
                progressBar.setProgress(values[0]);

                if (lastLoadTimestamp == null)
                {
                    listener.onLoadFinished();
                }
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if (isCancelled()) return;

                progressBar.setVisibility(View.INVISIBLE);
                lastLoadTimestamp = DateHelper.getStartOfToday();
                currentFetchTask = null;
            }

        };

        currentFetchTask.execute();
    }

    public void updateHabit(final Long id)
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                long dateTo = DateHelper.getStartOfDay(DateHelper.getLocalTime());
                long dateFrom = dateTo - (checkmarkCount - 1) * DateHelper.millisecondsInOneDay;

                Habit h = Habit.get(id);
                habits.put(id, h);
                scores.put(id, h.getScore());
                checkmarks.put(id, h.getCheckmarks(dateFrom, dateTo));

                return null;
            }

            @Override
            protected void onPreExecute()
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (getStatus() == Status.RUNNING)
                        {
                            progressBar.setIndeterminate(true);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }, 500);
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                progressBar.setVisibility(View.GONE);

                if(listener != null)
                    listener.onLoadFinished();

            }
        }.execute();
    }
}
