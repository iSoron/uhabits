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

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.InterfaceUtils
import java.text.DecimalFormatSymbols
import javax.inject.Inject
import kotlin.math.roundToLong

class NumberPickerFactory
@Inject constructor(
    @ActivityContext private val context: Context
) {

    fun create(
        value: Double,
        unit: String,
        notes: String,
        dateString: String,
        callback: ListHabitsBehavior.NumberPickerCallback
    ): AlertDialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.number_picker_dialog, null)

        val picker = view.findViewById<NumberPicker>(R.id.picker)
        val picker2 = view.findViewById<NumberPicker>(R.id.picker2)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)

        val watcherFilter: InputFilter = SeparatorWatcherInputFilter(picker2)
        val numberPickerInputText = getNumberPickerInputText(picker)

        // watch the unfiltered input before the filters remove a possible separator from it
        numberPickerInputText.filters = arrayOf(watcherFilter).plus(numberPickerInputText.filters)

        view.findViewById<TextView>(R.id.tvUnit).text = unit
        view.findViewById<TextView>(R.id.tvSeparator).text =
            DecimalFormatSymbols.getInstance().decimalSeparator.toString()

        val intValue = (value * 100).roundToLong().toInt()

        picker.minValue = 0
        picker.maxValue = Integer.MAX_VALUE / 100
        picker.value = intValue / 100
        picker.wrapSelectorWheel = false

        picker2.minValue = 0
        picker2.maxValue = 99
        picker2.setFormatter { v -> String.format("%02d", v) }
        picker2.value = intValue % 100

        etNotes.setText(notes)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setTitle(dateString)
            .setPositiveButton(R.string.save) { _, _ ->
                picker.clearFocus()
                val v = picker.value + 0.01 * picker2.value
                val note = etNotes.text.toString()
                callback.onNumberPicked(v, note)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                callback.onNumberPickerDismissed()
            }
            .setOnDismissListener {
                callback.onNumberPickerDismissed()
            }
            .create()

        dialog.setOnShowListener {
            picker.getChildAt(0)?.requestFocus()
            dialog.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        InterfaceUtils.setupEditorAction(
            picker
        ) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            }
            false
        }

        InterfaceUtils.setupEditorAction(
            picker2
        ) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            }
            false
        }

        return dialog
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun getNumberPickerInputText(picker: NumberPicker): EditText {
        val f = NumberPicker::class.java.getDeclaredField("mInputText")
        f.isAccessible = true
        return f.get(picker) as EditText
    }
}

class SeparatorWatcherInputFilter(private val nextPicker: NumberPicker) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        if (source == null || source.isEmpty()) {
            return ""
        }
        for (c in source) {
            if (c == DecimalFormatSymbols.getInstance().decimalSeparator || c == '.' || c == ',') {
                nextPicker.performLongClick()
                break
            }
        }
        return source
    }
}
