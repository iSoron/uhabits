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

package org.isoron.uhabits.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.isoron.uhabits.helpers.DateHelper;
import org.isoron.uhabits.R;

public class WeekdayPickerDialog extends DialogFragment
        implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener
{

    public interface OnWeekdaysPickedListener
    {
        void onWeekdaysPicked(boolean[] selectedDays);
    }

    private boolean[] selectedDays;
    private OnWeekdaysPickedListener listener;

    public void setListener(OnWeekdaysPickedListener listener)
    {
        this.listener = listener;
    }

    public void setSelectedDays(boolean[] selectedDays)
    {
        this.selectedDays = selectedDays;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_weekdays)
                .setMultiChoiceItems(DateHelper.getLongDayNames(), selectedDays, this)
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked)
    {
        selectedDays[which] = isChecked;
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if(listener != null) listener.onWeekdaysPicked(selectedDays);
    }
}
