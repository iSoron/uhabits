package org.isoron.uhabits.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.isoron.helpers.DateHelper;

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
        builder.setTitle("Select weekdays")
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
