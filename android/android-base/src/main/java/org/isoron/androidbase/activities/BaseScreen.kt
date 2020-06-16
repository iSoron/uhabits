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
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import org.isoron.androidbase.R
import org.isoron.androidbase.utils.ColorUtils.mixColors
import org.isoron.androidbase.utils.InterfaceUtils.dpToPixels
import org.isoron.androidbase.utils.StyledResources
import java.io.File

/**
 * Base class for all screens in the application.
 *
 *
 * Screens are responsible for deciding what root views and what menus should be
 * attached to the main window. They are also responsible for showing other
 * screens and for receiving their results.
 */
open class BaseScreen(@JvmField protected var activity: BaseActivity) {
    private var rootView: BaseRootView? = null
    private var selectionMenu: BaseSelectionMenu? = null
    protected var snackbar: Snackbar? = null

    /**
     * Notifies the screen that its contents should be updated.
     */
    fun invalidate() {
        if (rootView == null) return
        rootView!!.invalidate()
    }

    fun invalidateToolbar() {
        if (rootView == null) return
        activity.runOnUiThread {
            val toolbar = rootView!!.getToolbar()
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar ?: return@runOnUiThread
            actionBar.setDisplayHomeAsUpEnabled(rootView!!.displayHomeAsUp)
            val color = rootView!!.getToolbarColor()
            setActionBarColor(actionBar, color)
            setStatusBarColor(color)
        }
    }

    /**
     * Called when another Activity has finished, and has returned some result.
     *
     * @param requestCode the request code originally supplied to [                    ][android.app.Activity.startActivityForResult].
     * @param resultCode  the result code sent by the other activity.
     * @param data        an Intent containing extra data sent by the other
     * activity.
     * @see {@link android.app.Activity.onActivityResult
     */
    open fun onResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    /**
     * Called after activity has been recreated, and the dialogs should be
     * reattached to their controllers.
     */
    open fun reattachDialogs() {}

    /**
     * Sets the menu to be shown by this screen.
     *
     *
     * This menu will be visible if when there is no active selection operation.
     * If the provided menu is null, then no menu will be shown.
     *
     * @param menu the menu to be shown.
     */
    fun setMenu(menu: BaseMenu?) {
        activity.setBaseMenu(menu)
    }

    /**
     * Sets the root view for this screen.
     *
     * @param rootView the root view for this screen.
     */
    fun setRootView(rootView: BaseRootView?) {
        this.rootView = rootView
        activity.setContentView(rootView)
        if (rootView == null) return
        rootView.onAttachedToScreen(this)
        invalidateToolbar()
    }

    /**
     * Sets the menu to be shown when a selection is active on the screen.
     *
     * @param menu the menu to be shown during a selection
     */
    fun setSelectionMenu(menu: BaseSelectionMenu?) {
        selectionMenu = menu
    }

    /**
     * Shows a message on the screen.
     *
     * @param stringId the string resource id for this message.
     */
    fun showMessage(@StringRes stringId: Int?) {
        if (stringId == null || rootView == null) return
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView!!, stringId, Snackbar.LENGTH_SHORT)
            val tvId = R.id.snackbar_text
            val tv = snackbar!!.view.findViewById<View>(tvId) as TextView
            tv.setTextColor(Color.WHITE)
        } else snackbar!!.setText(stringId)
        snackbar!!.show()
    }

    fun showSendEmailScreen(@StringRes toId: Int,
                            @StringRes subjectId: Int,
                            content: String?) {
        val to = activity.getString(toId)
        val subject = activity.getString(subjectId)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, content)
        activity.startActivity(intent)
    }

    fun showSendFileScreen(archiveFilename: String) {
        val file = File(archiveFilename)
        val fileUri = FileProvider.getUriForFile(activity, "org.isoron.uhabits", file)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "application/zip"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        activity.startActivity(intent)
    }

    /**
     * Instructs the screen to start a selection.
     *
     *
     * If a selection menu was provided, this menu will be shown instead of the
     * regular one.
     */
    fun startSelection() {
        activity.startSupportActionMode(ActionModeWrapper())
    }

    private fun setActionBarColor(actionBar: ActionBar, color: Int) {
        val drawable = ColorDrawable(color)
        actionBar.setBackgroundDrawable(drawable)
    }

    private fun setStatusBarColor(baseColor: Int) {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) return
        val darkerColor = mixColors(baseColor, Color.BLACK, 0.75f)
        activity.window.statusBarColor = darkerColor
    }

    private inner class ActionModeWrapper : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?,
                                         item: MenuItem?): Boolean {
            return if (item == null || selectionMenu == null) false else selectionMenu!!.onItemClicked(item)
        }

        override fun onCreateActionMode(mode: ActionMode?,
                                        menu: Menu?): Boolean {
            if (selectionMenu == null) return false
            if (mode == null || menu == null) return false
            selectionMenu!!.onCreate(activity.menuInflater, mode, menu)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (selectionMenu == null) return
            selectionMenu!!.onFinish()
        }

        override fun onPrepareActionMode(mode: ActionMode?,
                                         menu: Menu?): Boolean {
            return if (selectionMenu == null || menu == null) false else selectionMenu!!.onPrepare(menu)
        }
    }

    companion object {
        @JvmStatic
        @Deprecated("")
        fun getDefaultActionBarColor(context: Context): Int {
            return if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
                ResourcesCompat.getColor(context.resources,
                        R.color.grey_900, context.theme)
            } else {
                val res = StyledResources(context)
                res.getColor(R.attr.colorPrimary)
            }
        }

        @JvmStatic
        @Deprecated("")
        fun setupActionBarColor(activity: AppCompatActivity,
                                color: Int) {
            val toolbar = activity.findViewById<View>(R.id.toolbar) as Toolbar ?: return
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar ?: return
            actionBar.setDisplayHomeAsUpEnabled(true)
            val drawable = ColorDrawable(color)
            actionBar.setBackgroundDrawable(drawable)
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                val darkerColor = mixColors(color, Color.BLACK, 0.75f)
                activity.window.statusBarColor = darkerColor
                toolbar.elevation = dpToPixels(activity, 2f)
                var view = activity.findViewById<View>(R.id.toolbarShadow)
                if (view != null) view.visibility = View.GONE
                view = activity.findViewById(R.id.headerShadow)
                if (view != null) view.visibility = View.GONE
            }
        }
    }

}