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

package org.isoron.uhabits.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.isoron.uhabits.R;
import org.isoron.uhabits.utils.InterfaceUtils;

public abstract class BaseRootView extends FrameLayout
{
    private final Context context;

    public BaseRootView(Context context)
    {
        super(context);
        this.context = context;
    }

    public BaseRootView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
    }

    public BaseRootView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseRootView(Context context,
                        AttributeSet attrs,
                        int defStyleAttr,
                        int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public boolean getDisplayHomeAsUp()
    {
        return false;
    }

    @Nullable
    public ProgressBar getProgressBar()
    {
        return null;
    }

    @NonNull
    public abstract Toolbar getToolbar();

    public int getToolbarColor()
    {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//        {
//            if (InterfaceUtils.isNightMode()) return;
//            int color = activity.getResources().getColor(R.color.grey_900);
//        }
//        if (!InterfaceUtils.getStyledBoolean(activity, R.attr.useHabitColorAsPrimary)) return;
        return Color.BLACK;
    }

    private void hideFakeToolbarShadow()
    {
        View view = findViewById(R.id.toolbarShadow);
        if (view != null) view.setVisibility(View.GONE);

//        view = findViewById(R.id.headerShadow);
//        if (view != null) view.setVisibility(View.GONE);
    }

    protected void initToolbar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getToolbar().setElevation(InterfaceUtils.dpToPixels(context, 2));

        hideFakeToolbarShadow();
    }
}
