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

import android.support.annotation.*;
import android.support.v7.view.ActionMode;
import android.view.*;

/**
 * Base class for all the selection menus in the application.
 * <p>
 * A selection menu is a menu that appears when the screen starts a selection
 * operation. It contains actions that modify the selected items, such as delete
 * or archive. Since it replaces the toolbar, it also has a title.
 * <p>
 * This class hides many implementation details of creating such menus in
 * Android. The interface is supposed to look very similar to {@link BaseMenu},
 * with a few additional methods, such as finishing the selection operation.
 * Internally, it uses an {@link ActionMode}.
 */
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

    /**
     * Declare that the menu has changed, and should be recreated.
     */
    public void invalidate()
    {
        if (actionMode != null) actionMode.invalidate();
    }

    /**
     * Called when the menu is first displayed.
     * <p>
     * This method cannot be overridden. The application should override the
     * methods onCreate(Menu) and getMenuResourceId instead.
     *
     * @param inflater a menu inflater, for creating the menu
     * @param mode     the action mode associated with this menu.
     * @param menu     the menu that is being created.
     */
    public final void onCreate(@NonNull MenuInflater inflater,
                               @NonNull ActionMode mode,
                               @NonNull Menu menu)
    {
        this.actionMode = mode;
        inflater.inflate(getResourceId(), menu);
        onCreate(menu);
    }

    /**
     * Called when the selection operation is about to finish.
     */
    public void onFinish()
    {

    }

    /**
     * Called whenever an item on the menu is selected.
     *
     * @param item the item that was selected.
     * @return true if the event was consumed, or false otherwise
     */
    public boolean onItemClicked(@NonNull MenuItem item)
    {
        return false;
    }


    /**
     * Called whenever the menu is invalidated.
     *
     * @param menu the menu to be refreshed
     * @return true if the menu has changes, false otherwise
     */
    public boolean onPrepare(@NonNull Menu menu)
    {
        return false;
    }

    /**
     * Sets the title of the selection menu.
     *
     * @param title the new title.
     */
    public void setTitle(String title)
    {
        if (actionMode != null) actionMode.setTitle(title);
    }

    protected abstract int getResourceId();

    /**
     * Called when the menu is first created.
     *
     * @param menu the menu being created
     */
    protected void onCreate(@NonNull Menu menu)
    {
    }
}
