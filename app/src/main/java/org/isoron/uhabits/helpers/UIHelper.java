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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.isoron.uhabits.BuildConfig;
import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.Command;

import java.util.Locale;

public abstract class UIHelper
{
    public static final String ISORON_NAMESPACE = "http://isoron.org/android";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    private static Typeface fontAwesome;
    private static Integer fixedTheme;

    public static void setFixedTheme(Integer fixedTheme)
    {
        UIHelper.fixedTheme = fixedTheme;
    }

    public interface OnSavedListener
    {
        void onSaved(Command command, Object savedObject);
    }

    public static Typeface getFontAwesome(Context context)
    {
        if(fontAwesome == null)
            fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");

        return fontAwesome;
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

    public static Integer getColorAttribute(Context context, AttributeSet attrs, String name,
                                          Integer defaultValue)
    {
        int resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0);
        if (resId != 0) return context.getResources().getColor(resId);
        else return defaultValue;
    }

    public static int getIntAttribute(Context context, AttributeSet attrs, String name,
                                      int defaultValue)
    {
        String number = getAttribute(context, attrs, name, null);
        if(number != null) return Integer.parseInt(number);
        else return defaultValue;
    }

    public static boolean getBooleanAttribute(Context context, AttributeSet attrs, String name,
                                      boolean defaultValue)
    {
        String boolText = getAttribute(context, attrs, name, null);
        if(boolText != null) return Boolean.parseBoolean(boolText);
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
        // TODO: Move this to another place, or detect automatically
        String fullyTranslatedLanguages[] = { "ca", "zh", "en", "de", "in", "it", "ko", "pl", "pt",
                "es", "tk", "uk", "ja", "fr", "hr", "sl"};

        final String currentLanguage = Locale.getDefault().getLanguage();

        for(String lang : fullyTranslatedLanguages)
            if(currentLanguage.equals(lang)) return true;

        return false;
    }

    public static float getScreenWidth(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getStyledColor(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        int color = ta.getColor(0, 0);
        ta.recycle();

        return color;
    }

    private static TypedArray getTypedArray(Context context, int attrId)
    {
        int[] attrs = new int[]{ attrId };
        if(fixedTheme != null)
            return context.getTheme().obtainStyledAttributes(fixedTheme, attrs);
        else
            return context.obtainStyledAttributes(attrs);
    }

    public static Drawable getStyledDrawable(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();

        return drawable;
    }

    public static boolean getStyledBoolean(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        boolean bool = ta.getBoolean(0, false);
        ta.recycle();

        return bool;
    }

    public static float getStyledFloat(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        float f = ta.getFloat(0, 0);
        ta.recycle();

        return f;
    }

    static int getStyleResource(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        int resourceId = ta.getResourceId(0, -1);
        ta.recycle();

        return resourceId;
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


    public static void setDefaultScoreInterval(Context context, int position)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt("pref_score_view_interval", position).apply();
    }

    public static int getDefaultScoreInterval(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultScoreInterval = prefs.getInt("pref_score_view_interval", 1);
        if(defaultScoreInterval > 5 || defaultScoreInterval < 0) defaultScoreInterval = 1;

        return defaultScoreInterval;
    }
}
