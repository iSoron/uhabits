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

import android.content.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;

public class StyledResources
{
    private static Integer fixedTheme;

    private final Context context;

    public StyledResources(@NonNull Context context)
    {
        this.context = context;
    }

    public static void setFixedTheme(Integer theme)
    {
        fixedTheme = theme;
    }

    public boolean getBoolean(@AttrRes int attrId)
    {
        TypedArray ta = getTypedArray(attrId);
        boolean bool = ta.getBoolean(0, false);
        ta.recycle();

        return bool;
    }

    public int getColor(@AttrRes int attrId)
    {
        TypedArray ta = getTypedArray(attrId);
        int color = ta.getColor(0, 0);
        ta.recycle();

        return color;
    }

    public Drawable getDrawable(@AttrRes int attrId)
    {
        TypedArray ta = getTypedArray(attrId);
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();

        return drawable;
    }

    public float getFloat(@AttrRes int attrId)
    {
        TypedArray ta = getTypedArray(attrId);
        float f = ta.getFloat(0, 0);
        ta.recycle();

        return f;
    }

    public int[] getPalette()
    {
        int resourceId = getStyleResource(R.attr.palette);
        if (resourceId < 0) throw new RuntimeException("resource not found");

        return context.getResources().getIntArray(resourceId);
    }

    int getStyleResource(@AttrRes int attrId)
    {
        TypedArray ta = getTypedArray(attrId);
        int resourceId = ta.getResourceId(0, -1);
        ta.recycle();

        return resourceId;
    }

    private TypedArray getTypedArray(@AttrRes int attrId)
    {
        int[] attrs = new int[]{ attrId };

        if (fixedTheme != null)
            return context.getTheme().obtainStyledAttributes(fixedTheme, attrs);

        return context.obtainStyledAttributes(attrs);
    }
}
