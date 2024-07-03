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
import androidx.recyclerview.widget.RecyclerView.Adapter
import org.isoron.uhabits.activities.habits.list.MAX_CHECKMARK_COUNT
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
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
import java.util.UUID
import javax.inject.Inject

/**
 * Provides data that backs a [HabitCardListView].
 *
 *
 * The data is fetched and cached by a [HabitCardListCache]. This adapter
 * also holds a list of items that have been selected.
 */
@ActivityScope
class HabitCardListAdapter @Inject constructor(
    private val cache: HabitCardListCache,
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer
) : Adapter<HabitCardViewHolder?>(),
    HabitCardListCache.Listener,
    MidnightTimer.MidnightListener,
    ListHabitsMenuBehavior.Adapter,
    ListHabitsSelectionMenuBehavior.Adapter {
    val observable: ModelObservable = ModelObservable()
    private var listView: HabitCardListView? = null
    val selectedHabits: LinkedList<Habit> = LinkedList()
    val selectedHabitGroups: LinkedList<HabitGroup> = LinkedList()

    override fun atMidnight() {
        cache.refreshAllHabits()
    }

    fun cancelRefresh() {
        cache.cancelTasks()
    }

    fun hasNoHabit(): Boolean {
        return cache.hasNoHabit()
    }

    fun hasNoHabitGroup(): Boolean {
        return cache.hasNoHabitGroup()
    }

    /**
     * Sets all items as not selected.
     */
    override fun clearSelection() {
        selectedHabits.clear()
        selectedHabitGroups.clear()
        notifyDataSetChanged()
        observable.notifyListeners()
    }

    override fun getSelectedHabits(): List<Habit> {
        return ArrayList(selectedHabits)
    }

    override fun getSelectedHabitGroups(): List<HabitGroup> {
        return ArrayList(selectedHabitGroups)
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

    fun getHabit(position: Int): Habit? {
        return cache.getHabitByPosition(position)
    }

    fun getHabitGroup(position: Int): HabitGroup? {
        return cache.getHabitGroupByPosition(position)
    }

    override fun getItemCount(): Int {
        return cache.itemCount
    }

    override fun getItemId(position: Int): Long {
        val uuidString = getItemUUID(position)
        return if (uuidString != null) {
            val formattedUUIDString = formatUUID(uuidString)
            val uuid = UUID.fromString(formattedUUIDString)
            uuid.mostSignificantBits and Long.MAX_VALUE
        } else {
            -1
        }
    }

    fun getItemUUID(position: Int): String? {
        val h = cache.getHabitByPosition(position)
        val hgr = cache.getHabitGroupByPosition(position)
        return if (h != null) {
            h.uuid!!
        } else if (hgr != null) {
            hgr.uuid!!
        } else {
            null
        }
    }

    private fun formatUUID(uuidString: String): String {
        return uuidString.substring(0, 8) + "-" +
            uuidString.substring(8, 12) + "-" +
            uuidString.substring(12, 16) + "-" +
            uuidString.substring(16, 20) + "-" +
            uuidString.substring(20, 32)
    }

    /**
     * Returns whether list of selected items is empty.
     *
     * @return true if selection is empty, false otherwise
     */
    val isSelectionEmpty: Boolean
        get() = selectedHabits.isEmpty() && selectedHabitGroups.isEmpty()
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
        if (habit != null) {
            val score = cache.getScore(habit.uuid!!)
            val checkmarks = cache.getCheckmarks(habit.uuid!!)
            val notes = cache.getNotes(habit.uuid!!)
            val selected = selectedHabits.contains(habit)
            listView!!.bindCardView(holder, habit, score, checkmarks, notes, selected)
        } else {
            val habitGroup = cache.getHabitGroupByPosition(position)
            val score = cache.getScore(habitGroup!!.uuid!!)
            val selected = selectedHabitGroups.contains(habitGroup)
            listView!!.bindGroupCardView(holder, habitGroup, score, selected)
        }
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
        if (viewType == 0) {
            val view = listView!!.createHabitCardView()
            return HabitCardViewHolder(view, null)
        } else {
            val view = listView!!.createHabitGroupCardView()
            return HabitCardViewHolder(null, view)
        }
    }

    // function to override getItemViewType and return the type of the view. The view can either be a HabitCardView or a HabitGroupCardView
    override fun getItemViewType(position: Int): Int {
        return if (position < cache.habitCount) {
            0
        } else {
            1
        }
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
        for (habit in selected) cache.remove(habit.uuid!!)
    }

    override fun performRemoveHabitGroup(selected: List<HabitGroup>) {
        for (hgr in selected) cache.remove(hgr.uuid!!)
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
        val h = cache.getHabitByPosition(position)
        val hgr = cache.getHabitGroupByPosition(position)
        if (h != null) {
            val k = selectedHabits.indexOf(h)
            if (k < 0) selectedHabits.add(h) else selectedHabits.remove(h)
            notifyDataSetChanged()
        } else if (hgr != null) {
            val k = selectedHabitGroups.indexOf(hgr)
            if (k < 0) selectedHabitGroups.add(hgr) else selectedHabitGroups.remove(hgr)
            notifyDataSetChanged()
        }
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
