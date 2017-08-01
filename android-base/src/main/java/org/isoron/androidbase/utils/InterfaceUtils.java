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

package org.isoron.androidbase.utils;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.support.annotation.*;
import android.support.v4.view.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public abstract class InterfaceUtils
{
    private static Typeface fontAwesome;

    @Nullable
    private static Float fixedResolution = null;

    public static void setFixedResolution(@NonNull Float f)
    {
        fixedResolution = f;
    }

    public static Typeface getFontAwesome(Context context)
    {
        if(fontAwesome == null) fontAwesome =
            Typeface.createFromAsset(context.getAssets(),
                "fontawesome-webfont.ttf");

        return fontAwesome;
    }

    public static float dpToPixels(Context context, float dp)
    {
        if(fixedResolution != null) return dp * fixedResolution;

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static float spToPixels(Context context, float sp)
    {
        if(fixedResolution != null) return sp * fixedResolution;

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static float getDimension(Context context, int id)
    {
        float dim = context.getResources().getDimension(id);
        if (fixedResolution == null) return dim;
        else
        {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float actualDensity = dm.density;
            return dim / actualDensity * fixedResolution;
        }
    }

    public static void setupEditorAction(@NonNull ViewGroup parent,
                                         @NonNull TextView.OnEditorActionListener listener)
    {
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            View child = parent.getChildAt(i);

            if (child instanceof ViewGroup)
                setupEditorAction((ViewGroup) child, listener);

            if (child instanceof TextView)
                ((TextView) child).setOnEditorActionListener(listener);
        }
    }

    public static boolean isLayoutRtl(View view)
    {
        return ViewCompat.getLayoutDirection(view) ==
               ViewCompat.LAYOUT_DIRECTION_RTL;
    }
}
