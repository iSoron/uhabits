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

package org.isoron.uhabits.activities;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

public abstract class BaseRootView extends FrameLayout
{
    private final Context context;

    public BaseRootView(Context context)
    {
        super(context);
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
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) &&
            !InterfaceUtils.isNightMode())
        {
            return getContext().getResources().getColor(R.color.grey_900);
        }

        StyledResources res = new StyledResources(getContext());
        return res.getColor(R.attr.colorPrimary);
    }

    protected void initToolbar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getToolbar().setElevation(InterfaceUtils.dpToPixels(context, 2));
            View view = findViewById(R.id.toolbarShadow);
            if (view != null) view.setVisibility(View.GONE);
        }
    }
}
