package org.isoron.uhabits.notifications;

import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.text.format.*;
import android.view.*;
import android.widget.*;

import com.android.datetimepicker.time.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.receivers.*;

import java.util.*;

import butterknife.*;

import static android.content.ContentUris.*;

public class SnoozeDelayPickerActivity extends AppCompatActivity implements AdapterView
        .OnItemClickListener
{
    public static final String ACTION_ASK_SNOOZE = "org.isoron.uhabits.ACTION_ASK_SNOOZE";

    private Habit habit;

    private ReminderController reminderController;

    @BindArray(R.array.snooze_picker_names)
    protected String[] snoozeNames;

    @BindArray(R.array.snooze_picker_values)
    protected int[] snoozeValues;

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

        setTheme(R.style.Theme_AppCompat_Light_Dialog_Alert);
        ButterKnife.bind(this);

        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                snoozeNames));
        listView.setOnItemClickListener(this);
        setContentView(listView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (snoozeValues[position] >= 0)
        {
            reminderController.onSnoozeDelayPicked(habit, snoozeValues[position]);
            finish();
        }
        else showTimePicker();
    }

    private void showTimePicker()
    {
        final Calendar calendar = Calendar.getInstance();
        int defaultHour = calendar.get(Calendar.HOUR_OF_DAY);
        int defaultMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = TimePickerDialog.newInstance(
                (view, hour, minute) ->
                        reminderController.onSnoozeTimePicked(habit, hour, minute),
                defaultHour, defaultMinute, DateFormat.is24HourFormat(this));

        dialog.show(getSupportFragmentManager(), "timePicker");
    }
}
