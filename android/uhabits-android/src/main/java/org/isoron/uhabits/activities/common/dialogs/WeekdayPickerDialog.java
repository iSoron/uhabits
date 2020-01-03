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

package org.isoron.uhabits.activities.common.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.WeekdayList;
import org.isoron.uhabits.core.utils.DateUtils;

import java.util.Calendar;

/**
 * Dialog that allows the user to pick one or more days of the week.
 */
public class WeekdayPickerDialog extends AppCompatDialogFragment implements
                                                                 DialogInterface.OnMultiChoiceClickListener,
                                                                 DialogInterface.OnClickListener
{
    private static final String KEY_SELECTED_DAYS = "selectedDays";
    private boolean[] selectedDays;

    private OnWeekdaysPickedListener listener;

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked)
    {
        selectedDays[which] = isChecked;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            selectedDays = savedInstanceState.getBooleanArray(KEY_SELECTED_DAYS);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(KEY_SELECTED_DAYS, selectedDays);
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (listener != null)
            listener.onWeekdaysSet(new WeekdayList(selectedDays));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(R.string.select_weekdays)
            .setMultiChoiceItems(DateUtils.getLongWeekdayNames(Calendar.SATURDAY),
                    selectedDays,
                    this)
            .setPositiveButton(android.R.string.yes, this)
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                dismiss();
            });

        return builder.create();
    }

    public void setListener(OnWeekdaysPickedListener listener)
    {
        this.listener = listener;
    }

    public void setSelectedDays(WeekdayList days)
    {
        this.selectedDays = days.toArray();
    }

    public interface OnWeekdaysPickedListener
    {
        void onWeekdaysSet(WeekdayList days);
    }
}
