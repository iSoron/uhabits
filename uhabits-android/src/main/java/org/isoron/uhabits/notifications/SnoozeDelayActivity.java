package org.isoron.uhabits.notifications;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.text.format.*;
import android.util.*;

import com.android.datetimepicker.time.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.utils.DateUtils;
import org.isoron.uhabits.receivers.*;

import java.util.*;

import static org.isoron.uhabits.core.ui.ThemeSwitcher.THEME_DARK;
import static org.isoron.uhabits.core.utils.DateUtils.applyTimezone;

public class SnoozeDelayActivity extends FragmentActivity implements
        TimePickerDialog.OnTimeSetListener {

    public static final String ACTION_ASK_SNOOZE = "org.isoron.uhabits.ACTION_ASK_SNOOZE";

    private static final String TAG = "SnoozeDelayActivity";

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        Intent intent = getIntent();
        try
        {
            switch (intent.getAction())
            {
                case ACTION_ASK_SNOOZE:
                    AskSnooze();
                    break;
            }
        }
        catch (RuntimeException e)
        {
            Log.e(TAG, "could not process intent", e);
        }
    }

    private void AskSnooze()
    {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog;
        dialog = TimePickerDialog.newInstance(this,
                hour, minute, DateFormat.is24HourFormat(this));
        HabitsApplicationComponent component = ((HabitsApplication) getApplicationContext()).getComponent();
        dialog.setThemeDark(component.getPreferences().getTheme() == THEME_DARK);
        dialog.show(getSupportFragmentManager(),"timePicker");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute)
    {
        Calendar calendar = DateUtils.getStartOfTodayCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        Long time = calendar.getTimeInMillis();
        if (DateUtils.getLocalTime() > time)
            time += DateUtils.DAY_LENGTH;
        time = applyTimezone(time);

        Intent intent = new Intent( ReminderReceiver.ACTION_SNOOZE_REMINDER_SET, getIntent().getData(),
                this, ReminderReceiver.class );
        intent.putExtra("reminderTime", time);
        sendBroadcast(intent);

        finish();
    }

    @Override
    public void onTimeCleared(RadialPickerLayout view)
    {
        Intent intent = new Intent( ReminderReceiver.ACTION_DISMISS_REMINDER, getIntent().getData(),
                this, ReminderReceiver.class );
        sendBroadcast(intent);

        finish();
    }
}
