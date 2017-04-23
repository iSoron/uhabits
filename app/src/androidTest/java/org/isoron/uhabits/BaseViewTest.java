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
import android.support.annotation.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.utils.*;
import org.isoron.uhabits.widgets.*;

import java.io.*;
import java.util.*;

import static android.os.Build.VERSION.*;
import static android.view.View.MeasureSpec.*;
import static junit.framework.Assert.*;

public class BaseViewTest extends BaseAndroidTest
{
    double similarityCutoff = 0.0005;

    @Override
    public void setUp()
    {
        super.setUp();
    }

    protected void assertRenders(View view, String expectedImagePath)
        throws IOException
    {
        expectedImagePath = getVersionedPath(expectedImagePath);

        if (view.isLayoutRequested()) measureView(view, view.getMeasuredWidth(),
            view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap expected = null;
        Bitmap actual = view.getDrawingCache();
        try
        {
            expected = getBitmapFromAssets(expectedImagePath);
        }
        catch (Exception e)
        {
            String path = saveBitmap(expectedImagePath, "", actual);
            fail(String.format("Could not open expected image. Actual " +
                               "rendered image saved to %s", path));
        }

        int width = actual.getWidth();
        int height = actual.getHeight();
        Bitmap scaledExpected =
            Bitmap.createScaledBitmap(expected, width, height, true);

        double distance = distance(actual, scaledExpected);
        if (distance > similarityCutoff)
        {
            saveBitmap(expectedImagePath, ".expected", scaledExpected);
            String path = saveBitmap(expectedImagePath, "", actual);
            fail(String.format("Image differs from expected " +
                               "(distance=%f). Actual rendered " +
                               "image saved to %s", distance, path));
        }

        expected.recycle();
        scaledExpected.recycle();
    }

    @NonNull
    protected FrameLayout convertToView(BaseWidget widget,
                                        int width,
                                        int height)
    {
        widget.setDimensions(
            new WidgetDimensions(width, height, width, height));
        FrameLayout view = new FrameLayout(targetContext);
        RemoteViews remoteViews = widget.getPortraitRemoteViews();
        view.addView(remoteViews.apply(targetContext, view));
        measureView(view, width, height);
        return view;
    }

    protected int dpToPixels(int dp)
    {
        return (int) InterfaceUtils.dpToPixels(targetContext, dp);
    }

    protected void measureView(View view, int width, int height)
    {
        int specWidth = makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int specHeight = makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.measure(specWidth, specHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    protected void skipAnimation(View view)
    {
        ViewPropertyAnimator animator = view.animate();
        animator.setDuration(0);
        animator.start();
    }

    private int[] colorToArgb(int c1)
    {
        return new int[]{
            (c1 >> 24) & 0xff, //alpha
            (c1 >> 16) & 0xff, //red
            (c1 >> 8) & 0xff, //green
            (c1) & 0xff  //blue
        };
    }

    private double distance(Bitmap b1, Bitmap b2)
    {
        if (b1.getWidth() != b2.getWidth()) return 1.0;
        if (b1.getHeight() != b2.getHeight()) return 1.0;

        Random random = new Random();

        double distance = 0.0;
        for (int x = 0; x < b1.getWidth(); x++)
        {
            for (int y = 0; y < b1.getHeight(); y++)
            {
                if (random.nextInt(4) != 0) continue;

                int[] argb1 = colorToArgb(b1.getPixel(x, y));
                int[] argb2 = colorToArgb(b2.getPixel(x, y));
                distance += Math.abs(argb1[0] - argb2[0]);
                distance += Math.abs(argb1[1] - argb2[1]);
                distance += Math.abs(argb1[2] - argb2[2]);
                distance += Math.abs(argb1[3] - argb2[3]);
            }
        }

        distance /= (0xff * 16) * b1.getWidth() * b1.getHeight();
        return distance;
    }

    private Bitmap getBitmapFromAssets(String path) throws IOException
    {
        InputStream stream = testContext.getAssets().open(path);
        return BitmapFactory.decodeStream(stream);
    }

    private String getVersionedPath(String path)
    {
        int version = SDK_INT;
        if (version >= 21) version = 21;
        else if (version >= 15) version = 15;

        return String.format("views-v%d/%s", version, path);
    }

    private String saveBitmap(String filename, String suffix, Bitmap bitmap)
        throws IOException
    {
        File dir = FileUtils.getSDCardDir("test-screenshots");
        if (dir == null)
            dir = FileUtils.getFilesDir(targetContext, "test-screenshots");
        if (dir == null) throw new RuntimeException(
            "Could not find suitable dir for screenshots");

        filename = filename.replaceAll("\\.png$", suffix + ".png");
        String absolutePath =
            String.format("%s/%s", dir.getAbsolutePath(), filename);

        File parent = new File(absolutePath).getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new RuntimeException(
            String.format("Could not create dir: %s",
                parent.getAbsolutePath()));

        FileOutputStream out = new FileOutputStream(absolutePath);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        return absolutePath;
    }
}
