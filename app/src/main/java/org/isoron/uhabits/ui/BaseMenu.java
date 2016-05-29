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

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class BaseMenu
{
    private final BaseActivity activity;

    public BaseMenu(BaseActivity activity)
    {
        this.activity = activity;
    }

    public final boolean onCreate(@NonNull MenuInflater inflater,
                                  @NonNull Menu menu)
    {
        menu.clear();
        inflater.inflate(getMenuResourceId(), menu);
        onCreate(menu);
        return true;
    }

    public void onCreate(@NonNull Menu menu)
    {
    }

    public boolean onItemSelected(@NonNull MenuItem item)
    {
        return true;
    }

    protected abstract int getMenuResourceId();

    public void invalidate()
    {
        activity.invalidateOptionsMenu();
    }
}
