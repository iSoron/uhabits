/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui.screens.habits.list;

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.commands.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.tasks.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import javax.inject.*;

/**
 * A HabitCardListCache fetches and keeps a cache of all the data necessary to
 * render a HabitCardListView.
 * <p>
 * This is needed since performing database lookups during scrolling can make
 * the ListView very slow. It also registers itself as an observer of the
 * models, in order to update itself automatically.
 * <p>
 * Note that this class is singleton-scoped, therefore it is shared among all
 * activities.
 */
@AppScope
public class HabitCardListCache implements CommandRunner.Listener
{
    private int checkmarkCount;

    @Nullable
    private Task currentFetchTask;

    @NonNull
    private Listener listener;

    @NonNull
    private CacheData data;

    @NonNull
    private final HabitList allHabits;

    @NonNull
    private HabitList filteredHabits;

    @NonNull
    private final TaskRunner taskRunner;

    @NonNull
    private final CommandRunner commandRunner;

    @Inject
    public HabitCardListCache(@NonNull HabitList allHabits,
                              @NonNull CommandRunner commandRunner,
                              @NonNull TaskRunner taskRunner)
    {
        if (allHabits == null) throw new NullPointerException();
        if (commandRunner == null) throw new NullPointerException();
        if (taskRunner == null) throw new NullPointerException();

        this.allHabits = allHabits;
        this.commandRunner = commandRunner;
        this.filteredHabits = allHabits;
        this.taskRunner = taskRunner;

        this.listener = new Listener()
        {
        };
        data = new CacheData();
    }

    public synchronized void cancelTasks()
    {
        if (currentFetchTask != null) currentFetchTask.cancel();
    }

    public synchronized int[] getCheckmarks(long habitId)
    {
        return data.checkmarks.get(habitId);
    }

    /**
     * Returns the habits that occupies a certain position on the list.
     *
     * @param position the position of the habit
     * @return the habit at given position or null if position is invalid
     */
    @Nullable
    public synchronized Habit getHabitByPosition(int position)
    {
        if (position < 0 || position >= data.habits.size()) return null;
        return data.habits.get(position);
    }

    public synchronized int getHabitCount()
    {
        return data.habits.size();
    }

    public synchronized HabitList.Order getOrder()
    {
        return filteredHabits.getOrder();
    }

    public synchronized double getScore(long habitId)
    {
        return data.scores.get(habitId);
    }

    public synchronized void onAttached()
    {
        refreshAllHabits();
        commandRunner.addListener(this);
    }

    @Override
    public synchronized void onCommandExecuted(@Nullable Command command,
                                               @Nullable Long refreshKey)
    {
        if (refreshKey == null) refreshAllHabits();
        else refreshHabit(refreshKey);
    }

    public synchronized void onDetached()
    {
        commandRunner.removeListener(this);
    }

    public synchronized void refreshAllHabits()
    {
        if (currentFetchTask != null) currentFetchTask.cancel();
        currentFetchTask = new RefreshTask();
        taskRunner.execute(currentFetchTask);
    }

    public synchronized void refreshHabit(long id)
    {
        taskRunner.execute(new RefreshTask(id));
    }

    public synchronized void remove(long id)
    {
        Habit h = data.id_to_habit.get(id);
        if (h == null) return;

        int position = data.habits.indexOf(h);
        data.habits.remove(position);
        data.id_to_habit.remove(id);
        data.checkmarks.remove(id);
        data.scores.remove(id);

        listener.onItemRemoved(position);
    }

    public synchronized void reorder(int from, int to)
    {
        Habit fromHabit = data.habits.get(from);
        data.habits.remove(from);
        data.habits.add(to, fromHabit);
        listener.onItemMoved(from, to);
    }

    public synchronized void setCheckmarkCount(int checkmarkCount)
    {
        this.checkmarkCount = checkmarkCount;
    }

    public synchronized void setFilter(@NonNull HabitMatcher matcher)
    {
        if (matcher == null) throw new NullPointerException();
        filteredHabits = allHabits.getFiltered(matcher);
    }

    public synchronized void setListener(@NonNull Listener listener)
    {
        if (listener == null) throw new NullPointerException();
        this.listener = listener;
    }

    public synchronized void setOrder(@NonNull HabitList.Order order)
    {
        if (order == null) throw new NullPointerException();
        allHabits.setOrder(order);
        filteredHabits.setOrder(order);
        refreshAllHabits();
    }

    /**
     * Interface definition for a callback to be invoked when the data on the
     * cache has been modified.
     */
    public interface Listener
    {
        default void onItemChanged(int position)
        {
        }

        default void onItemInserted(int position)
        {
        }

        default void onItemMoved(int oldPosition, int newPosition)
        {
        }

        default void onItemRemoved(int position)
        {
        }

        default void onRefreshFinished()
        {
        }
    }

    private class CacheData
    {
        @NonNull
        public final HashMap<Long, Habit> id_to_habit;

        @NonNull
        public final List<Habit> habits;

        @NonNull
        public final HashMap<Long, int[]> checkmarks;

        @NonNull
        public final HashMap<Long, Double> scores;

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

        public synchronized void copyCheckmarksFrom(@NonNull CacheData oldData)
        {
            if (oldData == null) throw new NullPointerException();

            int[] empty = new int[checkmarkCount];

            for (Long id : id_to_habit.keySet())
            {
                if (oldData.checkmarks.containsKey(id))
                    checkmarks.put(id, oldData.checkmarks.get(id));
                else checkmarks.put(id, empty);
            }
        }

        public synchronized void copyScoresFrom(@NonNull CacheData oldData)
        {
            if (oldData == null) throw new NullPointerException();

            for (Long id : id_to_habit.keySet())
            {
                if (oldData.scores.containsKey(id))
                    scores.put(id, oldData.scores.get(id));
                else scores.put(id, 0.0);
            }
        }

        public synchronized void fetchHabits()
        {
            for (Habit h : filteredHabits)
            {
                if (h.getId() == null) continue;
                habits.add(h);
                id_to_habit.put(h.getId(), h);
            }
        }
    }

    private class RefreshTask implements Task
    {
        @NonNull
        private final CacheData newData;

        @Nullable
        private final Long targetId;

        private boolean isCancelled;

        @Nullable
        private TaskRunner runner;

        public RefreshTask()
        {
            newData = new CacheData();
            targetId = null;
            isCancelled = false;
        }

        public RefreshTask(long targetId)
        {
            newData = new CacheData();
            this.targetId = targetId;
        }

        @Override
        public synchronized void cancel()
        {
            isCancelled = true;
        }

        @Override
        public synchronized void doInBackground()
        {
            newData.fetchHabits();
            newData.copyScoresFrom(data);
            newData.copyCheckmarksFrom(data);

            Timestamp dateTo = DateUtils.getTodayWithOffset();
            Timestamp dateFrom = dateTo.minus(checkmarkCount - 1);

            if (runner != null) runner.publishProgress(this, -1);

            for (int position = 0; position < newData.habits.size(); position++)
            {
                if (isCancelled) return;

                Habit habit = newData.habits.get(position);
                Long id = habit.getId();
                if (targetId != null && !targetId.equals(id)) continue;

                newData.scores.put(id, habit.getScores().getTodayValue());
                newData.checkmarks.put(
                        id,
                        habit.getCheckmarks().getValues(dateFrom, dateTo));

                runner.publishProgress(this, position);
            }
        }

        @Override
        public synchronized void onAttached(@NonNull TaskRunner runner)
        {
            if (runner == null) throw new NullPointerException();
            this.runner = runner;
        }

        @Override
        public synchronized void onPostExecute()
        {
            currentFetchTask = null;
            listener.onRefreshFinished();
        }

        @Override
        public synchronized void onProgressUpdate(int currentPosition)
        {
            if (currentPosition < 0) processRemovedHabits();
            else processPosition(currentPosition);
        }

        private synchronized void performInsert(Habit habit, int position)
        {
            Long id = habit.getId();
            data.habits.add(position, habit);
            data.id_to_habit.put(id, habit);
            data.scores.put(id, newData.scores.get(id));
            data.checkmarks.put(id, newData.checkmarks.get(id));
            listener.onItemInserted(position);
        }

        private synchronized void performMove(@NonNull Habit habit,
                                              int fromPosition,
                                              int toPosition)
        {
            if(habit == null) throw new NullPointerException();
            data.habits.remove(fromPosition);
            data.habits.add(toPosition, habit);
            listener.onItemMoved(fromPosition, toPosition);
        }

        private synchronized void performUpdate(long id, int position)
        {
            double oldScore = data.scores.get(id);
            int[] oldCheckmarks = data.checkmarks.get(id);

            double newScore = newData.scores.get(id);
            int[] newCheckmarks = newData.checkmarks.get(id);

            boolean unchanged = true;
            if (oldScore != newScore) unchanged = false;
            if (!Arrays.equals(oldCheckmarks, newCheckmarks)) unchanged = false;
            if (unchanged) return;

            data.scores.put(id, newScore);
            data.checkmarks.put(id, newCheckmarks);
            listener.onItemChanged(position);
        }

        private synchronized void processPosition(int currentPosition)
        {
            Habit habit = newData.habits.get(currentPosition);
            Long id = habit.getId();

            int prevPosition = data.habits.indexOf(habit);

            if (prevPosition < 0)
            {
                performInsert(habit, currentPosition);
            }
            else
            {
                if (prevPosition != currentPosition)
                    performMove(habit, prevPosition, currentPosition);

                performUpdate(id, currentPosition);
            }
        }

        private synchronized void processRemovedHabits()
        {
            Set<Long> before = data.id_to_habit.keySet();
            Set<Long> after = newData.id_to_habit.keySet();

            Set<Long> removed = new TreeSet<>(before);
            removed.removeAll(after);

            for (Long id : removed) remove(id);
        }
    }
}
