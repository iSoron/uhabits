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

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.BundleSavedState
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.inject.ActivityContext
import javax.inject.Inject

class HabitCardListViewFactory
@Inject constructor(
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

    var dataOffset: Int = 0
        set(value) {
            field = value
            attachedHolders
                .map { it.itemView as HabitCardView }
                .forEach { it.dataOffset = value }
        }

    private val attachedHolders = mutableListOf<HabitCardViewHolder>()
    private val touchHelper = ItemTouchHelper(TouchHelperCallback()).apply {
        attachToRecyclerView(this@HabitCardListView)
    }

    init {
        setHasFixedSize(true)
        isLongClickable = true
        layoutManager = LinearLayoutManager(context)
        super.setAdapter(adapter)
    }

    fun createHabitCardView(): HabitCardView {
        return cardViewFactory.create()
    }

    fun bindCardView(
        holder: HabitCardViewHolder,
        habit: Habit,
        score: Double,
        checkmarks: IntArray,
        notes: Array<String>,
        selected: Boolean
    ): View {
        val cardView = holder.itemView as HabitCardView
        cardView.habit = habit
        cardView.isSelected = selected
        cardView.values = checkmarks
        cardView.buttonCount = checkmarkCount
        cardView.dataOffset = dataOffset
        cardView.score = score
        cardView.unit = habit.unit
        cardView.threshold = habit.targetValue / habit.frequency.denominator
        cardView.notes = notes

        val detector = GestureDetector(context, CardViewGestureDetector(holder))
        cardView.setOnTouchListener { _, ev ->
            detector.onTouchEvent(ev)
            true
        }

        return cardView
    }

    fun attachCardView(holder: HabitCardViewHolder) {
        (holder.itemView as HabitCardView).dataOffset = dataOffset
        attachedHolders.add(holder)
    }

    fun detachCardView(holder: HabitCardViewHolder) {
        attachedHolders.remove(holder)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter.onAttached()
    }

    override fun onDetachedFromWindow() {
        adapter.onDetached()
        super.onDetachedFromWindow()
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
        fun drop(from: Int, to: Int) {}
        fun onItemClick(pos: Int) {}
        fun onItemLongClick(pos: Int) {}
        fun startDrag(position: Int) {}
    }

    private inner class CardViewGestureDetector(
        private val holder: HabitCardViewHolder
    ) : GestureDetector.SimpleOnGestureListener() {

        override fun onLongPress(e: MotionEvent) {
            val position = holder.adapterPosition
            controller.get().onItemLongClick(position)
            if (adapter.isSortable) touchHelper.startDrag(holder)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val position = holder.adapterPosition
            controller.get().onItemClick(position)
            return true
        }
    }

    inner class TouchHelperCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder
        ): Int {
            return makeMovementFlags(UP or DOWN, START or END)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            from: ViewHolder,
            to: ViewHolder
        ): Boolean {
            controller.get().drop(from.adapterPosition, to.adapterPosition)
            return true
        }

        override fun onSwiped(
            viewHolder: ViewHolder,
            direction: Int
        ) {
        }

        override fun isItemViewSwipeEnabled() = false
        override fun isLongPressDragEnabled() = false
    }
}
