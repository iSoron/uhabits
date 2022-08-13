/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.activities.common.dialogs

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.databinding.CheckmarkPopupBinding
import org.isoron.uhabits.utils.dimBehind
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.requestFocusWithKeyboard
import java.text.DecimalFormat

class NumberPopup(
    private val context: Context,
    private var notes: String,
    private var value: Double,
    private val prefs: Preferences,
    private val anchor: View,
) {
    var onToggle: (Double, String) -> Unit = { _, _ -> }
    var onDismiss: () -> Unit = {}
    private val originalValue = value
    private lateinit var dialog: Dialog

    private val view = CheckmarkPopupBinding.inflate(LayoutInflater.from(context)).apply {
        // Required for round corners
        container.clipToOutline = true
    }

    init {
        view.numberButtons.visibility = VISIBLE
        hideDisabledButtons()
        populate()
    }

    private fun hideDisabledButtons() {
        if (!prefs.isSkipEnabled) view.skipBtnNumber.visibility = GONE
    }

    private fun populate() {
        view.notes.setText(notes)
        view.value.setText(
            when {
                value < 0.01 -> "0"
                else -> DecimalFormat("#.##").format(value)
            }
        )
    }

    fun show() {
        HabitsApplication.clearCurrentDialog()
        dialog = Dialog(context, android.R.style.Theme_NoTitleBar)
        dialog.setContentView(view.root)
        dialog.window?.apply {
            setLayout(
                view.root.dp(POPUP_WIDTH).toInt(),
                view.root.dp(POPUP_HEIGHT).toInt()
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.setOnDismissListener {
            onDismiss()
            HabitsApplication.currentDialog = null
        }

        view.value.setOnKeyListener { _, keyCode, event ->
            if (event.action == ACTION_DOWN && keyCode == KEYCODE_ENTER) {
                save()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        view.saveBtn.setOnClickListener {
            save()
        }
        view.skipBtnNumber.setOnClickListener {
            view.value.setText((Entry.SKIP.toDouble() / 1000).toString())
            save()
        }
        view.value.requestFocusWithKeyboard()
        dialog.setCanceledOnTouchOutside(true)
        dialog.dimBehind()
        HabitsApplication.currentDialog = dialog
        dialog.show()
    }

    fun save() {
        val value = view.value.text.toString().toDoubleOrNull() ?: originalValue
        val notes = view.notes.text.toString()
        onToggle(value, notes)
        dialog.dismiss()
    }
}
