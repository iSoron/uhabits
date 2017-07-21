/*
 * Copyright (C) 2017 Álinson Santos Xavier
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
 * with this program. If not, see .
 */

package org.isoron.uhabits.activities.common.dialogs

import android.content.*
import android.support.v7.app.*
import android.text.*
import android.view.*
import android.view.inputmethod.*
import android.widget.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import javax.inject.*

class NumberPickerFactory
@Inject constructor(
        @ActivityContext private val context: Context
) {
    fun create(value: Double,
               unit: String,
               callback: ListHabitsBehavior.NumberPickerCallback): AlertDialog {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.number_picker_dialog, null)

        val picker = view.findViewById(R.id.picker) as NumberPicker
        val picker2 = view.findViewById(R.id.picker2) as NumberPicker
        val tvUnit = view.findViewById(R.id.tvUnit) as TextView

        val intValue = Math.round(value * 100).toInt()

        picker.minValue = 0
        picker.maxValue = Integer.MAX_VALUE / 100
        picker.value = intValue / 100
        picker.wrapSelectorWheel = false

        picker2.minValue = 0
        picker2.maxValue = 19
        picker2.setFormatter { v -> String.format("%02d", 5 * v) }
        picker2.value = intValue % 100 / 5
        refreshInitialValue(picker2)

        tvUnit.text = unit

        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setTitle(R.string.change_value)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    picker.clearFocus()
                    val v = picker.value + 0.05 * picker2.value
                    callback.onNumberPicked(v)
                }
                .create()

        InterfaceUtils.setupEditorAction(picker) { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        }

        return dialog
    }

    private fun refreshInitialValue(picker: NumberPicker) {
        // Workaround for Android bug:
        // https://code.google.com/p/android/issues/detail?id=35482
        val f = NumberPicker::class.java.getDeclaredField("mInputText")
        f.isAccessible = true
        val inputText = f.get(picker) as EditText
        inputText.filters = arrayOfNulls<InputFilter>(0)
    }
}
