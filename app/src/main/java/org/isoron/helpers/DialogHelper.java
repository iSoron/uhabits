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

package org.isoron.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.commands.Command;

public abstract class DialogHelper
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

    public static void vibrate(Context context, int duration)
    {
        Vibrator vb = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(duration);
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

    public static String getAttribute(Context context, AttributeSet attrs, String name)
    {
        int resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0);

        if (resId != 0) return context.getResources().getString(resId);
        else return attrs.getAttributeValue(ISORON_NAMESPACE, name);
    }

    public static int getIntAttribute(Context context, AttributeSet attrs, String name)
    {
        String number = getAttribute(context, attrs, name);
        if(number != null) return Integer.parseInt(number);
        else return 0;
    }

    public static float getFloatAttribute(Context context, AttributeSet attrs, String name)
    {
        String number = getAttribute(context, attrs, name);
        if(number != null) return Float.parseFloat(number);
        else return 0;
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
}
