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

package org.isoron.uhabits;

import android.graphics.*;
import android.os.*;
import android.view.*;

import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.habits.show.views.*;
import org.isoron.uhabits.utils.*;

import java.io.*;

import static junit.framework.Assert.*;

public class BaseViewTest extends BaseAndroidTest
{
    protected static final double DEFAULT_SIMILARITY_CUTOFF = 0.09;
    public static final int HISTOGRAM_BIN_SIZE = 8;
    private double similarityCutoff;

    @Override
    public void setUp()
    {
        super.setUp();
        similarityCutoff = DEFAULT_SIMILARITY_CUTOFF;
    }

    protected void setSimilarityCutoff(double similarityCutoff)
    {
        this.similarityCutoff = similarityCutoff;
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
        expectedImagePath = getVersionedViewAssetPath(expectedImagePath);

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap actual = view.getDrawingCache();
        Bitmap expected = getBitmapFromAssets(expectedImagePath);

        int width = actual.getWidth();
        int height = actual.getHeight();
        Bitmap scaledExpected = Bitmap.createScaledBitmap(expected, width, height, true);

        double distance;
        boolean similarEnough = true;

        if ((distance = compareHistograms(getHistogram(actual), getHistogram(scaledExpected))) >
                similarityCutoff)
        {
            similarEnough = false;
            errorMessage.append(String.format(
                    "Rendered image has wrong histogram (distance=%f). ",
                    distance));
        }

        if(!similarEnough)
        {
            saveBitmap(expectedImagePath, ".expected", scaledExpected);
            String path = saveBitmap(expectedImagePath, "", actual);
            errorMessage.append(String.format("Actual rendered image " + "saved to %s", path));
            fail(errorMessage.toString());
        }

        actual.recycle();
        expected.recycle();
        scaledExpected.recycle();
    }

    private Bitmap getBitmapFromAssets(String path) throws IOException
    {
        InputStream stream = testContext.getAssets().open(path);
        return BitmapFactory.decodeStream(stream);
    }

    private String getVersionedViewAssetPath(String path)
    {
        String result = null;

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            try
            {
                String vpath = "views-v21/" + path;
                testContext.getAssets().open(vpath);
                result = vpath;
            }
            catch (IOException e)
            {
                // ignored
            }
        }

        if(result == null)
            result = "views/" + path;

        return result;
    }

    private String saveBitmap(String filename, String suffix, Bitmap bitmap)
            throws IOException
    {
        File dir = FileUtils.getSDCardDir("test-screenshots");
        if(dir == null) dir = FileUtils.getFilesDir("test-screenshots");
        if(dir == null) throw new RuntimeException("Could not find suitable dir for screenshots");

        filename = filename.replaceAll("\\.png$", suffix + ".png");
        String absolutePath = String.format("%s/%s", dir.getAbsolutePath(), filename);

        File parent = new File(absolutePath).getParentFile();
        if(!parent.exists() && !parent.mkdirs())
            throw new RuntimeException(String.format("Could not create dir: %s",
                    parent.getAbsolutePath()));

        FileOutputStream out = new FileOutputStream(absolutePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        return absolutePath;
    }

    private int[][] getHistogram(Bitmap bitmap)
    {
        int histogram[][] = new int[4][256 / HISTOGRAM_BIN_SIZE];

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

                histogram[0][argb[0] / HISTOGRAM_BIN_SIZE]++;
                histogram[1][argb[1] / HISTOGRAM_BIN_SIZE]++;
                histogram[2][argb[2] / HISTOGRAM_BIN_SIZE]++;
                histogram[3][argb[3] / HISTOGRAM_BIN_SIZE]++;
            }
        }

        return histogram;
    }

    private double compareHistograms(int[][] actualHistogram, int[][] expectedHistogram)
    {
        long diff = 0;
        long total = 0;

        for(int i = 0; i < 256 / HISTOGRAM_BIN_SIZE; i ++)
        {
            diff += Math.abs(actualHistogram[0][i] - expectedHistogram[0][i]);
            diff += Math.abs(actualHistogram[1][i] - expectedHistogram[1][i]);
            diff += Math.abs(actualHistogram[2][i] - expectedHistogram[2][i]);
            diff += Math.abs(actualHistogram[3][i] - expectedHistogram[3][i]);

            total += actualHistogram[0][i];
            total += actualHistogram[1][i];
            total += actualHistogram[2][i];
            total += actualHistogram[3][i];
        }

        return (double) diff / total / 2;
    }

    protected int dpToPixels(int dp)
    {
        return (int) InterfaceUtils.dpToPixels(targetContext, dp);
    }

    protected void tap(GestureDetector.OnGestureListener view, int x, int y) throws InterruptedException
    {
        long now = SystemClock.uptimeMillis();
        MotionEvent e = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, dpToPixels(x),
                dpToPixels(y), 0);
        view.onSingleTapUp(e);
        e.recycle();
    }

    protected void refreshData(final HabitDataView view)
    {
        new BaseTask()
        {
            @Override
            protected void doInBackground()
            {
                view.refreshData();
            }
        }.execute();

        try
        {
            waitForAsyncTasks();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Time out");
        }
    }
}
