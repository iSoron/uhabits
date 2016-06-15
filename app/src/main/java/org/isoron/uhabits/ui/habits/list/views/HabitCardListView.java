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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.habits.list.controllers.CheckmarkButtonController;
import org.isoron.uhabits.ui.habits.list.controllers.HabitCardController;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListAdapter;

public class HabitCardListView extends DragSortListView
{
    @Nullable
    private HabitCardListAdapter adapter;

    @Nullable
    private Controller controller;

    public HabitCardListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setFloatViewManager(new ViewManager());
        setDragEnabled(true);
        setLongClickable(true);
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
     * @return the HabitCardView generated
     */
    public View buildCardView(@Nullable HabitCardView cardView,
                              @NonNull Habit habit,
                              int score,
                              int[] checkmarks,
                              boolean selected)
    {
        if (cardView == null) cardView = new HabitCardView(getContext());

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
        }

        return cardView;
    }

    @Override
    public void setAdapter(ListAdapter adapter)
    {
        this.adapter = (HabitCardListAdapter) adapter;
        super.setAdapter(adapter);
    }

    public void setController(@Nullable Controller controller)
    {
        this.controller = controller;
        setDropListener(controller);
        setDragListener(controller);
        setOnItemClickListener(null);
        setOnLongClickListener(null);

        if (controller == null) return;

        setOnItemClickListener((p, v, pos, id) -> controller.onItemClick(pos));
        setOnItemLongClickListener((p, v, pos, id) -> {
            controller.onItemLongClick(pos);
            return true;
        });
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
                                        HabitCardController.Listener,
                                        DropListener,
                                        DragListener
    {
        void onItemClick(int pos);

        void onItemLongClick(int pos);
    }

    private class ViewManager extends DragSortController
    {
        public ViewManager()
        {
            super(HabitCardListView.this);
            setRemoveEnabled(false);
        }

        @Override
        public View onCreateFloatView(int position)
        {
            if (adapter == null) return null;
            return adapter.getView(position, null, null);
        }

        @Override
        public void onDestroyFloatView(View floatView)
        {
        }
    }
}
