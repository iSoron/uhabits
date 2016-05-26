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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class WidgetManager
{
    public static void updateWidgets(Context context)
    {
        updateWidgets(context, CheckmarkWidgetProvider.class);
        updateWidgets(context, HistoryWidgetProvider.class);
        updateWidgets(context, ScoreWidgetProvider.class);
        updateWidgets(context, StreakWidgetProvider.class);
        updateWidgets(context, FrequencyWidgetProvider.class);
    }

    private static void updateWidgets(Context context, Class providerClass)
    {
        ComponentName provider = new ComponentName(context, providerClass);
        Intent intent = new Intent(context, providerClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(provider);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
