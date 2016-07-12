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

package org.isoron.uhabits.ui.habits.list.views;

import android.content.*;
import android.support.annotation.*;
import android.support.v7.widget.*;
import android.support.v7.widget.helper.*;
import android.util.*;
import android.view.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.habits.list.controllers.*;
import org.isoron.uhabits.ui.habits.list.model.*;

public class HabitCardListView extends RecyclerView
{
    @Nullable
    private HabitCardListAdapter adapter;

    @Nullable
    private Controller controller;

    public HabitCardListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setLongClickable(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

//        TouchHelperCallback callback = new TouchHelperCallback();
//        new ItemTouchHelper(callback).attachToRecyclerView(this);
    }

    /**
     * Builds a new HabitCardView to be eventually added to this list,
     * containing the given data.
     *
     * @param cardView   an old HabitCardView that should be reused if possible,
     *                   possibly null
     * @param habit      the habit for this card
     * @param score      the current score for the habit
     * @param checkmarks the list of checkmark values to be included in the
     *                   card
     * @param selected   true if the card is selected, false otherwise
     * @param position
     * @return the HabitCardView generated
     */
    public View bindCardView(@NonNull HabitCardView cardView,
                             @NonNull Habit habit,
                             int score,
                             int[] checkmarks,
                             boolean selected,
                             int position)
    {
        cardView.setHabit(habit);
        cardView.setSelected(selected);
        cardView.setCheckmarkValues(checkmarks);
        cardView.setScore(score);

        if (controller != null)
        {
            HabitCardController cardController = new HabitCardController();
            cardController.setListener(controller);
            cardView.setController(cardController);
            cardController.setView(cardView);

            cardView.setOnClickListener(v -> controller.onItemClick(position));
            cardView.setOnLongClickListener(v -> {
                controller.onItemLongClick(position);
                return true;
            });
        }

        return cardView;
    }

    public View createCardView()
    {
        return new HabitCardView(getContext());
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter)
    {
        this.adapter = (HabitCardListAdapter) adapter;
        super.setAdapter(adapter);
    }

    public void setController(@Nullable Controller controller)
    {
        this.controller = controller;
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

    public interface Controller extends CheckmarkButtonController.Listener,
                                        HabitCardController.Listener
    {
        void drag(int from, int to);

        void drop(int from, int to);

        void onItemClick(int pos);

        void onItemLongClick(int pos);

        void startDrag(int position);
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
        public boolean isLongPressDragEnabled()
        {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled()
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
