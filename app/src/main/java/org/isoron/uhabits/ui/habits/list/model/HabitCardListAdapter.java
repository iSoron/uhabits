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
import android.support.v7.widget.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.habits.list.views.*;

import java.util.*;

/**
 * Provides data that backs a {@link HabitCardListView}.
 * <p>
 * The data if fetched and cached by a {@link HabitCardListCache}. This adapter
 * also holds a list of items that have been selected.
 */
public class HabitCardListAdapter
    extends RecyclerView.Adapter<HabitCardViewHolder>
    implements HabitCardListCache.Listener
{
    @NonNull
    private ModelObservable observable;

    @Nullable
    private HabitCardListView listView;

    @NonNull
    private final LinkedList<Habit> selected;

    @NonNull
    private final HabitCardListCache cache;

    public HabitCardListAdapter(@NonNull HabitList allHabits,
                                int checkmarkCount)
    {
        this.selected = new LinkedList<>();
        this.observable = new ModelObservable();

        HabitsApplication.getComponent().inject(this);

        cache = new HabitCardListCache(allHabits);
        cache.setListener(this);
        cache.setCheckmarkCount(checkmarkCount);

        setHasStableIds(true);
    }

    public void cancelRefresh()
    {
        cache.cancelTasks();
    }

    /**
     * Sets all items as not selected.
     */
    public void clearSelection()
    {
        selected.clear();
        notifyDataSetChanged();
    }

    /**
     * Returns the item that occupies a certain position on the list
     *
     * @param position position of the item
     * @return the item at given position
     * @throws IndexOutOfBoundsException if position is not valid
     */
    @Deprecated
    @NonNull
    public Habit getItem(int position)
    {
        return cache.getHabitByPosition(position);
    }

    @Override
    public int getItemCount()
    {
        return cache.getHabitCount();
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).getId();
    }

    @NonNull
    public ModelObservable getObservable()
    {
        return observable;
    }

    @NonNull
    public List<Habit> getSelected()
    {
        return new LinkedList<>(selected);
    }

    /**
     * Returns whether list of selected items is empty.
     *
     * @return true if selection is empty, false otherwise
     */
    public boolean isSelectionEmpty()
    {
        return selected.isEmpty();
    }

    /**
     * Notify the adapter that it has been attached to a ListView.
     */
    public void onAttached()
    {
        cache.onAttached();
    }

    @Override
    public void onBindViewHolder(@Nullable HabitCardViewHolder holder,
                                 int position)
    {
        if (holder == null) return;
        if (listView == null) return;

        Habit habit = cache.getHabitByPosition(position);
            int score = cache.getScore(habit.getId());
        int checkmarks[] = cache.getCheckmarks(habit.getId());
        boolean selected = this.selected.contains(habit);

        HabitCardView cardView = (HabitCardView) holder.itemView;
        listView.bindCardView(cardView, habit, score, checkmarks, selected,
            position);
    }

    @Override
    public HabitCardViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType)
    {
        if (listView == null) return null;
        View view = listView.createCardView();
        return new HabitCardViewHolder(view);
    }

    /**
     * Notify the adapter that it has been detached from a ListView.
     */
    public void onDetached()
    {
        cache.onDetached();
    }

    @Override
    public void onItemChanged(int position)
    {
        notifyItemChanged(position);
        observable.notifyListeners();
    }

    @Override
    public void onItemInserted(int position)
    {
        notifyItemInserted(position);
        observable.notifyListeners();
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition)
    {
        notifyItemMoved(fromPosition, toPosition);
        observable.notifyListeners();
    }

    @Override
    public void onItemRemoved(int position)
    {
        notifyItemRemoved(position);
        observable.notifyListeners();
    }

    public void refresh()
    {
        cache.refreshAllHabits();
    }

    /**
     * Changes the order of habits on the adapter.
     * <p>
     * Note that this only has effect on the adapter cache. The database is not
     * modified, and the change is lost when the cache is refreshed. This method
     * is useful for making the ListView more responsive: while we wait for the
     * database operation to finish, the cache can be modified to reflect the
     * changes immediately.
     *
     * @param from the habit that should be moved
     * @param to   the habit that currently occupies the desired position
     */
    public void reorder(int from, int to)
    {
        cache.reorder(from, to);
        notifyItemMoved(from, to);
    }

    public void setFilter(HabitMatcher matcher)
    {
        cache.setFilter(matcher);
    }

    /**
     * Sets the HabitCardListView that this adapter will provide data for.
     * <p>
     * This object will be used to generated new HabitCardViews, upon demand.
     *
     * @param listView the HabitCardListView associated with this adapter
     */
    public void setListView(@Nullable HabitCardListView listView)
    {
        this.listView = listView;
    }

    public void setProgressBar(ProgressBar progressBar)
    {
        cache.setProgressBar(progressBar);
    }

    /**
     * Selects or deselects the item at a given position.
     *
     * @param position position of the item to be toggled
     */
    public void toggleSelection(int position)
    {
        Habit h = getItem(position);
        int k = selected.indexOf(h);
        if (k < 0) selected.add(h);
        else selected.remove(h);
        notifyItemChanged(position);
    }
}
