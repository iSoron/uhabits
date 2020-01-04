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

package org.isoron.androidbase.activities;

import android.content.*;

import androidx.appcompat.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.androidbase.*;
import org.isoron.androidbase.utils.*;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Base class for all root views in the application.
 * <p>
 * A root view is an Android view that is directly attached to an activity. This
 * view usually includes a toolbar and a progress bar. This abstract class hides
 * some of the complexity of setting these things up, for every version of
 * Android.
 */
public abstract class BaseRootView extends FrameLayout
{
    @NonNull
    private final Context context;

    protected boolean shouldDisplayHomeAsUp = false;

    @Nullable
    private BaseScreen screen;

    public BaseRootView(@NonNull Context context)
    {
        super(context);
        this.context = context;
    }

    public boolean getDisplayHomeAsUp()
    {
        return shouldDisplayHomeAsUp;
    }

    public void setDisplayHomeAsUp(boolean b)
    {
        shouldDisplayHomeAsUp = b;
    }

    @NonNull
    public Toolbar getToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) throw new RuntimeException(
            "Your BaseRootView should have a " +
            "toolbar with id R.id.toolbar");
        return toolbar;
    }

    public int getToolbarColor()
    {
        StyledResources res = new StyledResources(context);
        return res.getColor(R.attr.colorPrimary);
    }

    protected void initToolbar()
    {
        if (SDK_INT >= LOLLIPOP)
        {
            getToolbar().setElevation(InterfaceUtils.dpToPixels(context, 2));

            View view = findViewById(R.id.toolbarShadow);
            if (view != null) view.setVisibility(GONE);

            view = findViewById(R.id.headerShadow);
            if (view != null) view.setVisibility(GONE);
        }
    }

    public void onAttachedToScreen(BaseScreen screen)
    {
        this.screen = screen;
    }

    @Nullable
    public BaseScreen getScreen()
    {
        return screen;
    }
}
