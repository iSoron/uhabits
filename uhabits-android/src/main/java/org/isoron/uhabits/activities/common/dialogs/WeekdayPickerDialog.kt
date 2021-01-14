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
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.utils.DateUtils
import java.util.Calendar

/**
 * Dialog that allows the user to pick one or more days of the week.
 */
class WeekdayPickerDialog :
    AppCompatDialogFragment(),
    OnMultiChoiceClickListener,
    DialogInterface.OnClickListener {
    private var selectedDays: BooleanArray? = null
    private var listener: OnWeekdaysPickedListener? = null
    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        selectedDays!![which] = isChecked
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            selectedDays = savedInstanceState.getBooleanArray(KEY_SELECTED_DAYS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray(KEY_SELECTED_DAYS, selectedDays)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (listener != null) listener!!.onWeekdaysSet(WeekdayList(selectedDays))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(
            activity!!
        )
        builder
            .setTitle(R.string.select_weekdays)
            .setMultiChoiceItems(
                DateUtils.getLongWeekdayNames(Calendar.SATURDAY),
                selectedDays,
                this
            )
            .setPositiveButton(android.R.string.yes, this)
            .setNegativeButton(
                android.R.string.cancel
            ) { _: DialogInterface?, _: Int -> dismiss() }
        return builder.create()
    }

    fun setListener(listener: OnWeekdaysPickedListener?) {
        this.listener = listener
    }

    fun setSelectedDays(days: WeekdayList) {
        selectedDays = days.toArray()
    }

    fun interface OnWeekdaysPickedListener {
        fun onWeekdaysSet(days: WeekdayList)
    }

    companion object {
        private const val KEY_SELECTED_DAYS = "selectedDays"
    }
}
