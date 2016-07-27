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

import android.appwidget.*;
import android.content.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.commands.*;

import javax.inject.*;

/**
 * A WidgetUpdater listens to the commands being executed by the application and
 * updates the home-screen widgets accordingly.
 * <p>
 * There should be only one instance of this class, created when the application
 * starts. To access it, call HabitApplication.getWidgetUpdater().
 */
public class WidgetUpdater implements CommandRunner.Listener
{
    @NonNull
    private final CommandRunner commandRunner;

    @NonNull
    private final Context context;

    @Inject
    public WidgetUpdater(@NonNull @AppContext Context context,
                         @NonNull CommandRunner commandRunner)
    {
        this.context = context;
        this.commandRunner = commandRunner;
    }

    @Override
    public void onCommandExecuted(@NonNull Command command,
                                  @Nullable Long refreshKey)
    {
        updateWidgets();
    }

    /**
     * Instructs the updater to start listening to commands. If any relevant
     * commands are executed after this method is called, the corresponding
     * widgets will get updated.
     */
    public void startListening()
    {
        commandRunner.addListener(this);
    }

    /**
     * Instructs the updater to stop listening to commands. Every command
     * executed after this method is called will be ignored by the updater.
     */
    public void stopListening()
    {
        commandRunner.removeListener(this);
    }

    public void updateWidgets()
    {
        updateWidgets(CheckmarkWidgetProvider.class);
        updateWidgets(HistoryWidgetProvider.class);
        updateWidgets(ScoreWidgetProvider.class);
        updateWidgets(StreakWidgetProvider.class);
        updateWidgets(FrequencyWidgetProvider.class);
    }

    public void updateWidgets(Class providerClass)
    {
        ComponentName provider = new ComponentName(context, providerClass);
        Intent intent = new Intent(context, providerClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] =
            AppWidgetManager.getInstance(context).getAppWidgetIds(provider);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
