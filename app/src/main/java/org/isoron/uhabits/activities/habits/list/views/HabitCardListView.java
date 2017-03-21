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

package org.isoron.uhabits.activities.habits.list.views;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.support.v7.widget.helper.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.activities.habits.list.controllers.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.models.*;

import java.util.*;

public class HabitCardListView extends RecyclerView
{
    @Nullable
    private HabitCardListAdapter adapter;

    @Nullable
    private Controller controller;

    private final ItemTouchHelper touchHelper;

    private int checkmarkCount;

    private int dataOffset;

    private LinkedList<HabitCardViewHolder> attachedHolders;

    public HabitCardListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setLongClickable(true);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

        TouchHelperCallback callback = new TouchHelperCallback();
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(this);

        attachedHolders = new LinkedList<>();
    }

    public void attachCardView(HabitCardViewHolder holder)
    {
        attachedHolders.add(holder);
    }

    /**
     * Builds a new HabitCardView to be eventually added to this list,
     * containing the given data.
     *
     * @param holder     the ViewHolder containing the HabitCardView that should
     *                   be built
     * @param habit      the habit for this card
     * @param score      the current score for the habit
     * @param checkmarks the list of checkmark values to be included in the
     *                   card
     * @param selected   true if the card is selected, false otherwise
     * @return the HabitCardView generated
     */
    public View bindCardView(@NonNull HabitCardViewHolder holder,
                             @NonNull Habit habit,
                             int score,
                             int[] checkmarks,
                             boolean selected)
    {
        HabitCardView cardView = (HabitCardView) holder.itemView;
        cardView.setHabit(habit);
        cardView.setSelected(selected);
        cardView.setCheckmarkValues(checkmarks);
        cardView.setCheckmarkCount(checkmarkCount);
        cardView.setDataOffset(dataOffset);
        cardView.setScore(score);
        if (controller != null) setupCardViewController(holder);
        return cardView;
    }

    public View createCardView()
    {
        return new HabitCardView(getContext());
    }

    public void detachCardView(HabitCardViewHolder holder)
    {
        attachedHolders.remove(holder);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter)
    {
        this.adapter = (HabitCardListAdapter) adapter;
        super.setAdapter(adapter);
    }

    public void setCheckmarkCount(int checkmarkCount)
    {
        this.checkmarkCount = checkmarkCount;
    }

    public void setController(@Nullable Controller controller)
    {
        this.controller = controller;
    }

    public void setDataOffset(int dataOffset)
    {
        this.dataOffset = dataOffset;
        for (HabitCardViewHolder holder : attachedHolders)
        {
            HabitCardView cardView = (HabitCardView) holder.itemView;
            cardView.setDataOffset(dataOffset);
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (adapter != null) adapter.onAttached();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (adapter != null) adapter.onDetached();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        BundleSavedState bss = (BundleSavedState) state;
        dataOffset = bss.bundle.getInt("dataOffset");
        super.onRestoreInstanceState(bss.getSuperState());
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putInt("dataOffset", dataOffset);
        return new BundleSavedState(superState, bundle);
    }

    protected void setupCardViewController(@NonNull HabitCardViewHolder holder)
    {
        HabitCardView cardView = (HabitCardView) holder.itemView;
        HabitCardController cardController = new HabitCardController();
        cardController.setListener(controller);
        cardView.setController(cardController);
        cardController.setView(cardView);

        GestureDetector detector = new GestureDetector(getContext(),
            new CardViewGestureDetector(holder));

        cardView.setOnTouchListener((v, ev) -> {
            detector.onTouchEvent(ev);
            return true;
        });
    }

    public interface Controller
        extends CheckmarkButtonController.Listener, HabitCardController.Listener
    {
        void drop(int from, int to);

        void onItemClick(int pos);

        void onItemLongClick(int pos);

        void startDrag(int position);
    }

    private class CardViewGestureDetector
        extends GestureDetector.SimpleOnGestureListener
    {
        @NonNull
        private final HabitCardViewHolder holder;

        public CardViewGestureDetector(@NonNull HabitCardViewHolder holder)
        {
            this.holder = holder;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            int position = holder.getAdapterPosition();
            if (controller != null) controller.onItemLongClick(position);
            if (adapter.isSortable()) touchHelper.startDrag(holder);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            int position = holder.getAdapterPosition();
            if (controller != null) controller.onItemClick(position);
            return true;
        }
    }

    class TouchHelperCallback extends ItemTouchHelper.Callback
    {
        @Override
        public int getMovementFlags(RecyclerView recyclerView,
                                    ViewHolder viewHolder)
        {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean isItemViewSwipeEnabled()
        {
            return false;
        }

        @Override
        public boolean isLongPressDragEnabled()
        {
            return false;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              ViewHolder from,
                              ViewHolder to)
        {
            if (controller == null) return false;
            controller.drop(from.getAdapterPosition(), to.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(ViewHolder viewHolder, int direction)
        {
            // NOP
        }
    }
}
