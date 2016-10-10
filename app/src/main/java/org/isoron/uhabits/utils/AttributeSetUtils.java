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
import android.support.annotation.*;
import android.support.annotation.Nullable;
import android.util.*;

import org.jetbrains.annotations.*;

public class AttributeSetUtils
{
    public static final String ISORON_NAMESPACE = "http://isoron.org/android";

    @Nullable
    public static String getAttribute(@NonNull Context context,
                                      @NonNull AttributeSet attrs,
                                      @NonNull String name,
                                      @Nullable String defaultValue)
    {
        int resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0);
        if (resId != 0) return context.getResources().getString(resId);

        String value = attrs.getAttributeValue(ISORON_NAMESPACE, name);
        if (value != null) return value;
        else return defaultValue;
    }

    public static boolean getBooleanAttribute(@NonNull Context context,
                                              @NonNull AttributeSet attrs,
                                              @NonNull String name,
                                              boolean defaultValue)
    {
        String boolText = getAttribute(context, attrs, name, null);
        if (boolText != null) return Boolean.parseBoolean(boolText);
        else return defaultValue;
    }

    @Contract("_,_,_,!null -> !null")
    public static Integer getColorAttribute(@NonNull Context context,
                                            @NonNull AttributeSet attrs,
                                            @NonNull String name,
                                            @Nullable Integer defaultValue)
    {
        int resId = attrs.getAttributeResourceValue(ISORON_NAMESPACE, name, 0);
        if (resId != 0) return context.getResources().getColor(resId);
        else return defaultValue;
    }

    public static float getFloatAttribute(@NonNull Context context,
                                          @NonNull AttributeSet attrs,
                                          @NonNull String name,
                                          float defaultValue)
    {
        String number = getAttribute(context, attrs, name, null);
        if (number != null) return Float.parseFloat(number);
        else return defaultValue;
    }
}
