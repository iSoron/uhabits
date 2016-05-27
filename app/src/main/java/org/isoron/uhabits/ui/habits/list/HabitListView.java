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

package org.isoron.uhabits.ui.habits.list;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.utils.Preferences;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HabitListView extends DragSortListView implements View.OnClickListener,
        View.OnLongClickListener, DragSortListView.DropListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, DragSortListView.DragListener, HabitListLoader.Listener
{
    private final HabitListLoader loader;
    private final HabitListAdapter adapter;
    private final ListHabitsHelper helper;
    private final Preferences prefs;
    private final List<Integer> selectedPositions;

    @Nullable
    private Listener listener;
    private long lastLongClick;
    private boolean showArchived;

    public HabitListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        loader = new HabitListLoader();
        adapter = new HabitListAdapter(context, loader);
        selectedPositions = new LinkedList<>();
        prefs = Preferences.getInstance();
        helper = new ListHabitsHelper(getContext(), loader);

        adapter.setSelectedPositions(selectedPositions);
        adapter.setOnCheckmarkClickListener(this);
        adapter.setOnCheckmarkLongClickListener(this);
        loader.setListener(this);
        loader.setCheckmarkCount(helper.getButtonCount());

        setAdapter(adapter);
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        setDropListener(this);
        setDragListener(this);
        setFloatViewManager(new HabitsDragSortController());
        setDragEnabled(false);
        setLongClickable(true);
    }

    public HabitListLoader getLoader()
    {
        return loader;
    }

    public List<Integer> getSelectedPositions()
    {
        return selectedPositions;
    }

    public void setListener(@Nullable Listener l)
    {
        this.listener = l;
    }

    @Override
    public void drop(int from, int to)
    {
        if(from == to) return;
        cancelSelection();

        loader.reorder(from, to);
        adapter.notifyDataSetChanged();
        loader.updateAllHabits(false);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() != R.id.tvCheck) return;

        if (prefs.isShortToggleEnabled()) toggleCheckmark(v);
        else if(listener != null) listener.onInvalidToggle();
    }

    @Override
    public boolean onLongClick(View v)
    {
        lastLongClick = new Date().getTime();
        if (v.getId() != R.id.tvCheck) return true;
        if (prefs.isShortToggleEnabled()) return true;
        toggleCheckmark(v);
        return true;
    }

    public void toggleShowArchived()
    {
        showArchived = !showArchived;
        loader.setIncludeArchived(showArchived);
        loader.updateAllHabits(true);
    }

    private void toggleCheckmark(View v)
    {
        Long id = helper.getHabitIdFromCheckmarkView(v);
        Habit habit = loader.habits.get(id);
        if(habit == null) return;

        float x = v.getX() + v.getWidth() / 2.0f + ((View) v.getParent()).getX();
        float y = v.getY() + v.getHeight() / 2.0f + ((View) v.getParent()).getY();
        helper.triggerRipple((View) v.getParent().getParent(), x, y);

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        helper.toggleCheckmarkView(v, habit);

        long timestamp = helper.getTimestampFromCheckmarkView(v);

        if(listener != null) listener.onToggleCheckmark(habit, timestamp);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (new Date().getTime() - lastLongClick < 1000) return;

        if(selectedPositions.isEmpty())
        {
            Habit habit = loader.habitsList.get(position);
            if(listener != null) listener.onHabitClick(habit);
        }
        else
        {
            toggleItemSelected(position);
            adapter.notifyDataSetChanged();
        }
    }

    private void toggleItemSelected(int position)
    {
        int k = selectedPositions.indexOf(position);
        if(k < 0) selectedPositions.add(position);
        else selectedPositions.remove(k);

        if(listener != null)
        {
            if (selectedPositions.isEmpty()) listener.onHabitSelectionFinish();
            else listener.onHabitSelectionChange();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        selectHabit(position);
        return true;
    }

    private void selectHabit(int position)
    {
        if(!selectedPositions.contains(position)) selectedPositions.add(position);
        adapter.notifyDataSetChanged();

        if(listener != null)
        {
            if (selectedPositions.size() == 1) listener.onHabitSelectionStart();
            else listener.onHabitSelectionChange();
        }
    }

    @Override
    public void drag(int from, int to)
    {
    }

    @Override
    public void startDrag(int position)
    {
        selectHabit(position);
    }

    public boolean getShowArchived()
    {
        return showArchived;
    }

    public void cancelSelection()
    {
        selectedPositions.clear();
        adapter.notifyDataSetChanged();
        setDragEnabled(true);
        if(listener != null) listener.onHabitSelectionFinish();
    }

    public void refreshData(Long refreshKey)
    {
        if (refreshKey == null) loader.updateAllHabits(true);
        else loader.updateHabit(refreshKey);
    }

    @Override
    public void onLoadFinished()
    {
        adapter.notifyDataSetChanged();
        if(listener != null) listener.onDatasetChanged();
    }

    private class HabitsDragSortController extends DragSortController
    {
        public HabitsDragSortController()
        {
            super(HabitListView.this);
            setRemoveEnabled(false);
        }

        @Override
        public View onCreateFloatView(int position)
        {
            return adapter.getView(position, null, null);
        }

        @Override
        public void onDestroyFloatView(View floatView)
        {
        }
    }

    public interface Listener
    {
        void onToggleCheckmark(Habit habit, long timestamp);

        void onHabitClick(Habit habit);

        void onHabitSelectionStart();

        void onHabitSelectionFinish();

        void onHabitSelectionChange();

        void onInvalidToggle();

        void onDatasetChanged();
    }
}
