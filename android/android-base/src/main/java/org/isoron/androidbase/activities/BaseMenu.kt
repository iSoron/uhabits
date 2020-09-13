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
package org.isoron.androidbase.activities

import android.view.*
import androidx.annotation.*

/**
 * Base class for all the menus in the application.
 *
 * This class receives from BaseActivity all callbacks related to menus, such as
 * menu creation and click events. It also handles some implementation details
 * of creating menus in Android, such as inflating the resources.
 */
abstract class BaseMenu(private val activity: BaseActivity) {

    /**
     * Declare that the menu has changed, and should be recreated.
     */
    fun invalidate() {
        activity.invalidateOptionsMenu()
    }

    /**
     * Called when the menu is first displayed.
     *
     * The given menu is already inflated and ready to receive items. The
     * application should override this method and add items to the menu here.
     *
     * @param menu the menu that is being created.
     */
    open fun onCreate(menu: Menu) {}

    /**
     * Called when the menu is first displayed.
     *
     * This method should not be overridden. The application should override
     * the methods onCreate(Menu) and getMenuResourceId instead.
     *
     * @param inflater a menu inflater, for creating the menu
     * @param menu     the menu that is being created.
     */
    fun onCreate(inflater: MenuInflater, menu: Menu) {
        menu.clear()
        inflater.inflate(getMenuResourceId(), menu)
        onCreate(menu)
    }

    /**
     * Called whenever an item on the menu is selected.
     *
     * @param item the item that was selected.
     * @return true if the event was consumed, or false otherwise
     */
    open fun onItemSelected(item: MenuItem): Boolean = false

    /**
     * Returns the id of the resource that should be used to inflate this menu.
     *
     * @return id of the menu resource.
     */
    @MenuRes
    protected abstract fun getMenuResourceId(): Int

}