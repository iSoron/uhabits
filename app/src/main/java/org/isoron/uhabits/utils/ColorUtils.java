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

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public final class ColorUtils
{
    private static final byte ALPHA_CHANNEL = 24;
    private static final byte RED_CHANNEL = 16;
    private static final byte GREEN_CHANNEL = 8;
    private static final byte BLUE_CHANNEL = 0;
    public static final String INVALID_COLOR_MESSAGE = "Invalid color: %d. Returning default.";
    public static final String COLOR_HELPER_TAG = "ColorHelper";

    private ColorUtils() {
        throw new IllegalAccessError("Instantiating utility class");
    }

    public  enum  CSVPaletteEnum{
        RED(0,"#D32F2F"),
        ORANGE(1,"#E64A19"),
        YELLOW(2,"#F9A825"),
        LIGHT_GREEN(3,"#AFB42B"),
        DARK_GREEN(4,"#388E3C"),
        TEAL(5,"#00897B"),
        CYAN(6,"#00ACC1"),
        BLUE(7,"#039BE5"),
        DEEP_PURPLE(8,"#5E35B1"),
        PURPLE(9,"#8E24AA"),
        PINK(10,"#D81B60"),
        DARK_GREY(11,"#303030"),
        LIGHT_GREY(12,"#aaaaaa");

        private final int colourIndex;
        private final  String colourCode;
        private final static Map<Integer,CSVPaletteEnum> colourIndexValues=new HashMap<>();
        private  final static Map<String,CSVPaletteEnum> colourCodeValues=new HashMap<>();

        static {
            for (CSVPaletteEnum en : CSVPaletteEnum.values()){
                colourIndexValues.put(en.getColourIndex(),en);
                colourCodeValues.put(en.getColourCode(),en);
            }
        }

        CSVPaletteEnum(int colourIndex, String colourCode) {
            this.colourIndex = colourIndex;
            this.colourCode = colourCode;
        }

        public static CSVPaletteEnum valueOfIndex(int value) {
            final CSVPaletteEnum val = colourIndexValues.get(value);
            return val ;
        }

        public static CSVPaletteEnum valueOfColour(String value) {
            final CSVPaletteEnum val = colourCodeValues.get(value);
            return val ;
        }

        public int getColourIndex() {
            return colourIndex;
        }

        public String getColourCode() {
            return colourCode;
        }

        public static Map<Integer, CSVPaletteEnum> getColourIndexValues() {
            return colourIndexValues;
        }

        public static Map<String, CSVPaletteEnum> getColourCodeValues() {
            return colourCodeValues;
        }
    }


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
        return Color.parseColor(CSVPaletteEnum.valueOfIndex(index).getColourCode());
    }

    public static int getColor(Context context, int paletteColor)
    {
        if (context == null)
            throw new IllegalArgumentException("Context is null");

        StyledResources res = new StyledResources(context);
        int palette[] = res.getPalette();
        if (paletteColor < 0 || paletteColor >= palette.length)
        {
            Log.w(COLOR_HELPER_TAG,
                String.format(INVALID_COLOR_MESSAGE,
                    paletteColor));
            paletteColor = 0;
        }

        return palette[paletteColor];
    }

    public static int mixColors(int color1, int color2, float amount)
    {


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