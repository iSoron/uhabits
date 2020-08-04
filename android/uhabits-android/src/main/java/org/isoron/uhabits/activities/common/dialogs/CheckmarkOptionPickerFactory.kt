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
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import javax.inject.*


class CheckmarkOptionPickerFactory
@Inject constructor(
        @ActivityContext private val context: Context
) {
    fun create(habit: Habit,
               habitTimestamp: String,
               value: Int,
               callback: ListHabitsBehavior.CheckmarkOptionsCallback): AlertDialog {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.checkmark_option_picker_dialog, null)
        val dialog = AlertDialog.Builder(context)
                .setView(view)
                .setTitle(habit.name)
                .setOnDismissListener{
                    callback.onCheckmarkOptionDismissed()
                }
                .create()

        val buttonValues = mapOf(
                R.id.yes_button to Checkmark.CHECKED_EXPLICITLY,
                R.id.skip_button to Checkmark.SKIPPED_EXPLICITLY,
                R.id.no_button to Checkmark.UNCHECKED_EXPLICITLY_NECESSARY,
                R.id.clear_button to Checkmark.UNCHECKED
        )
        val valuesToButton = mapOf(
                Checkmark.CHECKED_EXPLICITLY to R.id.yes_button,
                Checkmark.SKIPPED_EXPLICITLY to R.id.skip_button ,
                Checkmark.UNCHECKED_EXPLICITLY_NECESSARY to R.id.no_button,
                Checkmark.UNCHECKED_EXPLICITLY_UNNECESSARY to R.id.no_button
        )

        for ((buttonId, buttonValue) in buttonValues) {
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener{
                callback.onCheckmarkOptionPicked(buttonValue)
                dialog.dismiss()
            }
            button.isPressed = (
                    valuesToButton.containsKey(value) &&
                    valuesToButton[value] == buttonId)
            button.typeface = InterfaceUtils.getFontAwesome(context)
        }

        val questionTextView = view.findViewById<TextView>(R.id.choose_checkmark_question_textview)
        var question = context.resources.getString(R.string.default_checkmark_option_question)
        if (habit.question.isNotEmpty()) {
            question = habit.question.trim('?')
        }
        val questionFullText = context.resources.getString(
                R.string.choose_checkmark_question, question, habitTimestamp)
        questionTextView.text = questionFullText

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        return dialog
    }
}
