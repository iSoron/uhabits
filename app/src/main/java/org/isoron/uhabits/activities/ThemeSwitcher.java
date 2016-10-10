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

package org.isoron.uhabits.activities;

import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.preferences.*;

import javax.inject.*;

@ActivityScope
public class ThemeSwitcher
{
    public static final int THEME_DARK = 1;

    public static final int THEME_LIGHT = 0;

    @NonNull
    private final BaseActivity activity;

    private Preferences preferences;

    @Inject
    public ThemeSwitcher(@NonNull BaseActivity activity,
                         @NonNull Preferences preferences)
    {
        this.activity = activity;
        this.preferences = preferences;
    }

    public void apply()
    {
        switch (getTheme())
        {
            case THEME_DARK:
                applyDarkTheme();
                break;

            case THEME_LIGHT:
            default:
                applyLightTheme();
                break;
        }
    }

    public boolean isNightMode()
    {
        return getTheme() == THEME_DARK;
    }

    public void refreshTheme()
    {

    }

    public void toggleNightMode()
    {
        if (isNightMode()) setTheme(THEME_LIGHT);
        else setTheme(THEME_DARK);
    }

    private void applyDarkTheme()
    {
        if (preferences.isPureBlackEnabled())
            activity.setTheme(R.style.AppBaseThemeDark_PureBlack);
        else activity.setTheme(R.style.AppBaseThemeDark);
    }

    private void applyLightTheme()
    {
        activity.setTheme(R.style.AppBaseTheme);
    }

    private int getTheme()
    {
        return preferences.getTheme();
    }

    public void setTheme(int theme)
    {
        preferences.setTheme(theme);
    }
}
