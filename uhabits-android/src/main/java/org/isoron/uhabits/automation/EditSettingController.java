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

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;

import static org.isoron.uhabits.automation.FireSettingReceiver.*;

public class EditSettingController
{
    @NonNull
    private final Activity activity;

    public EditSettingController(@NonNull Activity activity)
    {
        this.activity = activity;
    }

    public void onSave(Habit habit, int action)
    {
        if (habit.getId() == null) return;

        String actionName = getActionName(action);
        String blurb = String.format("%s: %s", actionName, habit.getName());

        Bundle bundle = new Bundle();
        bundle.putInt("action", action);
        bundle.putLong("habit", habit.getId());

        Intent intent = new Intent();
        intent.putExtra(EXTRA_STRING_BLURB, blurb);
        intent.putExtra(EXTRA_BUNDLE, bundle);

        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    private String getActionName(int action)
    {
        switch (action)
        {
            case ACTION_CHECK:
                return activity.getString(R.string.check);

            case ACTION_UNCHECK:
                return activity.getString(R.string.uncheck);

            case ACTION_TOGGLE:
                return activity.getString(R.string.toggle);

            default:
                return "???";
        }
    }
}
