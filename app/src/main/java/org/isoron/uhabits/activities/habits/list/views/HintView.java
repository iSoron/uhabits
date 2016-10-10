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

package org.isoron.uhabits.activities.habits.list.views;

import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.habits.list.model.HintList;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HintView extends FrameLayout
{
    @BindView(R.id.hintContent)
    TextView hintContent;

    @Nullable
    private HintList hintList;

    public HintView(Context context)
    {
        super(context);
        init();
    }

    public HintView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        showNext();
    }

    /**
     * Sets the list of hints to be shown
     *
     * @param hintList the list of hints to be shown
     */
    public void setHints(@Nullable HintList hintList)
    {
        this.hintList = hintList;
    }

    private void dismiss()
    {
        animate().alpha(0f).setDuration(500).setListener(new DismissAnimator());
    }

    private void init()
    {
        addView(inflate(getContext(), R.layout.list_habits_hint, null));
        ButterKnife.bind(this);

        setVisibility(GONE);
        setClickable(true);
        setOnClickListener(v -> dismiss());

        if (isInEditMode()) initEditMode();
    }

    @SuppressLint("SetTextI18n")
    private void initEditMode()
    {
        String hints[] = {
            "Cats are the most popular pet in the United States: There " +
            "are 88 million pet cats and 74 million dogs.",
            "A cat has been mayor of Talkeetna, Alaska, for 15 years. " +
            "His name is Stubbs.",
            "Cats can’t taste sweetness."
        };

        int k = new Random().nextInt(hints.length);
        hintContent.setText(hints[k]);
        setVisibility(VISIBLE);
        setAlpha(1.0f);
    }

    protected void showNext()
    {
        if (hintList == null) return;
        if (!hintList.shouldShow()) return;

        String hint = hintList.pop();
        if (hint == null) return;

        hintContent.setText(hint);
        requestLayout();

        setAlpha(0.0f);
        setVisibility(View.VISIBLE);
        animate().alpha(1f).setDuration(500);
    }

    private class DismissAnimator extends AnimatorListenerAdapter
    {
        @Override
        public void onAnimationEnd(android.animation.Animator animation)
        {
            setVisibility(View.GONE);
        }
    }
}
