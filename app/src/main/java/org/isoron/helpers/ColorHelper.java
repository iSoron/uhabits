package org.isoron.helpers;

import android.graphics.Color;

public class ColorHelper
{
    public static final int[] palette = { Color.parseColor("#900000"),
            Color.parseColor("#c54100"), Color.parseColor("#c0ab00"),
            Color.parseColor("#8db600"), Color.parseColor("#117209"),
            Color.parseColor("#06965b"), Color.parseColor("#069a95"),
            Color.parseColor("#114896"), Color.parseColor("#501394"),
            Color.parseColor("#872086"), Color.parseColor("#c31764"),
            Color.parseColor("#303030"), Color.parseColor("#aaaaaa") };

    public static int mixColors(int color1, int color2, float amount)
	{
		final byte ALPHA_CHANNEL = 24;
		final byte RED_CHANNEL = 16;
		final byte GREEN_CHANNEL = 8;
		final byte BLUE_CHANNEL = 0;

		final float inverseAmount = 1.0f - amount;

		int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
				((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
		int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
				((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
		int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
				((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
		int b = ((int) (((float) (color1 & 0xff) * amount) +
				((float) (color2 & 0xff) * inverseAmount))) & 0xff;

		return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
	}
}