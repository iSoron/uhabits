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
import android.graphics.*;
import android.util.*;

public abstract class ColorUtils
{
    public static String CSV_PALETTE[] = {
        "#D32F2F", //  0 red
        "#E64A19", //  1 deep orange
        "#F57C00", //  2 orange
        "#FF8F00", //  3 amber
        "#F9A825", //  4 yellow
        "#AFB42B", //  5 lime
        "#7CB342", //  6 light green
        "#388E3C", //  7 green
        "#00897B", //  8 teal
        "#00ACC1", //  9 cyan
        "#039BE5", // 10 light blue
        "#1976D2", // 11 blue
        "#303F9F", // 12 indigo
        "#5E35B1", // 13 deep purple
        "#8E24AA", // 14 purple
        "#D81B60", // 15 pink
        "#5D4037", // 16 brown
        "#303030", // 17 dark grey
        "#757575", // 18 grey
        "#aaaaaa"  // 19 light grey
    };

    public static int colorToPaletteIndex(Context context, int color)
    {
        StyledResources res = new StyledResources(context);
        int[] palette = res.getPalette();

        for (int k = 0; k < palette.length; k++)
            if (palette[k] == color) return k;

        return -1;
    }

    public static int getAndroidTestColor(int index)
    {
        int palette[] = {
            Color.parseColor("#D32F2F"), //  0 red
            Color.parseColor("#E64A19"), //  1 deep orange
            Color.parseColor("#F57C00"), //  2 orange
            Color.parseColor("#FF8F00"), //  3 amber
            Color.parseColor("#F9A825"), //  4 yellow
            Color.parseColor("#AFB42B"), //  5 lime
            Color.parseColor("#7CB342"), //  6 light green
            Color.parseColor("#388E3C"), //  7 green
            Color.parseColor("#00897B"), //  8 teal
            Color.parseColor("#00ACC1"), //  9 cyan
            Color.parseColor("#039BE5"), // 10 light blue
            Color.parseColor("#1976D2"), // 11 blue
            Color.parseColor("#303F9F"), // 12 indigo
            Color.parseColor("#5E35B1"), // 13 deep purple
            Color.parseColor("#8E24AA"), // 14 purple
            Color.parseColor("#D81B60"), // 15 pink
            Color.parseColor("#5D4037"), // 16 brown
            Color.parseColor("#303030"), // 17 dark grey
            Color.parseColor("#757575"), // 18 grey
            Color.parseColor("#aaaaaa")  // 19 light grey
        };

        return palette[index];
    }

    public static int getColor(Context context, int paletteColor)
    {
        if (context == null)
            throw new IllegalArgumentException("Context is null");

        StyledResources res = new StyledResources(context);
        int palette[] = res.getPalette();
        if (paletteColor < 0 || paletteColor >= palette.length)
        {
            Log.w("ColorHelper",
                String.format("Invalid color: %d. Returning default.",
                    paletteColor));
            paletteColor = 0;
        }

        return palette[paletteColor];
    }

    public static int mixColors(int color1, int color2, float amount)
    {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
                        ((float) (color2 >> ALPHA_CHANNEL & 0xff) *
                         inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
                        ((float) (color2 >> RED_CHANNEL & 0xff) *
                         inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
                        ((float) (color2 >> GREEN_CHANNEL & 0xff) *
                         inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) +
                        ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL |
               b << BLUE_CHANNEL;
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

}