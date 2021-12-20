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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.frequency_picker_dialog.view.*
import org.isoron.uhabits.R

class FrequencyPickerDialog(
    var freqNumerator: Int,
    var freqDenominator: Int
) : AppCompatDialogFragment() {

    lateinit var contentView: View
    var onFrequencyPicked: (num: Int, den: Int) -> Unit = { _, _ -> }

    constructor() : this(1, 1)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireActivity())
        contentView = inflater.inflate(R.layout.frequency_picker_dialog, null)

        addBeforeAfterText(
            this.getString(R.string.every_x_days),
            contentView.everyXDaysContainer,
        )

        addBeforeAfterText(
            this.getString(R.string.x_times_per_week),
            contentView.xTimesPerWeekContainer,
        )

        addBeforeAfterText(
            this.getString(R.string.x_times_per_month),
            contentView.xTimesPerMonthContainer,
        )

        addBeforeAfterText(
            this.getString(R.string.x_times_per_y_days),
            contentView.xTimesPerYDaysContainer,
        )

        contentView.everyDayRadioButton.setOnClickListener {
            check(contentView.everyDayRadioButton)
        }

        contentView.everyXDaysRadioButton.setOnClickListener {
            check(contentView.everyXDaysRadioButton)
            val everyXDaysTextView = contentView.everyXDaysTextView
            selectInputField(everyXDaysTextView)
        }

        contentView.everyXDaysTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) check(contentView.everyXDaysRadioButton)
        }

        contentView.xTimesPerWeekRadioButton.setOnClickListener {
            check(contentView.xTimesPerWeekRadioButton)
            selectInputField(contentView.xTimesPerWeekTextView)
        }

        contentView.xTimesPerWeekTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) check(contentView.xTimesPerWeekRadioButton)
        }

        contentView.xTimesPerMonthRadioButton.setOnClickListener {
            check(contentView.xTimesPerMonthRadioButton)
            selectInputField(contentView.xTimesPerMonthTextView)
        }

        contentView.xTimesPerMonthTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) check(contentView.xTimesPerMonthRadioButton)
        }

        contentView.xTimesPerYDaysRadioButton.setOnClickListener {
            check(contentView.xTimesPerYDaysRadioButton)
            selectInputField(contentView.xTimesPerYDaysXTextView)
        }

        contentView.xTimesPerYDaysXTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) check(contentView.xTimesPerYDaysRadioButton)
        }

        contentView.xTimesPerYDaysYTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) check(contentView.xTimesPerYDaysRadioButton)
        }

        return AlertDialog.Builder(requireActivity())
            .setView(contentView)
            .setPositiveButton(R.string.save) { _, _ -> onSaveClicked() }
            .create()
    }

    private fun addBeforeAfterText(
        str: String,
        container: LinearLayout
    ) {
        val parts = str.split("%d")
        for (i in parts.indices) {
            container.addView(
                TextView(activity).apply { text = parts[i].trim() }, 2 * i + 1,
            )
        }
    }

    private fun onSaveClicked() {
        var numerator = 1
        var denominator = 1
        when {
            contentView.everyDayRadioButton.isChecked -> {
                // NOP
            }
            contentView.everyXDaysRadioButton.isChecked -> {
                if (contentView.everyXDaysTextView.text.isNotEmpty()) {
                    denominator = Integer.parseInt(contentView.everyXDaysTextView.text.toString())
                }
            }
            contentView.xTimesPerWeekRadioButton.isChecked -> {
                if (contentView.xTimesPerWeekTextView.text.isNotEmpty()) {
                    numerator = Integer.parseInt(contentView.xTimesPerWeekTextView.text.toString())
                    denominator = 7
                }
            }
            contentView.xTimesPerYDaysRadioButton.isChecked -> {
                if (contentView.xTimesPerYDaysXTextView.text.isNotEmpty() && contentView.xTimesPerYDaysYTextView.text.isNotEmpty()) {
                    numerator = Integer.parseInt(contentView.xTimesPerYDaysXTextView.text.toString())
                    denominator = Integer.parseInt(contentView.xTimesPerYDaysYTextView.text.toString())
                }
            }
            else -> {
                if (contentView.xTimesPerMonthTextView.text.isNotEmpty()) {
                    numerator = Integer.parseInt(contentView.xTimesPerMonthTextView.text.toString())
                    denominator = 30
                }
            }
        }
        if (numerator >= denominator || numerator < 1) {
            numerator = 1
            denominator = 1
        }
        onFrequencyPicked(numerator, denominator)
        dismiss()
    }

    private fun check(view: RadioButton) {
        uncheckAll()
        view.isChecked = true
        view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        populateViews()
    }

    private fun populateViews() {
        uncheckAll()
        if (freqDenominator == 30 || freqDenominator == 31) {
            contentView.xTimesPerMonthRadioButton.isChecked = true
            contentView.xTimesPerMonthTextView.setText(freqNumerator.toString())
            selectInputField(contentView.xTimesPerMonthTextView)
        } else {
            if (freqNumerator == 1) {
                if (freqDenominator == 1) {
                    contentView.everyDayRadioButton.isChecked = true
                } else {
                    contentView.everyXDaysRadioButton.isChecked = true
                    contentView.everyXDaysTextView.setText(freqDenominator.toString())
                    selectInputField(contentView.everyXDaysTextView)
                }
            } else {
                if (freqDenominator == 7) {
                    contentView.xTimesPerWeekRadioButton.isChecked = true
                    contentView.xTimesPerWeekTextView.setText(freqNumerator.toString())
                    selectInputField(contentView.xTimesPerWeekTextView)
                } else {
                    contentView.xTimesPerYDaysRadioButton.isChecked = true
                    contentView.xTimesPerYDaysXTextView.setText(freqNumerator.toString())
                    contentView.xTimesPerYDaysYTextView.setText(freqDenominator.toString())
                }
            }
        }
    }

    private fun selectInputField(view: EditText) {
        view.setSelection(view.text.length)
    }

    private fun uncheckAll() {
        contentView.everyDayRadioButton.isChecked = false
        contentView.everyXDaysRadioButton.isChecked = false
        contentView.xTimesPerWeekRadioButton.isChecked = false
        contentView.xTimesPerMonthRadioButton.isChecked = false
        contentView.xTimesPerYDaysRadioButton.isChecked = false
    }
}
