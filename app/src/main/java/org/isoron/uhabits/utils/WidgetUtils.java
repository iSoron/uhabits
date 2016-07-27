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

package org.isoron.uhabits.utils;

import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.widget.*;

import org.isoron.uhabits.widgets.*;

import static android.appwidget.AppWidgetManager.*;
import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;
import static org.isoron.uhabits.utils.InterfaceUtils.*;

public abstract class WidgetUtils
{
    @NonNull
    public static WidgetDimensions getDimensionsFromOptions(
        @NonNull Context ctx, @NonNull Bundle options)
    {
        if (SDK_INT < JELLY_BEAN)
            throw new AssertionError("method requires jelly-bean");

        int maxWidth =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MAX_WIDTH));
        int maxHeight =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MAX_HEIGHT));
        int minWidth =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MIN_WIDTH));
        int minHeight =
            (int) dpToPixels(ctx, options.getInt(OPTION_APPWIDGET_MIN_HEIGHT));

        return new WidgetDimensions(minWidth, maxHeight, maxWidth, minHeight);
    }

    public static void updateAppWidget(@NonNull AppWidgetManager manager,
                                       @NonNull BaseWidget widget)
    {
        if (SDK_INT < JELLY_BEAN)
        {
            RemoteViews portrait = widget.getPortraitRemoteViews();
            manager.updateAppWidget(widget.getId(), portrait);
        }
        else
        {
            RemoteViews landscape = widget.getLandscapeRemoteViews();
            RemoteViews portrait = widget.getPortraitRemoteViews();
            RemoteViews views = new RemoteViews(landscape, portrait);
            manager.updateAppWidget(widget.getId(), views);
        }
    }
}
