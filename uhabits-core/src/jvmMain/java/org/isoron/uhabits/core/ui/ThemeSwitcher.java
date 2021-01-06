/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

import androidx.annotation.*;

import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.views.*;

public abstract class ThemeSwitcher
{
    public static final int THEME_DARK = 1;

    public static final int THEME_LIGHT = 2;

    public static final int THEME_AUTOMATIC = 0;

    private final Preferences preferences;

    public ThemeSwitcher(@NonNull Preferences preferences)
    {
        this.preferences = preferences;
    }

    public void apply()
    {
        if (isNightMode())
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

    public abstract int getSystemTheme();

    public abstract Theme getCurrentTheme();

    public boolean isNightMode()
    {
        int systemTheme = getSystemTheme();
        int userTheme = preferences.getTheme();

        return (userTheme == THEME_DARK ||
                (systemTheme == THEME_DARK && userTheme == THEME_AUTOMATIC));
    }

    public void toggleNightMode()
    {
        int systemTheme = getSystemTheme();
        int userTheme = preferences.getTheme();

        if(userTheme == THEME_AUTOMATIC)
        {
            if(systemTheme == THEME_LIGHT) preferences.setTheme(THEME_DARK);
            if(systemTheme == THEME_DARK) preferences.setTheme(THEME_LIGHT);
        }
        else if(userTheme == THEME_LIGHT)
        {
            if (systemTheme == THEME_LIGHT) preferences.setTheme(THEME_DARK);
            if (systemTheme == THEME_DARK) preferences.setTheme(THEME_AUTOMATIC);
        }
        else if(userTheme == THEME_DARK)
        {
            if (systemTheme == THEME_LIGHT) preferences.setTheme(THEME_AUTOMATIC);
            if (systemTheme == THEME_DARK) preferences.setTheme(THEME_LIGHT);
        }
    }
}
