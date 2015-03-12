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

import org.isoron.uhabits.R;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.android.datetimepicker.Utils;

/**
 * View to show what number is selected. This will draw a blue circle over the number, with a blue
 * line coming from the center of the main circle to the edge of the blue selection.
 */
public class RadialSelectorView extends View {
    private static final String TAG = "RadialSelectorView";

    // Alpha level for selected circle.
    private static final int SELECTED_ALPHA = Utils.SELECTED_ALPHA;
    private static final int SELECTED_ALPHA_THEME_DARK = Utils.SELECTED_ALPHA_THEME_DARK;
    // Alpha level for the line.
    private static final int FULL_ALPHA = Utils.FULL_ALPHA;

    private final Paint mPaint = new Paint();

    private boolean mIsInitialized;
    private boolean mDrawValuesReady;

    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private float mInnerNumbersRadiusMultiplier;
    private float mOuterNumbersRadiusMultiplier;
    private float mNumbersRadiusMultiplier;
    private float mSelectionRadiusMultiplier;
    private float mAnimationRadiusMultiplier;
    private boolean mIs24HourMode;
    private boolean mHasInnerCircle;
    private int mSelectionAlpha;

    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;
    private float mTransitionMidRadiusMultiplier;
    private float mTransitionEndRadiusMultiplier;
    private int mLineLength;
    private int mSelectionRadius;
    private InvalidateUpdateListener mInvalidateUpdateListener;

    private int mSelectionDegrees;
    private double mSelectionRadians;
    private boolean mForceDrawDot;

    public RadialSelectorView(Context context) {
        super(context);
        mIsInitialized = false;
    }

    /**
     * Initialize this selector with the state of the picker.
     * @param context Current context.
     * @param is24HourMode Whether the selector is in 24-hour mode, which will tell us
     * whether the circle's center is moved up slightly to make room for the AM/PM circles.
     * @param hasInnerCircle Whether we have both an inner and an outer circle of numbers
     * that may be selected. Should be true for 24-hour mode in the hours circle.
     * @param disappearsOut Whether the numbers' animation will have them disappearing out
     * or disappearing in.
     * @param selectionDegrees The initial degrees to be selected.
     * @param isInnerCircle Whether the initial selection is in the inner or outer circle.
     * Will be ignored when hasInnerCircle is false.
     */
    public void initialize(Context context, boolean is24HourMode, boolean hasInnerCircle,
            boolean disappearsOut, int selectionDegrees, boolean isInnerCircle) {
        if (mIsInitialized) {
            Log.e(TAG, "This RadialSelectorView may only be initialized once.");
            return;
        }

        Resources res = context.getResources();

        int blue = res.getColor(R.color.blue);
        mPaint.setColor(blue);
        mPaint.setAntiAlias(true);
        mSelectionAlpha = SELECTED_ALPHA;

        // Calculate values for the circle radius size.
        mIs24HourMode = is24HourMode;
        if (is24HourMode) {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.circle_radius_multiplier_24HourMode));
        } else {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.circle_radius_multiplier));
            mAmPmCircleRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.ampm_circle_radius_multiplier));
        }

        // Calculate values for the radius size(s) of the numbers circle(s).
        mHasInnerCircle = hasInnerCircle;
        if (hasInnerCircle) {
            mInnerNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_inner));
            mOuterNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_outer));
        } else {
            mNumbersRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.numbers_radius_multiplier_normal));
        }
        mSelectionRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.selection_radius_multiplier));

        // Calculate values for the transition mid-way states.
        mAnimationRadiusMultiplier = 1;
        mTransitionMidRadiusMultiplier = 1f + (0.05f * (disappearsOut? -1 : 1));
        mTransitionEndRadiusMultiplier = 1f + (0.3f * (disappearsOut? 1 : -1));
        mInvalidateUpdateListener = new InvalidateUpdateListener();

        setSelection(selectionDegrees, isInnerCircle, false);
        mIsInitialized = true;
    }

    /* package */ void setTheme(Context context, boolean themeDark) {
        Resources res = context.getResources();
        int color;
        if (themeDark) {
            color = res.getColor(R.color.red);
            mSelectionAlpha = SELECTED_ALPHA_THEME_DARK;
        } else {
            color = res.getColor(R.color.blue);
            mSelectionAlpha = SELECTED_ALPHA;
        }
        mPaint.setColor(color);
    }

    /**
     * Set the selection.
     * @param selectionDegrees The degrees to be selected.
     * @param isInnerCircle Whether the selection should be in the inner circle or outer. Will be
     * ignored if hasInnerCircle was initialized to false.
     * @param forceDrawDot Whether to force the dot in the center of the selection circle to be
     * drawn. If false, the dot will be drawn only when the degrees is not a multiple of 30, i.e.
     * the selection is not on a visible number.
     */
    public void setSelection(int selectionDegrees, boolean isInnerCircle, boolean forceDrawDot) {
        mSelectionDegrees = selectionDegrees;
        mSelectionRadians = selectionDegrees * Math.PI / 180;
        mForceDrawDot = forceDrawDot;

        if (mHasInnerCircle) {
            if (isInnerCircle) {
                mNumbersRadiusMultiplier = mInnerNumbersRadiusMultiplier;
            } else {
                mNumbersRadiusMultiplier = mOuterNumbersRadiusMultiplier;
            }
        }
    }

    /**
     * Allows for smoother animations.
     */
    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    /**
     * Set the multiplier for the radius. Will be used during animations to move in/out.
     */
    public void setAnimationRadiusMultiplier(float animationRadiusMultiplier) {
        mAnimationRadiusMultiplier = animationRadiusMultiplier;
    }

    public int getDegreesFromCoords(float pointX, float pointY, boolean forceLegal,
            final Boolean[] isInnerCircle) {
        if (!mDrawValuesReady) {
            return -1;
        }

        double hypotenuse = Math.sqrt(
                (pointY - mYCenter)*(pointY - mYCenter) +
                (pointX - mXCenter)*(pointX - mXCenter));
        // Check if we're outside the range
        if (mHasInnerCircle) {
            if (forceLegal) {
                // If we're told to force the coordinates to be legal, we'll set the isInnerCircle
                // boolean based based off whichever number the coordinates are closer to.
                int innerNumberRadius = (int) (mCircleRadius * mInnerNumbersRadiusMultiplier);
                int distanceToInnerNumber = (int) Math.abs(hypotenuse - innerNumberRadius);
                int outerNumberRadius = (int) (mCircleRadius * mOuterNumbersRadiusMultiplier);
                int distanceToOuterNumber = (int) Math.abs(hypotenuse - outerNumberRadius);

                isInnerCircle[0] = (distanceToInnerNumber <= distanceToOuterNumber);
            } else {
                // Otherwise, if we're close enough to either number (with the space between the
                // two allotted equally), set the isInnerCircle boolean as the closer one.
                // appropriately, but otherwise return -1.
                int minAllowedHypotenuseForInnerNumber =
                        (int) (mCircleRadius * mInnerNumbersRadiusMultiplier) - mSelectionRadius;
                int maxAllowedHypotenuseForOuterNumber =
                        (int) (mCircleRadius * mOuterNumbersRadiusMultiplier) + mSelectionRadius;
                int halfwayHypotenusePoint = (int) (mCircleRadius *
                        ((mOuterNumbersRadiusMultiplier + mInnerNumbersRadiusMultiplier) / 2));

                if (hypotenuse >= minAllowedHypotenuseForInnerNumber &&
                        hypotenuse <= halfwayHypotenusePoint) {
                    isInnerCircle[0] = true;
                } else if (hypotenuse <= maxAllowedHypotenuseForOuterNumber &&
                        hypotenuse >= halfwayHypotenusePoint) {
                    isInnerCircle[0] = false;
                } else {
                    return -1;
                }
            }
        } else {
            // If there's just one circle, we'll need to return -1 if:
            // we're not told to force the coordinates to be legal, and
            // the coordinates' distance to the number is within the allowed distance.
            if (!forceLegal) {
                int distanceToNumber = (int) Math.abs(hypotenuse - mLineLength);
                // The max allowed distance will be defined as the distance from the center of the
                // number to the edge of the circle.
                int maxAllowedDistance = (int) (mCircleRadius * (1 - mNumbersRadiusMultiplier));
                if (distanceToNumber > maxAllowedDistance) {
                    return -1;
                }
            }
        }


        float opposite = Math.abs(pointY - mYCenter);
        double radians = Math.asin(opposite / hypotenuse);
        int degrees = (int) (radians * 180 / Math.PI);

        // Now we have to translate to the correct quadrant.
        boolean rightSide = (pointX > mXCenter);
        boolean topSide = (pointY < mYCenter);
        if (rightSide && topSide) {
            degrees = 90 - degrees;
        } else if (rightSide && !topSide) {
            degrees = 90 + degrees;
        } else if (!rightSide && !topSide) {
            degrees = 270 - degrees;
        } else if (!rightSide && topSide) {
            degrees = 270 + degrees;
        }
        return degrees;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = (int) (Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier);

            if (!mIs24HourMode) {
                // We'll need to draw the AM/PM circles, so the main circle will need to have
                // a slightly higher center. To keep the entire view centered vertically, we'll
                // have to push it up by half the radius of the AM/PM circles.
                int amPmCircleRadius = (int) (mCircleRadius * mAmPmCircleRadiusMultiplier);
                mYCenter -= amPmCircleRadius / 2;
            }

            mSelectionRadius = (int) (mCircleRadius * mSelectionRadiusMultiplier);

            mDrawValuesReady = true;
        }

        // Calculate the current radius at which to place the selection circle.
        mLineLength = (int) (mCircleRadius * mNumbersRadiusMultiplier * mAnimationRadiusMultiplier);
        int pointX = mXCenter + (int) (mLineLength * Math.sin(mSelectionRadians));
        int pointY = mYCenter - (int) (mLineLength * Math.cos(mSelectionRadians));

        // Draw the selection circle.
        mPaint.setAlpha(mSelectionAlpha);
        canvas.drawCircle(pointX, pointY, mSelectionRadius, mPaint);

        if (mForceDrawDot | mSelectionDegrees % 30 != 0) {
            // We're not on a direct tick (or we've been told to draw the dot anyway).
            mPaint.setAlpha(FULL_ALPHA);
            canvas.drawCircle(pointX, pointY, (mSelectionRadius * 2 / 7), mPaint);
        } else {
            // We're not drawing the dot, so shorten the line to only go as far as the edge of the
            // selection circle.
            int lineLength = mLineLength;
            lineLength -= mSelectionRadius;
            pointX = mXCenter + (int) (lineLength * Math.sin(mSelectionRadians));
            pointY = mYCenter - (int) (lineLength * Math.cos(mSelectionRadians));
        }

        // Draw the line from the center of the circle.
        mPaint.setAlpha(255);
        mPaint.setStrokeWidth(1);
        canvas.drawLine(mXCenter, mYCenter, pointX, pointY, mPaint);
    }

    public ObjectAnimator getDisappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) {
            Log.e(TAG, "RadialSelectorView was not ready for animation.");
            return null;
        }

        Keyframe kf0, kf1, kf2;
        float midwayPoint = 0.2f;
        int duration = 500;

        kf0 = Keyframe.ofFloat(0f, 1);
        kf1 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf2 = Keyframe.ofFloat(1f, mTransitionEndRadiusMultiplier);
        PropertyValuesHolder radiusDisappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2);

        kf0 = Keyframe.ofFloat(0f, 1f);
        kf1 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1);

        ObjectAnimator disappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusDisappear, fadeOut).setDuration(duration);
        disappearAnimator.addUpdateListener(mInvalidateUpdateListener);

        return disappearAnimator;
    }

    public ObjectAnimator getReappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) {
            Log.e(TAG, "RadialSelectorView was not ready for animation.");
            return null;
        }

        Keyframe kf0, kf1, kf2, kf3;
        float midwayPoint = 0.2f;
        int duration = 500;

        // The time points are half of what they would normally be, because this animation is
        // staggered against the disappear so they happen seamlessly. The reappear starts
        // halfway into the disappear.
        float delayMultiplier = 0.25f;
        float transitionDurationMultiplier = 1f;
        float totalDurationMultiplier = transitionDurationMultiplier + delayMultiplier;
        int totalDuration = (int) (duration * totalDurationMultiplier);
        float delayPoint = (delayMultiplier * duration) / totalDuration;
        midwayPoint = 1 - (midwayPoint * (1 - delayPoint));

        kf0 = Keyframe.ofFloat(0f, mTransitionEndRadiusMultiplier);
        kf1 = Keyframe.ofFloat(delayPoint, mTransitionEndRadiusMultiplier);
        kf2 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf3 = Keyframe.ofFloat(1f, 1);
        PropertyValuesHolder radiusReappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2, kf3);

        kf0 = Keyframe.ofFloat(0f, 0f);
        kf1 = Keyframe.ofFloat(delayPoint, 0f);
        kf2 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder fadeIn = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1, kf2);

        ObjectAnimator reappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusReappear, fadeIn).setDuration(totalDuration);
        reappearAnimator.addUpdateListener(mInvalidateUpdateListener);
        return reappearAnimator;
    }

    /**
     * We'll need to invalidate during the animation.
     */
    private class InvalidateUpdateListener implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            RadialSelectorView.this.invalidate();
        }
    }
}
