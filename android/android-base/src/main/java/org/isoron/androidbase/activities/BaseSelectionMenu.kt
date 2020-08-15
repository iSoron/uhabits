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
import androidx.appcompat.view.ActionMode

/**
 * Base class for all the selection menus in the application.
 *
 * A selection menu is a menu that appears when the screen starts a selection
 * operation. It contains actions that modify the selected items, such as delete
 * or archive. Since it replaces the toolbar, it also has a title.
 *
 * This class hides many implementation details of creating such menus in
 * Android. The interface is supposed to look very similar to [BaseMenu],
 * with a few additional methods, such as finishing the selection operation.
 * Internally, it uses an [ActionMode].
 */
abstract class BaseSelectionMenu {
    private var actionMode: ActionMode? = null

    /**
     * Finishes the selection operation.
     */
    fun finish() {
        actionMode?.finish()
    }

    /**
     * Declare that the menu has changed, and should be recreated.
     */
    fun invalidate() {
        actionMode?.invalidate()
    }

    /**
     * Called when the menu is first displayed.
     *
     * This method should not be overridden. The application should override
     * the methods onCreate(Menu) and getMenuResourceId instead.
     *
     * @param inflater a menu inflater, for creating the menu
     * @param mode     the action mode associated with this menu.
     * @param menu     the menu that is being created.
     */
    fun onCreate(inflater: MenuInflater, mode: ActionMode, menu: Menu) {
        actionMode = mode
        inflater.inflate(getResourceId(), menu)
        onCreate(menu)
    }

    /**
     * Called when the selection operation is about to finish.
     */
    open fun onFinish() {}

    /**
     * Called whenever an item on the menu is selected.
     *
     * @param item the item that was selected.
     * @return true if the event was consumed, or false otherwise
     */
    open fun onItemClicked(item: MenuItem): Boolean = false

    /**
     * Called whenever the menu is invalidated.
     *
     * @param menu the menu to be refreshed
     * @return true if the menu has changes, false otherwise
     */
    open fun onPrepare(menu: Menu): Boolean = false

    /**
     * Sets the title of the selection menu.
     *
     * @param title the new title.
     */
    fun setTitle(title: String?) {
        actionMode?.title = title
    }

    protected abstract fun getResourceId(): Int

    /**
     * Called when the menu is first created.
     *
     * @param menu the menu being created
     */
    protected fun onCreate(menu: Menu) {}
}