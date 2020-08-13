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

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import org.isoron.androidbase.R
import org.isoron.androidbase.utils.InterfaceUtils.dpToPixels
import org.isoron.androidbase.utils.StyledResources

/**
 * Base class for all root views in the application.
 *
 *
 * A root view is an Android view that is directly attached to an activity. This
 * view usually includes a toolbar and a progress bar. This abstract class hides
 * some of the complexity of setting these things up, for every version of
 * Android.
 */
abstract class BaseRootView(context: Context) : FrameLayout(context) {
    var displayHomeAsUp = false
    var screen: BaseScreen? = null
        private set

    open fun getToolbar(): Toolbar {
        return findViewById(R.id.toolbar)
                ?: throw RuntimeException("Your BaseRootView should have a toolbar with id R.id.toolbar")
    }

    open fun getToolbarColor(): Int = StyledResources(context).getColor(R.attr.colorPrimary)

    protected open fun initToolbar() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getToolbar().elevation = dpToPixels(context, 2f)
            findViewById<View>(R.id.toolbarShadow)?.visibility = View.GONE
            findViewById<View>(R.id.headerShadow)?.visibility = View.GONE
        }
    }

    fun onAttachedToScreen(screen: BaseScreen?) {
        this.screen = screen
    }
}