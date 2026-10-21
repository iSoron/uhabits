/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.requestFocusWithKeyboard

class CategoryDialog : AppCompatDialogFragment() {

    var onConfirmed: (String) -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val pad = InterfaceUtils.dpToPixels(context, 24f).toInt()

        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setText(arguments?.getString(ARG_CATEGORY) ?: "")
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            addView(input)
        }
        val title = TextView(context).apply {
            text = context.getString(R.string.categorize)
            textSize = 18f
            setPadding(pad, pad, pad, 0)
        }
        var save: () -> Unit = {}
        val dialog = AlertDialog.Builder(context)
            .setCustomTitle(title)
            .setView(content)
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.save) { _, _ -> save() }
            .create()
        save = {
            onConfirmed(input.text.toString().trim())
            dialog.dismiss()
        }
        input.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_SEND,
                EditorInfo.IME_ACTION_NEXT -> {
                    save()
                    true
                }
                else -> false
            }
        }
        dialog.window?.setBackgroundDrawableResource(R.drawable.checkmark_dialog_bg)
        input.requestFocusWithKeyboard()
        return dialog
    }

    companion object {
        private const val ARG_CATEGORY = "category"
    }
}
