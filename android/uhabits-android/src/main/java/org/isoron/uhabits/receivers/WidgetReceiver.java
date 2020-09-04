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

package org.isoron.uhabits.receivers;

import android.content.*;
import android.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.widgets.*;
import org.isoron.uhabits.intents.*;
import org.isoron.uhabits.widgets.*;
import org.isoron.uhabits.widgets.activities.*;

import dagger.*;

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 * <p>
 * All broadcast messages are received and processed by this class.
 */
public class WidgetReceiver extends BroadcastReceiver
{
    public static final String ACTION_ADD_REPETITION =
            "org.isoron.uhabits.ACTION_ADD_REPETITION";

    public static final String ACTION_DISMISS_REMINDER =
            "org.isoron.uhabits.ACTION_DISMISS_REMINDER";

    public static final String ACTION_REMOVE_REPETITION =
            "org.isoron.uhabits.ACTION_REMOVE_REPETITION";

    public static final String ACTION_TOGGLE_REPETITION =
            "org.isoron.uhabits.ACTION_TOGGLE_REPETITION";

    public static final String ACTION_SET_NUMERICAL_VALUE =
            "org.isoron.uhabits.ACTION_SET_NUMERICAL_VALUE";

    public static final String ACTION_UPDATE_WIDGETS_VALUE =
            "org.isoron.uhabits.ACTION_UPDATE_WIDGETS_VALUE";

    private static final String TAG = "WidgetReceiver";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        HabitsApplication app =
                (HabitsApplication) context.getApplicationContext();

        WidgetComponent component = DaggerWidgetReceiver_WidgetComponent
                .builder()
                .habitsApplicationComponent(app.getComponent())
                .build();

        IntentParser parser = app.getComponent().getIntentParser();
        WidgetBehavior controller = component.getWidgetController();
        Preferences prefs = app.getComponent().getPreferences();
        WidgetUpdater widgetUpdater = app.getComponent().getWidgetUpdater();

        Log.i(TAG, String.format("Received intent: %s", intent.toString()));

        try
        {
            IntentParser.CheckmarkIntentData data = null;
            if (intent.getAction() != ACTION_UPDATE_WIDGETS_VALUE)
            {
                data = parser.parseCheckmarkIntent(intent);
            }

            switch (intent.getAction())
            {
                case ACTION_ADD_REPETITION:
                    Log.d(TAG, String.format(
                            "onAddRepetition habit=%d timestamp=%d",
                            data.getHabit().getId(),
                            data.getTimestamp().getUnixTime()));
                    controller.onAddRepetition(data.getHabit(),
                            data.getTimestamp());
                    break;

                case ACTION_TOGGLE_REPETITION:
                    Log.d(TAG, String.format(
                            "onToggleRepetition habit=%d timestamp=%d",
                            data.getHabit().getId(),
                            data.getTimestamp().getUnixTime()));
                    controller.onToggleRepetition(data.getHabit(),
                            data.getTimestamp());
                    break;

                case ACTION_REMOVE_REPETITION:
                    Log.d(TAG, String.format(
                            "onRemoveRepetition habit=%d timestamp=%d",
                            data.getHabit().getId(),
                            data.getTimestamp().getUnixTime()));
                    controller.onRemoveRepetition(data.getHabit(),
                            data.getTimestamp());
                    break;
                case ACTION_SET_NUMERICAL_VALUE:
                    Intent numberSelectorIntent = new Intent(context, NumericalCheckmarkWidgetActivity.class);
                    numberSelectorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    numberSelectorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    numberSelectorIntent.setAction(NumericalCheckmarkWidgetActivity.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY);
                    parser.copyIntentData(intent,numberSelectorIntent);
                    context.startActivity(numberSelectorIntent);
                    break;
                case ACTION_UPDATE_WIDGETS_VALUE:
                    widgetUpdater.updateWidgets();
                    widgetUpdater.scheduleStartDayWidgetUpdate();
                    break;
            }
        }
        catch (RuntimeException e)
        {
            Log.e("WidgetReceiver", "could not process intent", e);
        }
    }

    @ReceiverScope
    @Component(dependencies = HabitsApplicationComponent.class)
    interface WidgetComponent
    {
        WidgetBehavior getWidgetController();
    }
}
