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
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.BundleSavedState
import org.isoron.uhabits.core.models.Habit
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
    private val viewportLayoutManager: ViewportLayoutManager

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
        viewportLayoutManager = ViewportLayoutManager(context)
        layoutManager = viewportLayoutManager
        applyBottomInset()
        super.setAdapter(adapter)
    }

    /**
     * Remember the numeric viewport before a status reorder is applied.
     * Later moves in the same refresh must not replace that anchor: item positions
     * are already rewritten by the first [androidx.recyclerview.widget.RecyclerView.Adapter.notifyItemMoved].
     */
    fun preserveViewportForStatusMove() {
        viewportLayoutManager.preserveViewport()
    }

    /**
     * The refresh that emitted the moves has finished. Restore the anchor on the
     * next layout, then release it so user scrolling works again.
     */
    fun finishPreservingViewport() {
        viewportLayoutManager.onMovesFinished()
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

    fun bindCardView(
        holder: HabitCardViewHolder,
        habit: Habit,
        score: Double,
        checkmarks: IntArray,
        completed: BooleanArray,
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
        cardView.completed = completed
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
            controller.value.onItemLongClick(position)
            if (adapter.isSortable) touchHelper.startDrag(holder)
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
            return makeMovementFlags(UP or DOWN, START or END)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            from: ViewHolder,
            to: ViewHolder
        ): Boolean {
            controller.value.drop(from.adapterPosition, to.adapterPosition)
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

    /**
     * [LinearLayoutManager] anchors layout to a child view. After [notifyItemMoved] that
     * child's adapter position already points at the habit's new slot, so the viewport
     * follows the habit. Focus recovery does the same for a focused row.
     * While a status reorder is pending, layout is pinned to the captured position
     * and pixel offset instead.
     */
    private inner class ViewportLayoutManager(context: Context) : LinearLayoutManager(context) {
        private var pendingPosition = RecyclerView.NO_POSITION
        private var pendingOffset = 0
        private var movesFinished = false
        private var generation = 0

        fun preserveViewport() {
            // Drop a clear posted for an earlier layout, and keep the first anchor in this batch.
            movesFinished = false
            generation += 1
            if (pendingPosition != RecyclerView.NO_POSITION) return
            if (childCount == 0) return
            val position = findFirstVisibleItemPosition()
            if (position == RecyclerView.NO_POSITION) return
            val child = findViewByPosition(position) ?: return
            val topMargin = (child.layoutParams as? RecyclerView.LayoutParams)?.topMargin ?: 0
            pendingOffset = getDecoratedTop(child) - topMargin - this@HabitCardListView.paddingTop
            pendingPosition = position
        }

        fun onMovesFinished() {
            if (pendingPosition == RecyclerView.NO_POSITION) return
            if (childCount == 0) {
                clearPendingViewport()
                return
            }
            movesFinished = true
            this@HabitCardListView.requestLayout()
        }

        override fun supportsPredictiveItemAnimations(): Boolean {
            if (pendingPosition != RecyclerView.NO_POSITION) return false
            return super.supportsPredictiveItemAnimations()
        }

        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler,
            state: RecyclerView.State
        ) {
            if (!state.isPreLayout()) applyPendingViewport(state.itemCount)
            super.onLayoutChildren(recycler, state)
        }

        override fun onLayoutCompleted(state: RecyclerView.State) {
            super.onLayoutCompleted(state)
            if (pendingPosition == RecyclerView.NO_POSITION || !movesFinished) return
            val generationAtLayout = generation
            // Runs after this traversal, including focus recovery inside it.
            this@HabitCardListView.post {
                if (generationAtLayout == generation && movesFinished) clearPendingViewport()
            }
        }

        override fun onRequestChildFocus(
            parent: RecyclerView,
            state: RecyclerView.State,
            child: View,
            focused: View?
        ): Boolean {
            if (pendingPosition != RecyclerView.NO_POSITION) return true
            return super.onRequestChildFocus(parent, state, child, focused)
        }

        override fun requestChildRectangleOnScreen(
            parent: RecyclerView,
            child: View,
            rect: Rect,
            immediate: Boolean,
            focusedChildVisible: Boolean
        ): Boolean {
            if (pendingPosition != RecyclerView.NO_POSITION) return false
            return super.requestChildRectangleOnScreen(
                parent,
                child,
                rect,
                immediate,
                focusedChildVisible
            )
        }

        private fun applyPendingViewport(itemCount: Int) {
            val position = pendingPosition
            if (position == RecyclerView.NO_POSITION || itemCount <= 0) return
            scrollToPositionWithOffset(position.coerceIn(0, itemCount - 1), pendingOffset)
        }

        private fun clearPendingViewport() {
            pendingPosition = RecyclerView.NO_POSITION
            movesFinished = false
        }
    }
}
