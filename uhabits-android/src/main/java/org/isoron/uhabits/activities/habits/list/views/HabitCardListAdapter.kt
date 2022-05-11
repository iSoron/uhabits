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
package org.isoron.uhabits.activities.habits.list.views

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.isoron.uhabits.activities.habits.list.MAX_CHECKMARK_COUNT
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardListCache
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsMenuBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import org.isoron.uhabits.core.utils.MidnightTimer
import org.isoron.uhabits.inject.ActivityScope
import java.util.LinkedList
import javax.inject.Inject

/**
 * Provides data that backs a [HabitCardListView].
 *
 *
 * The data if fetched and cached by a [HabitCardListCache]. This adapter
 * also holds a list of items that have been selected.
 */
@ActivityScope
class HabitCardListAdapter @Inject constructor(
    private val cache: HabitCardListCache,
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer
) : RecyclerView.Adapter<HabitCardViewHolder?>(),
    HabitCardListCache.Listener,
    MidnightTimer.MidnightListener,
    ListHabitsMenuBehavior.Adapter,
    ListHabitsSelectionMenuBehavior.Adapter {
    val observable: ModelObservable = ModelObservable()
    private var listView: HabitCardListView? = null
    val selected: LinkedList<Habit> = LinkedList()
    override fun atMidnight() {
        cache.refreshAllHabits()
    }

    fun cancelRefresh() {
        cache.cancelTasks()
    }

    fun hasNoHabit(): Boolean {
        return cache.hasNoHabit()
    }

    /**
     * Sets all items as not selected.
     */
    override fun clearSelection() {
        selected.clear()
        notifyDataSetChanged()
        observable.notifyListeners()
    }

    override fun getSelected(): List<Habit> {
        return ArrayList(selected)
    }

    /**
     * Returns the item that occupies a certain position on the list
     *
     * @param position position of the item
     * @return the item at given position or null if position is invalid
     */
    @Deprecated("")
    fun getItem(position: Int): Habit? {
        return cache.getHabitByPosition(position)
    }

    override fun getItemCount(): Int {
        return cache.habitCount
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)!!.id!!
    }

    /**
     * Returns whether list of selected items is empty.
     *
     * @return true if selection is empty, false otherwise
     */
    val isSelectionEmpty: Boolean
        get() = selected.isEmpty()
    val isSortable: Boolean
        get() = cache.primaryOrder == HabitList.Order.BY_POSITION

    /**
     * Notify the adapter that it has been attached to a ListView.
     */
    fun onAttached() {
        cache.onAttached()
        midnightTimer.addListener(this)
    }

    override fun onBindViewHolder(
        holder: HabitCardViewHolder,
        position: Int
    ) {
        if (listView == null) return
        val habit = cache.getHabitByPosition(position)
        val score = cache.getScore(habit!!.id!!)
        val checkmarks = cache.getCheckmarks(habit.id!!)
        val notes = cache.getNotes(habit.id!!)
        val selected = selected.contains(habit)
        listView!!.bindCardView(holder, habit, score, checkmarks, notes, selected)
    }

    override fun onViewAttachedToWindow(holder: HabitCardViewHolder) {
        listView!!.attachCardView(holder)
    }

    override fun onViewDetachedFromWindow(holder: HabitCardViewHolder) {
        listView!!.detachCardView(holder)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitCardViewHolder {
        val view = listView!!.createHabitCardView()
        return HabitCardViewHolder(view)
    }

    /**
     * Notify the adapter that it has been detached from a ListView.
     */
    fun onDetached() {
        cache.onDetached()
        midnightTimer.removeListener(this)
    }

    override fun onItemChanged(position: Int) {
        notifyItemChanged(position)
        observable.notifyListeners()
    }

    override fun onItemInserted(position: Int) {
        notifyItemInserted(position)
        observable.notifyListeners()
    }

    override fun onItemMoved(oldPosition: Int, newPosition: Int) {
        notifyItemMoved(oldPosition, newPosition)
        observable.notifyListeners()
    }

    override fun onItemRemoved(position: Int) {
        notifyItemRemoved(position)
        observable.notifyListeners()
    }

    override fun onRefreshFinished() {
        observable.notifyListeners()
    }

    /**
     * Removes a list of habits from the adapter.
     *
     *
     * Note that this only has effect on the adapter cache. The database is not
     * modified, and the change is lost when the cache is refreshed. This method
     * is useful for making the ListView more responsive: while we wait for the
     * database operation to finish, the cache can be modified to reflect the
     * changes immediately.
     *
     * @param selected list of habits to be removed
     */
    override fun performRemove(selected: List<Habit>) {
        for (habit in selected) cache.remove(habit.id!!)
    }

    /**
     * Changes the order of habits on the adapter.
     *
     *
     * Note that this only has effect on the adapter cache. The database is not
     * modified, and the change is lost when the cache is refreshed. This method
     * is useful for making the ListView more responsive: while we wait for the
     * database operation to finish, the cache can be modified to reflect the
     * changes immediately.
     *
     * @param from the habit that should be moved
     * @param to   the habit that currently occupies the desired position
     */
    fun performReorder(from: Int, to: Int) {
        cache.reorder(from, to)
    }

    override fun refresh() {
        cache.refreshAllHabits()
    }

    override fun setFilter(matcher: HabitMatcher) {
        cache.setFilter(matcher)
    }

    /**
     * Sets the HabitCardListView that this adapter will provide data for.
     *
     *
     * This object will be used to generated new HabitCardViews, upon demand.
     *
     * @param listView the HabitCardListView associated with this adapter
     */
    fun setListView(listView: HabitCardListView?) {
        this.listView = listView
    }

    override var primaryOrder: HabitList.Order
        get() = cache.primaryOrder
        set(value) {
            cache.primaryOrder = value
            preferences.defaultPrimaryOrder = value
        }

    override var secondaryOrder: HabitList.Order
        get() = cache.secondaryOrder
        set(value) {
            cache.secondaryOrder = value
            preferences.defaultSecondaryOrder = value
        }

    /**
     * Selects or deselects the item at a given position.
     *
     * @param position position of the item to be toggled
     */
    fun toggleSelection(position: Int) {
        val h = getItem(position) ?: return
        val k = selected.indexOf(h)
        if (k < 0) selected.add(h) else selected.remove(h)
        notifyDataSetChanged()
    }

    init {
        cache.setListener(this)
        cache.setCheckmarkCount(
            MAX_CHECKMARK_COUNT
        )
        cache.secondaryOrder = preferences.defaultSecondaryOrder
        cache.primaryOrder = preferences.defaultPrimaryOrder
        setHasStableIds(true)
    }
}
