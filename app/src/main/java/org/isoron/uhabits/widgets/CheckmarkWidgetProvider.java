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

import org.isoron.uhabits.HabitBroadcastReceiver;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.CheckmarkView;

public class CheckmarkWidgetProvider extends  BaseWidgetProvider
{
    @Override
    protected View buildCustomView(Context context, int maxHeight, int maxWidth, Habit habit)
    {
        CheckmarkView widgetView = new CheckmarkView(context);

        widgetView.setHabit(habit);
        widgetView.measure(maxWidth, maxHeight);
        widgetView.layout(0, 0, maxWidth, maxHeight);

        int width = widgetView.getMeasuredWidth();
        int height = widgetView.getMeasuredHeight();
        widgetView.measure(width, height);
        widgetView.layout(0, 0, width, height);

        return widgetView;
    }

    @Override
    protected PendingIntent getOnClickPendingIntent(Context context, Habit habit)
    {
        return HabitBroadcastReceiver.buildCheckIntent(context, habit, null);
    }

    @Override
    protected int getDefaultHeight()
    {
        return 200;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 160;
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.widget_checkmark;
    }
}
