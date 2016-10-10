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
import android.view.*;

import javax.annotation.*;

/**
 * Base class for all the menus in the application.
 * <p>
 * This class receives from BaseActivity all callbacks related to menus, such as
 * menu creation and click events. It also handles some implementation details
 * of creating menus in Android, such as inflating the resources.
 */
public abstract class BaseMenu
{
    @NonNull
    private final BaseActivity activity;

    public BaseMenu(@NonNull BaseActivity activity)
    {
        this.activity = activity;
    }

    /**
     * Declare that the menu has changed, and should be recreated.
     */
    public void invalidate()
    {
        activity.invalidateOptionsMenu();
    }

    /**
     * Called when the menu is first displayed.
     * <p>
     * The given menu is already inflated and ready to receive items. The
     * application should override this method and add items to the menu here.
     *
     * @param menu the menu that is being created.
     */
    public void onCreate(@NonNull Menu menu)
    {
    }

    /**
     * Called when the menu is first displayed.
     * <p>
     * This method cannot be overridden. The application should override the
     * methods onCreate(Menu) and getMenuResourceId instead.
     *
     * @param inflater a menu inflater, for creating the menu
     * @param menu     the menu that is being created.
     */
    public final void onCreate(@NonNull MenuInflater inflater,
                               @NonNull Menu menu)
    {
        menu.clear();
        inflater.inflate(getMenuResourceId(), menu);
        onCreate(menu);
    }

    /**
     * Called whenever an item on the menu is selected.
     *
     * @param item the item that was selected.
     * @return true if the event was consumed, or false otherwise
     */
    public boolean onItemSelected(@NonNull MenuItem item)
    {
        return false;
    }

    /**
     * Returns the id of the resource that should be used to inflate this menu.
     *
     * @return id of the menu resource.
     */
    @Resource
    protected abstract int getMenuResourceId();
}
