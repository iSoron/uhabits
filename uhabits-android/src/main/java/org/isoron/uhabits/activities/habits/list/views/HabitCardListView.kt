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

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.BundleSavedState
import org.isoron.uhabits.inject.ActivityContext

@Inject
class HabitCardListViewFactory(
    @ActivityContext val context: Context,
    val adapter: HabitCardListAdapter,
    val cardViewFactory: HabitCardViewFactory,
    val controller: Lazy<HabitCardListController>
) {
    fun create() = HabitCardListView(context, adapter, cardViewFactory, controller)
}

class HabitCardListView(
    @ActivityContext context: Context,
    private val adapter: HabitCardListAdapter,
    private val cardViewFactory: HabitCardViewFactory,
    private val controller: Lazy<HabitCardListController>
) : RecyclerView(context, null, R.attr.scrollableRecyclerViewStyle) {

    var checkmarkCount: Int = 0
    private var insetDecorationsAdded: Boolean = false

    private var dataOffset: Int = 0

    fun setDataOffset(offset: Int) {
        dataOffset = offset
        attachedHolders
            .map { it.itemView as HabitCardView }
            .forEach { it.setDataOffset(offset) }
    }

    private val attachedHolders = mutableListOf<HabitCardViewHolder>()

    private val touchHelper = ItemTouchHelper(TouchHelperCallback()).apply {
        attachToRecyclerView(this@HabitCardListView)
    }

    init {
        setHasFixedSize(true)
        isLongClickable = true
        layoutManager = LinearLayoutManager(context)
        applyBottomInset()
        super.setAdapter(adapter)
    }

    private fun applyBottomInset() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            if (insetDecorationsAdded) return@setOnApplyWindowInsetsListener insets
            insetDecorationsAdded = true
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: State
                ) {
                    val itemCount = parent.adapter?.itemCount
                    if (parent.getChildAdapterPosition(view) == itemCount?.minus(1)) {
                        outRect.bottom = systemBarsInsets.bottom
                    }
                }
            })
            insets
        }
    }

    fun createHabitCardView(): HabitCardView {
        return cardViewFactory.create()
    }

    fun bindCardView(holder: HabitCardViewHolder, state: HabitCardViewState): View {
        val cardView = holder.itemView as HabitCardView
        cardView.setState(state.copy(buttonCount = checkmarkCount))

        val detector = GestureDetector(context, CardViewGestureDetector(holder))
        cardView.setOnTouchListener { _, ev ->
            detector.onTouchEvent(ev)
            true
        }

        return cardView
    }

    fun attachCardView(holder: HabitCardViewHolder) {
        (holder.itemView as HabitCardView).setDataOffset(dataOffset)
        attachedHolders.add(holder)
    }

    fun detachCardView(holder: HabitCardViewHolder) {
        attachedHolders.remove(holder)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is BundleSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        dataOffset = state.bundle!!.getInt("dataOffset")
        super.onRestoreInstanceState(state.superState)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle().apply {
            putInt("dataOffset", dataOffset)
        }
        return BundleSavedState(superState, bundle)
    }

    interface Controller {
        fun commitReorder() {}
        fun onItemClick(pos: Int) {}
        fun onItemLongClick(pos: Int) {}
        fun onToggle(habitId: Long, date: LocalDate, value: Int, notes: String, x: Float, y: Float) {}
        fun onEdit(habitId: Long, date: LocalDate, x: Float, y: Float) {}
    }

    private inner class CardViewGestureDetector(
        private val holder: HabitCardViewHolder
    ) : GestureDetector.SimpleOnGestureListener() {

        override fun onLongPress(e: MotionEvent) {
            val position = holder.adapterPosition
            controller.value.onItemLongClick(position)
            if (adapter.isSortable) {
                adapter.onDragStart(position)
                touchHelper.startDrag(holder)
            }
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val position = holder.adapterPosition
            controller.value.onItemClick(position)
            return true
        }
    }

    inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder
        ): Int {
            if (!adapter.isSortable) return 0
            return makeMovementFlags(UP or DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            from: ViewHolder,
            to: ViewHolder
        ): Boolean {
            if (!adapter.isSortable) return false
            val fromPos = from.adapterPosition
            val toPos = to.adapterPosition
            if (fromPos < 0 || toPos < 0) return false
            adapter.onDragMove(fromPos, toPos)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            controller.value.commitReorder()
        }

        override fun isItemViewSwipeEnabled() = false
        override fun isLongPressDragEnabled() = false
    }
}
