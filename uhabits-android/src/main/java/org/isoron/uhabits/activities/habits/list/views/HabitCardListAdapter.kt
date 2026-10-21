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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
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
    private var rows: MutableList<Row> = mutableListOf()
    private val categoryIds = HashMap<String, Long>()
    private var nextCategoryId = CATEGORY_ID_BASE

    companion object {
        const val VIEW_TYPE_HABIT = 0
        const val VIEW_TYPE_CATEGORY = 1
        private const val CATEGORY_ID_BASE = 1L shl 62
    }

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
        rows = buildRows(items).toMutableList()
        notifyDataSetChanged()
        observable.notifyListeners()
    }

    private fun buildRows(habits: List<HabitCardState>): List<Row> {
        if (habits.isEmpty()) return emptyList()
        val built = mutableListOf<Row>()
        var lastHeader: String? = null
        habits.forEach { card ->
            val cat = card.category
            if (cat.isNotBlank() && cat != lastHeader) {
                built.add(Row.Category(categoryIdFor(cat), cat))
                lastHeader = cat
            }
            built.add(Row.Habit(card))
        }
        return built
    }

    private fun categoryIdFor(category: String): Long {
        val existing = categoryIds[category]
        if (existing != null) return existing
        val id = nextCategoryId
        nextCategoryId++
        categoryIds[category] = id
        return id
    }

    fun habitAt(position: Int): HabitCardState? {
        return when (val row = rows.getOrNull(position)) {
            is Row.Habit -> row.card
            is Row.Category -> null
            null -> null
        }
    }

    fun categoryAt(position: Int): String? {
        return when (val row = rows.getOrNull(position)) {
            is Row.Category -> row.title
            is Row.Habit -> null
            null -> null
        }
    }

    fun hasNoHabit(): Boolean {
        return state?.hasHabits == false
    }

    override fun getSelected(): List<Habit> {
        return selected.mapNotNull { allHabits.getById(it) }
    }

    override fun getItemCount(): Int {
        return rows.size
    }

    override fun getItemId(position: Int): Long {
        return when (val row = rows[position]) {
            is Row.Habit -> row.card.id
            is Row.Category -> row.id
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (rows[position]) {
            is Row.Habit -> VIEW_TYPE_HABIT
            is Row.Category -> VIEW_TYPE_CATEGORY
        }
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
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (listView == null) return
        val st = state ?: return
        when (val row = rows[position]) {
            is Row.Habit -> {
                val isSelected = selected.contains(row.card.id)
                listView!!.bindCardView(
                    holder as HabitCardViewHolder,
                    HabitCardViewState(row.card, isSelected, st.checkmarkCount)
                )
            }
            is Row.Category -> {
                val categoryView = (holder as CategoryViewHolder).itemView as CategoryView
                categoryView.setState(CategoryViewState(row.title))
                listView!!.bindCategoryView(holder as CategoryViewHolder)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is HabitCardViewHolder) {
            listView!!.attachCardView(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is HabitCardViewHolder) {
            listView!!.detachCardView(holder)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> CategoryViewHolder(CategoryView(parent.context))
            else -> HabitCardViewHolder(listView!!.createHabitCardView())
        }
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
        val id = habitAt(position)?.id ?: return
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
        draggedId = (rows.getOrNull(position) as? Row.Habit)?.card?.id
        dragMoved = false
    }

    fun onDragMove(from: Int, to: Int) {
        if (!isDragging) return
        if (from !in rows.indices || to !in rows.indices) return
        dragMoved = true
        val row = rows.removeAt(from)
        rows.add(to, row)
        notifyItemMoved(from, to)
    }

    fun onDragEnd(): ReorderDragResult? {
        val id = draggedId
        val moved = dragMoved
        isDragging = false
        draggedId = null
        dragMoved = false
        if (id == null) return null
        if (!moved) return null
        val habitCount = rows.count { it is Row.Habit }
        if (habitCount < 2) return null
        val k = rows.indexOfFirst { it is Row.Habit && it.card.id == id }
        if (k < 0) return null
        var targetCategory = ""
        for (i in (k - 1) downTo 0) {
            val row = rows[i]
            if (row is Row.Category) {
                targetCategory = row.title
                break
            }
        }
        val above = rows.getOrNull(k - 1)
        val below = rows.getOrNull(k + 1)
        return when {
            above is Row.Habit -> ReorderDragResult(id, above.card.id, false, targetCategory)
            below is Row.Habit -> ReorderDragResult(id, below.card.id, true, targetCategory)
            else -> ReorderDragResult(id, null, false, targetCategory)
        }
    }

    fun flushPending() {
        val p = pendingState
        pendingState = null
        if (p != null && !isDragging) setState(p)
    }
}

sealed class Row {
    data class Habit(val card: HabitCardState) : Row()
    data class Category(val id: Long, val title: String) : Row()
}

data class ReorderDragResult(
    val habitId: Long,
    val refId: Long?,
    val placeBefore: Boolean,
    val targetCategory: String
)

class CategoryViewHolder(itemView: CategoryView) : RecyclerView.ViewHolder(itemView)
