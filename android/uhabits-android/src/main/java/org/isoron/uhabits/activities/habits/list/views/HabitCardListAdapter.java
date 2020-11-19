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

package org.isoron.uhabits.activities.habits.list.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.*;

import androidx.recyclerview.widget.RecyclerView;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.activities.habits.list.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.screens.habits.list.*;
import org.isoron.uhabits.core.utils.*;

import java.util.*;

import javax.inject.*;

/**
 * Provides data that backs a {@link HabitCardListView}.
 * <p>
 * The data if fetched and cached by a {@link HabitCardListCache}. This adapter
 * also holds a list of items that have been selected.
 */
@ActivityScope
public class HabitCardListAdapter
    extends RecyclerView.Adapter<HabitCardViewHolder> implements
                                                      HabitCardListCache.Listener,
                                                      MidnightTimer.MidnightListener,
                                                      ListHabitsMenuBehavior.Adapter,
                                                      ListHabitsSelectionMenuBehavior.Adapter
{
    @NonNull
    private ModelObservable observable;

    @Nullable
    private HabitCardListView listView;

    @NonNull
    private final LinkedList<Habit> selected;

    @NonNull
    private final HabitCardListCache cache;

    @NonNull
    private Preferences preferences;

    private final MidnightTimer midnightTimer;

    @Inject
    public HabitCardListAdapter(@NonNull HabitCardListCache cache,
                                @NonNull Preferences preferences,
                                @NonNull MidnightTimer midnightTimer)
    {
        this.preferences = preferences;
        this.selected = new LinkedList<>();
        this.observable = new ModelObservable();
        this.cache = cache;

        this.midnightTimer = midnightTimer;

        cache.setListener(this);
        cache.setCheckmarkCount(
            ListHabitsRootViewKt.MAX_CHECKMARK_COUNT);
        cache.setSecondaryOrder(preferences.getDefaultSecondaryOrder());
        cache.setPrimaryOrder(preferences.getDefaultPrimaryOrder());

        setHasStableIds(true);
    }

    @Override
    public void atMidnight()
    {
        cache.refreshAllHabits();
    }

    public void cancelRefresh()
    {
        cache.cancelTasks();
    }

    /**
     * Sets all items as not selected.
     */
    @Override
    public void clearSelection()
    {
        selected.clear();
        notifyDataSetChanged();
        observable.notifyListeners();
    }

    /**
     * Returns the item that occupies a certain position on the list
     *
     * @param position position of the item
     * @return the item at given position or null if position is invalid
     */
    @Deprecated
    @Nullable
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

    @Override
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

    public boolean isSortable()
    {
        return cache.getPrimaryOrder() == HabitList.Order.BY_POSITION;
    }

    /**
     * Notify the adapter that it has been attached to a ListView.
     */
    public void onAttached()
    {
        cache.onAttached();
        midnightTimer.addListener(this);
    }

    @Override
    public void onBindViewHolder(@Nullable HabitCardViewHolder holder,
                                 int position)
    {
        if (holder == null) return;
        if (listView == null) return;

        Habit habit = cache.getHabitByPosition(position);
        double score = cache.getScore(habit.getId());
        int checkmarks[] = cache.getCheckmarks(habit.getId());
        boolean selected = this.selected.contains(habit);

        listView.bindCardView(holder, habit, score, checkmarks, selected);
    }

    @Override
    public void onViewAttachedToWindow(@Nullable HabitCardViewHolder holder)
    {
        if (listView == null) return;
        listView.attachCardView(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@Nullable HabitCardViewHolder holder)
    {
        if (listView == null) return;
        listView.detachCardView(holder);
    }

    @Override
    public HabitCardViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType)
    {
        if (listView == null) return null;
        View view = listView.createHabitCardView();
        return new HabitCardViewHolder(view);
    }

    /**
     * Notify the adapter that it has been detached from a ListView.
     */
    public void onDetached()
    {
        cache.onDetached();
        midnightTimer.removeListener(this);
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

    @Override
    public void onRefreshFinished()
    {
        observable.notifyListeners();
    }

    /**
     * Removes a list of habits from the adapter.
     * <p>
     * Note that this only has effect on the adapter cache. The database is not
     * modified, and the change is lost when the cache is refreshed. This method
     * is useful for making the ListView more responsive: while we wait for the
     * database operation to finish, the cache can be modified to reflect the
     * changes immediately.
     *
     * @param habits list of habits to be removed
     */
    @Override
    public void performRemove(List<Habit> habits)
    {
        for (Habit h : habits)
            cache.remove(h.getId());
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
    public void performReorder(int from, int to)
    {
        cache.reorder(from, to);
    }

    @Override
    public void refresh()
    {
        cache.refreshAllHabits();
    }

    @Override
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

    @Override
    public void setPrimaryOrder(HabitList.Order order)
    {
        cache.setPrimaryOrder(order);
        preferences.setDefaultPrimaryOrder(order);
    }

    @Override
    public void setSecondaryOrder(HabitList.Order order) {
        cache.setSecondaryOrder(order);
        preferences.setDefaultSecondaryOrder(order);
    }

    @Override
    public HabitList.Order getPrimaryOrder()
    {
        return cache.getPrimaryOrder();
    }

    /**
     * Selects or deselects the item at a given position.
     *
     * @param position position of the item to be toggled
     */
    public void toggleSelection(int position)
    {
        Habit h = getItem(position);
        if (h == null) return;

        int k = selected.indexOf(h);
        if (k < 0) selected.add(h);
        else selected.remove(h);
        notifyDataSetChanged();
    }
}
