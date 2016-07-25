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

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.preference.*;
import android.util.*;

import org.isoron.uhabits.*;

import java.util.*;

public abstract class InterfaceUtils
{

    // TODO: Move this to another place, or detect automatically
    private static String fullyTranslatedLanguages[] = {
        "ca", "zh", "en", "de", "in", "it", "ko", "pl", "pt", "es", "tk", "uk",
        "ja", "fr", "hr", "sl"
    };

    public static final int THEME_DARK = 1;

    public static final int THEME_LIGHT = 0;

    public static Integer fixedTheme;

    private static Typeface fontAwesome;

    public static void setFixedTheme(Integer fixedTheme)
    {
        InterfaceUtils.fixedTheme = fixedTheme;
    }

    public static Typeface getFontAwesome(Context context)
    {
        if(fontAwesome == null)
            fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");

        return fontAwesome;
    }

    public static float dpToPixels(Context context, float dp)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static float spToPixels(Context context, float sp)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static boolean isLocaleFullyTranslated()
    {
        final String currentLanguage = Locale.getDefault().getLanguage();

        for(String lang : fullyTranslatedLanguages)
            if(currentLanguage.equals(lang)) return true;

        return false;
    }

    public static void applyCurrentTheme(Activity activity)
    {
        switch(getCurrentTheme())
        {
            case THEME_DARK:
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean pureBlackEnabled = prefs.getBoolean("pref_pure_black", false);

                if(pureBlackEnabled)
                    activity.setTheme(R.style.AppBaseThemeDark_PureBlack);
                else
                    activity.setTheme(R.style.AppBaseThemeDark);

                break;
            }

            case THEME_LIGHT:
            default:
                activity.setTheme(R.style.AppBaseTheme);
                break;
        }
    }

    private static int getCurrentTheme()
    {
        Context appContext = HabitsApplication.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        return prefs.getInt("pref_theme", THEME_LIGHT);
    }

    public static void setCurrentTheme(int theme)
    {
        Context appContext = HabitsApplication.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        prefs.edit().putInt("pref_theme", theme).apply();
    }

    public static boolean isNightMode()
    {
        return getCurrentTheme() == THEME_DARK;
    }
}
