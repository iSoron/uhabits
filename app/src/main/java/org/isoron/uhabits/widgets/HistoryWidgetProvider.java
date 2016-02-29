/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.view.View;

import org.isoron.helpers.DialogHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.HabitHistoryView;

public class HistoryWidgetProvider extends  BaseWidgetProvider
{
    @Override
    protected View buildCustomView(Context context, int maxHeight, int maxWidth, Habit habit)
    {
        HabitHistoryView view = new HabitHistoryView(context, null);
        view.setHabit(habit);
        view.measure(maxWidth, maxHeight);
        view.layout(0, 0, maxWidth, maxHeight);

        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        height -= DialogHelper.dpToPixels(context, 12);
        view.measure(width, height);
        view.layout(0, 0, width, height);

        return view;
    }

    @Override
    protected PendingIntent getOnClickPendingIntent(Context context, Habit habit)
    {
        return null;
    }

    @Override
    protected int getDefaultHeight()
    {
        return 200;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 200;
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.widget_graph;
    }
}
