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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.models.Habit;

import java.util.List;

class HabitListAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    private HabitListLoader loader;
    private ListHabitsHelper helper;
    private List selectedPositions;
    private View.OnLongClickListener onCheckmarkLongClickListener;
    private View.OnClickListener onCheckmarkClickListener;

    public HabitListAdapter(Context context, HabitListLoader loader)
    {
        this.loader = loader;

        inflater = LayoutInflater.from(context);
        helper = new ListHabitsHelper(context, loader);
    }

    @Override
    public int getCount()
    {
        return loader.habits.size();
    }

    @Override
    public Habit getItem(int position)
    {
        return loader.habitsList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return (getItem(position)).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        final Habit habit = loader.habitsList.get(position);
        boolean selected = selectedPositions.contains(position);

        if (view == null || (Long) view.getTag(R.id.timestamp_key) != DateUtils.getStartOfToday())
        {
            view = helper.inflateHabitCard(inflater, onCheckmarkLongClickListener,
                    onCheckmarkClickListener);
        }

        helper.updateHabitCard(view, habit, selected);
        return view;
    }

    public void setSelectedPositions(List selectedPositions)
    {
        this.selectedPositions = selectedPositions;
    }

    public void setOnCheckmarkLongClickListener(View.OnLongClickListener listener)
    {
        this.onCheckmarkLongClickListener = listener;
    }

    public void setOnCheckmarkClickListener(View.OnClickListener listener)
    {
        this.onCheckmarkClickListener = listener;
    }
}
