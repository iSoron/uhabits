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

package org.isoron.uhabits.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Debug;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.commands.Command;

import java.util.Locale;

public abstract class UIHelper
{

    public static final String ISORON_NAMESPACE = "http://isoron.org/android";
    private static Typeface fontawesome;

    public interface OnSavedListener
    {
        void onSaved(Command command, Object savedObject);
    }

    public static void showSoftKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void incrementLaunchCount(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int count = prefs.getInt("launch_count", 0);
        prefs.edit().putInt("launch_count", count + 1).apply();
    }

    public static void updateLastAppVersion(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt("last_version", BuildConfig.VERSION_CODE).apply();
    }

    public static int getLaunchCount(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("launch_count", 0);
    }

    public static String getAttribute(Context context, AttributeSet attrs, String name,
                                      String defaultValue)
    {
        int resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0);
        if (resId != 0) return context.getResources().getString(resId);

        String value = attrs.getAttributeValue(ISORON_NAMESPACE, name);
        if(value != null) return value;
        else return defaultValue;
    }

    public static int getIntAttribute(Context context, AttributeSet attrs, String name,
                                      int defaultValue)
    {
        String number = getAttribute(context, attrs, name, null);
        if(number != null) return Integer.parseInt(number);
        else return defaultValue;
    }

    public static float getFloatAttribute(Context context, AttributeSet attrs, String name,
                                          float defaultValue)
    {
        String number = getAttribute(context, attrs, name, null);
        if(number != null) return Float.parseFloat(number);
        else return defaultValue;
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

    /**
     * Throws a runtime exception if called from the main thread. Useful to make sure that
     * slow methods never accidentally slow the application down.
     *
     * @throws RuntimeException when run from main thread
     */
    public static void throwIfMainThread() throws RuntimeException
    {
        Looper looper = Looper.myLooper();
        if(looper == null) return;

        if(looper == Looper.getMainLooper())
            throw new RuntimeException("This method should never be called from the main thread");
    }

    public static void startTracing()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            throw new UnsupportedOperationException();
        }
        else
        {
            Debug.startMethodTracingSampling("Android/data/org.isoron.uhabits/perf",
                    32 * 1024 * 1024, 100);
        }
    }

    public static void stopTracing()
    {
        Debug.stopMethodTracing();
    }

    public static boolean isLocaleFullyTranslated()
    {
        String fullyTranslatedLanguages[] = { "en", "ar", "cs", "de", "it", "ja", "ko", "po", "pl",
                "pt", "ru", "sv", "zh", "es" };

        final String currentLanguage = Locale.getDefault().getLanguage();

        Log.d("UIHelper", String.format("lang=%s", currentLanguage));

        for(String lang : fullyTranslatedLanguages)
            if(currentLanguage.equals(lang)) return true;

        return false;
    }
}
