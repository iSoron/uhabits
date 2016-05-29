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
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class BaseSelectionMenu
{
    @Nullable
    private ActionMode actionMode;

    /**
     * Finishes the selection operation.
     */
    public void finish()
    {
        if (actionMode != null) actionMode.finish();
    }

    public void invalidate()
    {
        if (actionMode != null) actionMode.invalidate();
    }

    public final void onCreate(@NonNull MenuInflater menuInflater,
                               @NonNull ActionMode mode,
                               @NonNull Menu menu)
    {
        this.actionMode = mode;
        menuInflater.inflate(getResourceId(), menu);
        onCreate(menu);
    }

    public void onDestroy()
    {

    }

    public boolean onItemClicked(@NonNull MenuItem item)
    {
        return false;
    }

    public boolean onPrepare(@NonNull Menu menu)
    {
        return false;
    }

    public void setTitle(String title)
    {
        if (actionMode != null) actionMode.setTitle(title);
    }

    protected abstract int getResourceId();

    /**
     * Called when the menu is first created, right after the menu has been
     * inflated.
     *
     * @param menu the menu containing the buttons
     */
    protected void onCreate(@NonNull Menu menu)
    {
    }
}
