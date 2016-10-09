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
import android.support.annotation.*;
import android.support.v4.content.res.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;

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
    private final Context context;

    private final BaseActivity activity;

    private final ThemeSwitcher themeSwitcher;

    public BaseRootView(Context context)
    {
        super(context);
        this.context = context;
        activity = (BaseActivity) context;
        themeSwitcher = activity.getComponent().getThemeSwitcher();
    }

    public boolean getDisplayHomeAsUp()
    {
        return false;
    }

    @NonNull
    public abstract Toolbar getToolbar();

    public int getToolbarColor()
    {
        if (SDK_INT < LOLLIPOP && !themeSwitcher.isNightMode())
        {
            return ResourcesCompat.getColor(context.getResources(),
                R.color.grey_900, context.getTheme());
        }

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
            if(view != null) view.setVisibility(GONE);
        }
    }
}
