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

package org.isoron.uhabits.activities.settings;


import android.graphics.Color;
import android.os.*;

import org.isoron.androidbase.activities.*;
import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.*;

/**
 * Activity that allows the user to view and modify the app settings.
 */
public class SettingsActivity extends BaseActivity
{
    private AndroidThemeSwitcher themeSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        HabitsApplicationComponent compoenent = ((HabitsApplication) getApplication()).getComponent();
        themeSwitcher = new AndroidThemeSwitcher(this, compoenent.getPreferences());
        themeSwitcher.apply();

        setContentView(R.layout.settings_activity);
        setupActionBarColor();

        getWindow().setNavigationBarColor((Color.TRANSPARENT));
    }

    private void setupActionBarColor()
    {
        StyledResources res = new StyledResources(this);
        int color = BaseScreen.getDefaultActionBarColor(this);

        if (res.getBoolean(R.attr.useHabitColorAsPrimary))
            color = res.getColor(R.attr.colorPrimary);

        BaseScreen.setupActionBarColor(this, color);
    }

}
