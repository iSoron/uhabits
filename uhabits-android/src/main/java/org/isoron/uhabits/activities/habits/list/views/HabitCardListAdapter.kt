/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitList.Order
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardState
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsMenuBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsState
import org.isoron.uhabits.inject.ActivityScope
import java.util.LinkedList

/**
 * Provides data that backs a [HabitCardListView].
 *
 *
 * The adapter holds the current [ListHabitsState] and renders it via [setState];
 * the Screen builds the state with the pure [ListHabitsPresenter] and pushes it
 * in. The menu behavior and the selection behavior talk to this adapter for the
 * list's data (item at position, item count, is it empty, is it sortable, apply
 * filter/order, remove selected).
 *
 *
 * Selection is tracked by stable identity (habit id), not by position, so a full
 * rebuild after a filter change, delete, or sort change does not scramble which
 * items are selected.
 */
@Inject
@ActivityScope
class HabitCardListAdapter(
    private val allHabits: HabitList,
    private val preferences: Preferences
) : RecyclerView.Adapter<HabitCardViewHolder?>(),
    ListHabitsMenuBehavior.Adapter,
    ListHabitsSelectionMenuBehavior.Adapter {
    val observable: ModelObservable = ModelObservable()
    private var listView: HabitCardListView? = null

    val selected = LinkedList<Long>()
    var filter: HabitMatcher? = null
        private set
    private var state: ListHabitsState? = null
    private var currentPrimaryOrder: Order
    private var currentSecondaryOrder: Order

    private var items: MutableList<HabitCardState> = mutableListOf()

    private var isDragging = false

    private var draggedId: Long? = null

    private var dragMoved = false

    private var pendingState: ListHabitsState? = null

    override fun clearSelection() {
        if (selected.isEmpty()) return
        selected.clear()
        notifyDataSetChanged()
        observable.notifyListeners()
    }

    fun setState(state: ListHabitsState) {
        if (isDragging) {
            pendingState = state
            return
        }
        pendingState = null
        this.state = state
        items = state.items.toMutableList()
        val visibleIds = state.items.mapTo(mutableSetOf()) { it.id }
        selected.retainAll(visibleIds)
        notifyDataSetChanged()
        observable.notifyListeners()
    }

    fun hasNoHabit(): Boolean {
        return state?.hasHabits == false
    }

    override fun getSelected(): List<Habit> {
        return selected.mapNotNull { allHabits.getById(it) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    /**
     * Returns whether list of selected items is empty.
     *
     * @return true if selection is empty, false otherwise
     */
    val isSelectionEmpty: Boolean
        get() = selected.isEmpty()

    val isSortable: Boolean
        get() = state?.isSortable == true

    override fun onBindViewHolder(
        holder: HabitCardViewHolder,
        position: Int
    ) {
        if (listView == null) return
        val st = state ?: return
        val record = items[position]
        val isSelected = selected.contains(record.id)
        listView!!.bindCardView(
            holder,
            HabitCardViewState(record, isSelected, st.checkmarkCount)
        )
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
     * Removes a list of habits from the adapter.
     *
     *
     * Note that this only has effect on the adapter state. The models are not
     * modified, and the change is lost on the next [setState]. This method is
     * useful for making the list more responsive: while we wait for the command to
     * finish, the state can be modified to reflect the changes immediately.
     *
     * @param selected list of habits to be removed
     */
    override fun performRemove(selected: List<Habit>) {
        val current = state ?: return
        val ids = selected.mapNotNull { it.id }.toSet()
        if (ids.isEmpty()) return
        setState(current.copy(items = current.items.filter { it.id !in ids }))
    }

    override fun setFilter(matcher: HabitMatcher) {
        filter = matcher
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

    override var primaryOrder: Order
        get() = currentPrimaryOrder
        set(value) {
            currentPrimaryOrder = value
            preferences.defaultPrimaryOrder = value
        }

    override var secondaryOrder: Order
        get() = currentSecondaryOrder
        set(value) {
            currentSecondaryOrder = value
            preferences.defaultSecondaryOrder = value
        }

    /**
     * Selects or deselects the item at a given position.
     *
     * @param position position of the item to be toggled
     */
    fun toggleSelection(position: Int) {
        val id = items.getOrNull(position)?.id ?: return
        if (!selected.remove(id)) selected.add(id)
        notifyDataSetChanged()
    }

    init {
        currentPrimaryOrder = preferences.defaultPrimaryOrder
        currentSecondaryOrder = preferences.defaultSecondaryOrder
        setHasStableIds(true)
    }

    fun onDragStart(position: Int) {
        isDragging = true
        draggedId = items.getOrNull(position)?.id
        dragMoved = false
    }

    fun onDragMove(from: Int, to: Int) {
        if (!isDragging) return
        dragMoved = true
        val record = items.removeAt(from)
        items.add(to, record)
        notifyItemMoved(from, to)
    }

    fun onDragEnd(): Triple<Long, Long, Boolean>? {
        val id = draggedId
        val moved = dragMoved
        isDragging = false
        draggedId = null
        dragMoved = false
        if (id == null) return null
        if (!moved) return null
        if (items.size <= 1) return null
        val k = items.indexOfFirst { it.id == id }
        if (k < 0) return null
        val refId = if (k > 0) items[k - 1].id else items[1].id
        val placeBefore = k == 0
        return Triple(id, refId, placeBefore)
    }

    fun flushPending() {
        val p = pendingState
        pendingState = null
        if (p != null && !isDragging) setState(p)
    }
}
