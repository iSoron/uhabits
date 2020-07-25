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

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import org.isoron.androidbase.activities.ActivityContext
import org.isoron.androidbase.utils.InterfaceUtils
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Checkmark
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import javax.inject.Inject

class CheckmarkOptionPickerFactory
@Inject constructor(
        @ActivityContext private val context: Context
) {
    fun create(habitName: String,
               habitTimestamp: String,
               value: Int,
               callback: ListHabitsBehavior.CheckmarkOptionsCallback): AlertDialog {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.checkmark_option_picker_dialog, null)
        val title = context.resources.getString(
                R.string.choose_checkmark_option, habitName, habitTimestamp)
        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setTitle(title)
                .setOnDismissListener{
                    callback.onCheckmarkOptionDismissed()
                }
                .create()

        val buttonValues = mapOf(
                R.id.check_button to Checkmark.CHECKED_EXPLICITLY,
                R.id.skip_button to Checkmark.SKIPPED_EXPLICITLY,
                R.id.fail_button to Checkmark.FAILED_EXPLICITLY_NECESSARY,
                R.id.clear_button to Checkmark.UNCHECKED
        )
        val valuesToButton = mapOf(
                Checkmark.CHECKED_EXPLICITLY to R.id.check_button,
                Checkmark.SKIPPED_EXPLICITLY to R.id.skip_button ,
                Checkmark.FAILED_EXPLICITLY_NECESSARY to R.id.fail_button,
                Checkmark.FAILED_EXPLICITLY_UNNECESSARY to R.id.fail_button
        )

        for ((buttonId, buttonValue) in buttonValues) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setTypeface(InterfaceUtils.getFontAwesome(context))
            button.setOnClickListener{
                callback.onCheckmarkOptionPicked(buttonValue)
                dialog.dismiss()
            }
            if (valuesToButton.containsKey(value) &&  valuesToButton[value] == buttonId) {
                val color = context.resources.getColor(R.color.amber_800)
                if (Build.VERSION.SDK_INT >= 29) {
                    button.background.colorFilter = BlendModeColorFilter(color, BlendMode.MULTIPLY)
                }  else {
                    button.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                }
            }
        }

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        return dialog
    }
}
