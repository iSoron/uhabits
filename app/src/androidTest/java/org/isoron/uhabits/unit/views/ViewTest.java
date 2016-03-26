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

package org.isoron.uhabits.unit.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.view.View;

import org.isoron.uhabits.helpers.DialogHelper;
import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.fail;

public class ViewTest
{
    protected static final int SIMILARITY_CUTOFF = 6000;

    protected Context testContext;
    protected Context targetContext;

    @Before
    public void setup()
    {
        targetContext = InstrumentationRegistry.getTargetContext();
        testContext = InstrumentationRegistry.getContext();
    }

    protected void measureView(int width, int height, View view)
    {
        int specWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int specHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        view.measure(specWidth, specHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    protected void assertRenders(View view, String expectedImagePath) throws IOException
    {
        StringBuilder errorMessage = new StringBuilder();

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap actualBitmap = view.getDrawingCache();
        Bitmap expectedBitmap = getBitmapFromAssets(expectedImagePath);
        Bitmap scaledExpectedBitmap = Bitmap.createScaledBitmap(expectedBitmap,
                actualBitmap.getWidth(), actualBitmap.getHeight(), false);

        boolean similarEnough = true;
        long distance;

        if ((distance = compareHistograms(getHistogram(actualBitmap), getHistogram(
                scaledExpectedBitmap))) > SIMILARITY_CUTOFF)
        {
            similarEnough = false;
            errorMessage.append(String.format(
                    "Rendered image has wrong histogram (distance=%d). ",
                    distance));
        }

        if(!similarEnough)
        {
            String path = saveBitmap(expectedImagePath, actualBitmap);
            errorMessage.append(String.format("Actual rendered image " + "saved to %s", path));
            fail(errorMessage.toString());
        }

        actualBitmap.recycle();
        expectedBitmap.recycle();
        scaledExpectedBitmap.recycle();
    }

    private Bitmap getBitmapFromAssets(String path) throws IOException
    {
        InputStream stream = testContext.getAssets().open(path);
        return BitmapFactory.decodeStream(stream);
    }

    private String saveBitmap(String filename, Bitmap bitmap)
            throws IOException
    {
        String absolutePath = String.format("%s/Failed/%s", targetContext.getExternalCacheDir(),
                filename.replaceAll("\\.png$", ".actual.png"));
        new File(absolutePath).getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(absolutePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        return absolutePath;
    }

    private int[][] getHistogram(Bitmap bitmap)
    {
        int histogram[][] = new int[4][256];

        for(int x = 0; x < bitmap.getWidth(); x++)
        {
            for(int y = 0; y < bitmap.getHeight(); y++)
            {
                int color = bitmap.getPixel(x, y);
                int[] argb = new int[]{
                        (color >> 24) & 0xff, //alpha
                        (color >> 16) & 0xff, //red
                        (color >>  8) & 0xff, //green
                        (color      ) & 0xff  //blue
                };

                histogram[0][argb[0]]++;
                histogram[1][argb[1]]++;
                histogram[2][argb[2]]++;
                histogram[3][argb[3]]++;
            }
        }

        return histogram;
    }

    private long compareHistograms(int[][] actualHistogram, int[][] expectedHistogram)
    {
        long distance = 0;

        for(int i = 0; i < 255; i ++)
        {
            distance += Math.abs(actualHistogram[0][i] - expectedHistogram[0][i]);
            distance += Math.abs(actualHistogram[1][i] - expectedHistogram[1][i]);
            distance += Math.abs(actualHistogram[2][i] - expectedHistogram[2][i]);
            distance += Math.abs(actualHistogram[3][i] - expectedHistogram[3][i]);
        }

        return distance;
    }

    protected int dpToPixels(int dp)
    {
        return (int) DialogHelper.dpToPixels(targetContext, dp);
    }
}
