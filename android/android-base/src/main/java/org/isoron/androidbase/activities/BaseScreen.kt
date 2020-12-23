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

import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.*
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.content.*
import com.google.android.material.snackbar.*
import org.isoron.androidbase.*
import org.isoron.androidbase.utils.*
import org.isoron.androidbase.utils.ColorUtils.mixColors
import org.isoron.androidbase.utils.InterfaceUtils.dpToPixels
import java.io.*

/**
 * Base class for all screens in the application.
 *
 * Screens are responsible for deciding what root views and what menus should be attached to the
 * main window. They are also responsible for showing other screens and for receiving their results.
 */
open class BaseScreen(@JvmField protected var activity: BaseActivity) {

    private var snackbar: Snackbar? = null

    /**
     * Called when another Activity has finished, and has returned some result.
     *
     * @param requestCode the request code originally supplied to startActivityForResult.
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
     * Shows a message on the screen.
     *
     * @param stringId the string resource id for this message.
     */
    fun showMessage(@StringRes stringId: Int?, rootView: View?) {
        var snackbar = this.snackbar
        if (stringId == null || rootView == null) return
        if (snackbar == null) {
            snackbar = Snackbar.make(rootView, stringId, Snackbar.LENGTH_SHORT)
            val tvId = R.id.snackbar_text
            val tv = snackbar.view.findViewById<TextView>(tvId)
            tv.setTextColor(Color.WHITE)
            this.snackbar = snackbar
        }
        snackbar.setText(stringId)
        snackbar.show()
    }

    fun showSendEmailScreen(@StringRes toId: Int, @StringRes subjectId: Int, content: String?) {
        val to = activity.getString(toId)
        val subject = activity.getString(subjectId)
        activity.startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
        })
    }

    fun showSendFileScreen(archiveFilename: String) {
        val file = File(archiveFilename)
        val fileUri = FileProvider.getUriForFile(activity, "org.isoron.uhabits", file)
        activity.startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        })
    }
}