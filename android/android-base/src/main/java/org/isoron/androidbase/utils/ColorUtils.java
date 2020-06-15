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

import android.graphics.*;

public abstract class ColorUtils
{
    public static int mixColors(int color1, int color2, float amount)
    {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        int a = getComponent(color1, color2, amount, ALPHA_CHANNEL);
        int r = getComponent(color1, color2, amount, RED_CHANNEL);
        int g = getComponent(color1, color2, amount, GREEN_CHANNEL);
        int b = getComponent(color1, color2, amount, BLUE_CHANNEL);

        return a | r | g  | b ;
    }

    public static int setAlpha(int color, float newAlpha)
    {
        int intAlpha = (int) (newAlpha * 255);
        return Color.argb(intAlpha, Color.red(color), Color.green(color),
            Color.blue(color));
    }

    public static int setMinValue(int color, float newValue)
    {
        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.max(hsv[2], newValue);
        return Color.HSVToColor(hsv);
    }

    private static int getComponent(int color1, int color2, float amount, byte channel) {
        final float inverseAmount = 1.0f - amount;

        return (((int) (((float) (color1 >> channel & 0xff) * amount) +
                ((float) (color2 >> channel & 0xff) * inverseAmount))) & 0xff) << channel;
    }

}