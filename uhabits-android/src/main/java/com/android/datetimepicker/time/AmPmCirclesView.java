/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.datetimepicker.time;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.util.*;
import android.view.*;

import com.android.*;
import com.android.datetimepicker.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.StyledResources;

import java.text.*;

/**
 * Draw the two smaller AM and PM circles next to where the larger circle will be.
 */
public class AmPmCirclesView extends View {
    private static final String TAG = "AmPmCirclesView";

    // Alpha level for selected circle.
    private static final int SELECTED_ALPHA = Utils.SELECTED_ALPHA;
    private static final int SELECTED_ALPHA_THEME_DARK = Utils.SELECTED_ALPHA_THEME_DARK;

    private final Paint mPaint = new Paint();
    private int mSelectedAlpha;
    private int mUnselectedColor;
    protected int mAmPmTextColor = Color.WHITE;
    protected int mSelectedColor = Color.BLUE;
    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private String mAmText;
    private String mPmText;
    private boolean mIsInitialized;

    private static final int AM = TimePickerDialog.AM;
    private static final int PM = TimePickerDialog.PM;

    private boolean mDrawValuesReady;
    private int mAmPmCircleRadius;
    private int mAmXCenter;
    private int mPmXCenter;
    private int mAmPmYCenter;
    private int mAmOrPm;
    private int mAmOrPmPressed;

    public AmPmCirclesView(Context context) {
        super(context);
        mIsInitialized = false;
    }

    public void initialize(Context context, int amOrPm) {
        if (mIsInitialized) {
            Log.e(TAG, "AmPmCirclesView may only be initialized once.");
            return;
        }

        StyledResources styledResources = new StyledResources(context);
        mUnselectedColor = styledResources.getColor(R.attr.contrast0);

        Resources res = context.getResources();
        mSelectedAlpha = SELECTED_ALPHA;
        String typefaceFamily = res.getString(R.string.sans_serif);
        Typeface tf = Typeface.create(typefaceFamily, Typeface.NORMAL);
        mPaint.setTypeface(tf);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);

        mCircleRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.circle_radius_multiplier));
        mAmPmCircleRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.ampm_circle_radius_multiplier));
        String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
        mAmText = amPmTexts[0];
        mPmText = amPmTexts[1];

        setAmOrPm(amOrPm);
        mAmOrPmPressed = -1;

        mIsInitialized = true;
    }

    public void setAmOrPm(int amOrPm) {
        mAmOrPm = amOrPm;
    }

    public void setAmOrPmPressed(int amOrPmPressed) {
        mAmOrPmPressed = amOrPmPressed;
    }

    /**
     * Calculate whether the coordinates are touching the AM or PM circle.
     */
    public int getIsTouchingAmOrPm(float xCoord, float yCoord) {
        if (!mDrawValuesReady) {
            return -1;
        }

        int squaredYDistance = (int) ((yCoord - mAmPmYCenter)*(yCoord - mAmPmYCenter));

        int distanceToAmCenter =
                (int) Math.sqrt((xCoord - mAmXCenter)*(xCoord - mAmXCenter) + squaredYDistance);
        if (distanceToAmCenter <= mAmPmCircleRadius) {
            return AM;
        }

        int distanceToPmCenter =
                (int) Math.sqrt((xCoord - mPmXCenter)*(xCoord - mPmXCenter) + squaredYDistance);
        if (distanceToPmCenter <= mAmPmCircleRadius) {
            return PM;
        }

        // Neither was close enough.
        return -1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            int layoutXCenter = getWidth() / 2;
            int layoutYCenter = getHeight() / 2;
            int circleRadius =
                    (int) (Math.min(layoutXCenter, layoutYCenter) * mCircleRadiusMultiplier);
            mAmPmCircleRadius = (int) (circleRadius * mAmPmCircleRadiusMultiplier);
            int textSize = mAmPmCircleRadius * 3 / 4;
            mPaint.setTextSize(textSize);

            // Line up the vertical center of the AM/PM circles with the bottom of the main circle.
            mAmPmYCenter = layoutYCenter - mAmPmCircleRadius / 2 + circleRadius;
            // Line up the horizontal edges of the AM/PM circles with the horizontal edges
            // of the main circle.
            mAmXCenter = layoutXCenter - circleRadius + mAmPmCircleRadius;
            mPmXCenter = layoutXCenter + circleRadius - mAmPmCircleRadius;

            mDrawValuesReady = true;
        }

        // We'll need to draw either a lighter blue (for selection), a darker blue (for touching)
        // or white (for not selected).
        int amColor = mUnselectedColor;
        int amAlpha = 255;
        int pmColor = mUnselectedColor;
        int pmAlpha = 255;
        if (mAmOrPm == AM) {
            amColor = mSelectedColor;
            amAlpha = mSelectedAlpha;
        } else if (mAmOrPm == PM) {
            pmColor = mSelectedColor;
            pmAlpha = mSelectedAlpha;
        }
        if (mAmOrPmPressed == AM) {
            amColor = mSelectedColor;
            amAlpha = mSelectedAlpha;
        } else if (mAmOrPmPressed == PM) {
            pmColor = mSelectedColor;
            pmAlpha = mSelectedAlpha;
        }

        // Draw the two circles.
        mPaint.setColor(amColor);
        mPaint.setAlpha(amAlpha);
        canvas.drawCircle(mAmXCenter, mAmPmYCenter, mAmPmCircleRadius, mPaint);
        mPaint.setColor(pmColor);
        mPaint.setAlpha(pmAlpha);
        canvas.drawCircle(mPmXCenter, mAmPmYCenter, mAmPmCircleRadius, mPaint);

        // Draw the AM/PM texts on top.
        mPaint.setColor(mAmPmTextColor);
        int textYCenter = mAmPmYCenter - (int) (mPaint.descent() + mPaint.ascent()) / 2;
        canvas.drawText(mAmText, mAmXCenter, textYCenter, mPaint);
        canvas.drawText(mPmText, mPmXCenter, textYCenter, mPaint);
    }
}
