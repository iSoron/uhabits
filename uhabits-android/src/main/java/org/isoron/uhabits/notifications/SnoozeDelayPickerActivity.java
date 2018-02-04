package org.isoron.uhabits.notifications;


import android.app.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.text.format.*;
import android.view.*;
import android.widget.*;

import com.android.datetimepicker.time.TimePickerDialog;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.receivers.*;

import java.util.*;

import static android.content.ContentUris.parseId;

public class SnoozeDelayPickerActivity extends FragmentActivity
    implements AdapterView.OnItemClickListener
{
    private Habit habit;

    private ReminderController reminderController;

    @Override
    protected void onCreate(@Nullable Bundle bundle)
    {
        super.onCreate(bundle);
        if (getIntent() == null) finish();
        if (getIntent().getData() == null) finish();

        HabitsApplication app = (HabitsApplication) getApplicationContext();
        HabitsApplicationComponent appComponent = app.getComponent();
        reminderController = appComponent.getReminderController();
        habit = appComponent.getHabitList().getById(parseId(getIntent().getData()));
        if (habit == null) finish();

        int theme = R.style.Theme_AppCompat_Light_Dialog_Alert;
        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, theme))
            .setTitle(R.string.select_snooze_delay)
            .setItems(R.array.snooze_picker_names, null)
            .create();

        dialog.getListView().setOnItemClickListener(this);
        dialog.setOnDismissListener(d -> finish());
        dialog.show();
    }

    private void showTimePicker()
    {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = TimePickerDialog.newInstance(
            (view, hour, minute) -> {
                reminderController.onSnoozeTimePicked(habit, hour, minute);
                finish();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this));
        dialog.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        int[] snoozeValues = getResources().getIntArray(R.array.snooze_picker_values);
        if (snoozeValues[position] >= 0)
        {
            reminderController.onSnoozeDelayPicked(habit, snoozeValues[position]);
            finish();
        }
        else showTimePicker();
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
