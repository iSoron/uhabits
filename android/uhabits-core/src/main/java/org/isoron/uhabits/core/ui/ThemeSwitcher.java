/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.ui;

import android.support.annotation.*;

import org.isoron.uhabits.core.preferences.*;

public abstract class ThemeSwitcher
{
    public static final int THEME_DARK = 1;

    public static final int THEME_LIGHT = 0;

    private final Preferences preferences;

    public ThemeSwitcher(@NonNull Preferences preferences)
    {
        this.preferences = preferences;
    }

    public void apply()
    {
        if (preferences.getTheme() == THEME_DARK)
        {
            if (preferences.isPureBlackEnabled()) applyPureBlackTheme();
            else applyDarkTheme();
        }
        else
        {
            applyLightTheme();
        }
    }

    public abstract void applyDarkTheme();

    public abstract void applyLightTheme();

    public abstract void applyPureBlackTheme();

    public boolean isNightMode()
    {
        return preferences.getTheme() == THEME_DARK;
    }

    public void setTheme(int theme)
    {
        preferences.setTheme(theme);
    }

    public void toggleNightMode()
    {
        if (isNightMode()) setTheme(THEME_LIGHT);
        else setTheme(THEME_DARK);
    }
}
