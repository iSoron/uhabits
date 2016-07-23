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

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

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
    private int checkmarkCount;

    private Task currentFetchTask;

    @Nullable
    private Listener listener;

    @NonNull
    private CacheData data;

    @NonNull
    private HabitList allHabits;

    @NonNull
    private HabitList filteredHabits;

    private final TaskRunner taskRunner;

    private final CommandRunner commandRunner;

    public HabitCardListCache(@NonNull HabitList allHabits)
    {
        this.allHabits = allHabits;
        this.filteredHabits = allHabits;
        data = new CacheData();

        BaseComponent component = HabitsApplication.getComponent();
        commandRunner = component.getCommandRunner();
        taskRunner = component.getTaskRunner();
    }

    public void cancelTasks()
    {
        if (currentFetchTask != null) currentFetchTask.cancel();
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
        return data.habits.get(position);
    }

    public int getHabitCount()
    {
        return data.habits.size();
    }

    public int getScore(long habitId)
    {
        return data.scores.get(habitId);
    }

    public void onAttached()
    {
        refreshAllHabits();
        commandRunner.addListener(this);
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        if (refreshKey == null) refreshAllHabits();
        else refreshHabit(refreshKey);
    }

    public void onDetached()
    {
        commandRunner.removeListener(this);
    }

    public void refreshAllHabits()
    {
        if (currentFetchTask != null) currentFetchTask.cancel();
        currentFetchTask = new RefreshTask();
        taskRunner.execute(currentFetchTask);
    }

    public void refreshHabit(long id)
    {
        taskRunner.execute(new RefreshTask(id));
    }

    public void remove(@NonNull Long id)
    {
        Habit h = data.id_to_habit.get(id);
        if (h == null) return;

        int position = data.habits.indexOf(h);
        data.habits.remove(position);
        data.id_to_habit.remove(id);
        data.checkmarks.remove(id);
        data.scores.remove(id);

        if (listener != null) listener.onItemRemoved(position);
    }

    public void reorder(int from, int to)
    {
        Habit fromHabit = data.habits.get(from);
        data.habits.remove(from);
        data.habits.add(to, fromHabit);
        if (listener != null) listener.onItemMoved(from, to);
    }

    public void setCheckmarkCount(int checkmarkCount)
    {
        this.checkmarkCount = checkmarkCount;
    }

    public void setFilter(HabitMatcher matcher)
    {
        filteredHabits = allHabits.getFiltered(matcher);
    }

    public void setListener(@Nullable Listener listener)
    {
        this.listener = listener;
    }

    public void setProgressBar(@NonNull ProgressBar progressBar)
    {

    }

    /**
     * Interface definition for a callback to be invoked when the data on the
     * cache has been modified.
     */
    public interface Listener
    {
        void onItemChanged(int position);

        void onItemInserted(int position);

        void onItemMoved(int oldPosition, int newPosition);

        void onItemRemoved(int position);
    }

    private class CacheData
    {
        @NonNull
        public HashMap<Long, Habit> id_to_habit;

        @NonNull
        public List<Habit> habits;

        @NonNull
        public HashMap<Long, int[]> checkmarks;

        @NonNull
        public HashMap<Long, Integer> scores;

        /**
         * Creates a new CacheData without any content.
         */
        public CacheData()
        {
            id_to_habit = new HashMap<>();
            habits = new LinkedList<>();
            checkmarks = new HashMap<>();
            scores = new HashMap<>();
        }

        public void copyCheckmarksFrom(@NonNull CacheData oldData)
        {
            int[] empty = new int[checkmarkCount];

            for (Long id : id_to_habit.keySet())
            {
                if (oldData.checkmarks.containsKey(id))
                    checkmarks.put(id, oldData.checkmarks.get(id));
                else checkmarks.put(id, empty);
            }
        }

        public void copyScoresFrom(@NonNull CacheData oldData)
        {
            for (Long id : id_to_habit.keySet())
            {
                if (oldData.scores.containsKey(id))
                    scores.put(id, oldData.scores.get(id));
                else scores.put(id, 0);
            }
        }

        public void fetchHabits()
        {
            for (Habit h : filteredHabits)
            {
                habits.add(h);
                id_to_habit.put(h.getId(), h);
            }
        }
    }

    private class RefreshTask implements Task
    {
        @NonNull
        private CacheData newData;

        @Nullable
        private Long targetId;

        private boolean isCancelled;

        private TaskRunner runner;

        public RefreshTask()
        {
            newData = new CacheData();
            targetId = null;
            isCancelled = false;
        }

        public RefreshTask(Long targetId)
        {
            newData = new CacheData();
            this.targetId = targetId;
        }

        @Override
        public void cancel()
        {
            isCancelled = true;
        }

        @Override
        public void doInBackground()
        {
            newData.fetchHabits();
            newData.copyScoresFrom(data);
            newData.copyCheckmarksFrom(data);

            long dateTo = DateUtils.getStartOfDay(DateUtils.getLocalTime());
            long day = DateUtils.millisecondsInOneDay;
            long dateFrom = dateTo - (checkmarkCount - 1) * day;

            runner.setCurrentProgress(this, -1);

            for (int position = 0; position < newData.habits.size(); position++)
            {
                if (isCancelled) return;

                Habit habit = newData.habits.get(position);
                Long id = habit.getId();
                if (targetId != null && !targetId.equals(id)) continue;

                newData.scores.put(id, habit.getScores().getTodayValue());
                newData.checkmarks.put(id,
                    habit.getCheckmarks().getValues(dateFrom, dateTo));

                runner.setCurrentProgress(this, position);
            }
        }

        @Override
        public void onAttached(@NonNull TaskRunner runner)
        {
            this.runner = runner;
        }

        @Override
        public void onPostExecute()
        {
            currentFetchTask = null;
        }

        @Override
        public void onProgressUpdate(int currentPosition)
        {
            if (currentPosition < 0) processRemovedHabits();
            else processPosition(currentPosition);
        }

        private void performInsert(Habit habit, int position)
        {
            Long id = habit.getId();
            data.habits.add(position, habit);
            data.id_to_habit.put(id, habit);
            data.scores.put(id, newData.scores.get(id));
            data.checkmarks.put(id, newData.checkmarks.get(id));
            if (listener != null) listener.onItemInserted(position);
        }

        private void performMove(Habit habit, int fromPosition, int toPosition)
        {
            data.habits.remove(fromPosition);
            data.habits.add(toPosition, habit);
            if (listener != null)
                listener.onItemMoved(fromPosition, toPosition);
        }

        private void performUpdate(Long id, int position)
        {
            data.scores.put(id, newData.scores.get(id));
            data.checkmarks.put(id, newData.checkmarks.get(id));
            if (listener != null) listener.onItemChanged(position);
        }

        private void processPosition(int currentPosition)
        {
            Habit habit = newData.habits.get(currentPosition);
            Long id = habit.getId();

            int prevPosition = data.habits.indexOf(habit);

            if (prevPosition < 0) performInsert(habit, currentPosition);
            else if (prevPosition == currentPosition)
                performUpdate(id, currentPosition);
            else performMove(habit, prevPosition, currentPosition);
        }

        private void processRemovedHabits()
        {
            Set<Long> before = data.id_to_habit.keySet();
            Set<Long> after = newData.id_to_habit.keySet();

            Set<Long> removed = new TreeSet<>(before);
            removed.removeAll(after);

            for (Long id : removed) remove(id);
        }
    }
}
