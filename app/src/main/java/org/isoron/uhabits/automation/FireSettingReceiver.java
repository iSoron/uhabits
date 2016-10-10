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

package org.isoron.uhabits.automation;

import android.content.*;
import android.os.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.receivers.*;
import org.isoron.uhabits.utils.*;

import dagger.*;

public class FireSettingReceiver extends BroadcastReceiver
{
    public static final int ACTION_CHECK = 0;

    public static final int ACTION_UNCHECK = 1;

    public static final int ACTION_TOGGLE = 2;

    public static final String EXTRA_BUNDLE =
        "com.twofortyfouram.locale.intent.extra.BUNDLE";

    public static final String EXTRA_STRING_BLURB =
        "com.twofortyfouram.locale.intent.extra.BLURB";

    private HabitList allHabits;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        HabitsApplication app =
            (HabitsApplication) context.getApplicationContext();

        ReceiverComponent component =
            DaggerFireSettingReceiver_ReceiverComponent
                .builder()
                .appComponent(app.getComponent())
                .build();

        allHabits = app.getComponent().getHabitList();

        Arguments args = parseIntent(intent);
        if (args == null) return;

        long timestamp = DateUtils.getStartOfToday();
        WidgetController controller = component.getWidgetController();

        switch (args.action)
        {
            case ACTION_CHECK:
                controller.onAddRepetition(args.habit, timestamp);
                break;

            case ACTION_UNCHECK:
                controller.onRemoveRepetition(args.habit, timestamp);
                break;

            case ACTION_TOGGLE:
                controller.onToggleRepetition(args.habit, timestamp);
                break;
        }
    }

    private Arguments parseIntent(Intent intent)
    {
        Arguments args = new Arguments();

        Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE);
        if (bundle == null) return null;

        args.action = bundle.getInt("action");
        if (args.action < 0 || args.action > 2) return null;

        Habit habit = allHabits.getById(bundle.getLong("habit"));
        if (habit == null) return null;
        args.habit = habit;

        return args;
    }

    @ReceiverScope
    @Component(dependencies = AppComponent.class)
    interface ReceiverComponent
    {
        WidgetController getWidgetController();
    }

    private class Arguments
    {
        int action;

        Habit habit;
    }
}
