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

package org.isoron.uhabits.widgets;

import android.app.*;
import android.content.*;
import android.support.annotation.*;
import android.view.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.views.*;

public class CheckmarkWidget extends BaseWidget
{
    @NonNull
    private final Habit habit;

    public CheckmarkWidget(@NonNull Context context,
                           int widgetId,
                           @NonNull Habit habit)
    {
        super(context, widgetId);
        this.habit = habit;
    }

    @Override
    public PendingIntent getOnClickPendingIntent(Context context)
    {
        return pendingIntentFactory.toggleCheckmark(habit, null);
    }

    @Override
    public void refreshData(View v)
    {
        CheckmarkWidgetView view = (CheckmarkWidgetView) v;
        int color = ColorUtils.getColor(getContext(), habit.getColor());
        int score = habit.getScores().getTodayValue();
        float percentage = (float) score / Score.MAX_VALUE;
        int checkmark = habit.getCheckmarks().getTodayValue();

        view.setPercentage(percentage);
        view.setActiveColor(color);
        view.setName(habit.getName());
        view.setCheckmarkValue(checkmark);
        view.refresh();
    }

    @Override
    protected View buildView()
    {
        return new CheckmarkWidgetView(getContext());
    }

    @Override
    protected int getDefaultHeight()
    {
        return 125;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 125;
    }
}
