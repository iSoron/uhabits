package org.isoron.uhabits.dialogs;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.Command;
import org.isoron.helpers.DialogHelper.OnSavedListener;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

public class EditHabitFragment extends DialogFragment implements OnClickListener
{
	private int mode;
	static final int EDIT_MODE = 0;
	static final int CREATE_MODE = 1;

	private OnSavedListener onSavedListener;

	private Habit originalHabit, modified_habit;
	private TextView tvName, tvDescription, tvFreqNum, tvFreqDen, tvInputReminder;

	private SharedPreferences prefs;

	static class SolidColorMatrix extends ColorMatrix
	{
		public SolidColorMatrix(int color)
		{
			float matrix[] = { 0.0f, 0.0f, 0.0f, 0.0f, Color.red(color), 0.0f, 0.0f, 0.0f, 0.0f,
					Color.green(color), 0.0f, 0.0f, 0.0f, 0.0f, Color.blue(color), 0.0f, 0.0f,
					0.0f, 1.0f, 0 };
			set(matrix);
		}
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                    Factory                                    *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	static EditHabitFragment editSingleHabitFragment(long id)
	{
		EditHabitFragment frag = new EditHabitFragment();
		Bundle args = new Bundle();
		args.putLong("habitId", id);
		args.putInt("editMode", EDIT_MODE);
		frag.setArguments(args);
		return frag;
	}

	static EditHabitFragment createHabitFragment()
	{
		EditHabitFragment frag = new EditHabitFragment();
		Bundle args = new Bundle();
		args.putInt("editMode", CREATE_MODE);
		frag.setArguments(args);
		return frag;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                   Creation                                    *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.edit_habit, container, false);
		tvName = (TextView) view.findViewById(R.id.input_name);
		tvDescription = (TextView) view.findViewById(R.id.input_description);
		tvFreqNum = (TextView) view.findViewById(R.id.input_freq_num);
		tvFreqDen = (TextView) view.findViewById(R.id.input_freq_den);
		tvInputReminder = (TextView) view.findViewById(R.id.input_reminder_time);

		Button buttonSave = (Button) view.findViewById(R.id.buttonSave);
		Button buttonDiscard = (Button) view.findViewById(R.id.buttonDiscard);

		buttonSave.setOnClickListener(this);
		buttonDiscard.setOnClickListener(this);
		tvInputReminder.setOnClickListener(this);

		ImageButton buttonPickColor = (ImageButton) view.findViewById(R.id.button_pick_color);

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		Bundle args = getArguments();
		mode = (Integer) args.get("editMode");

		if(mode == CREATE_MODE)
		{
			getDialog().setTitle("Create habit");
			modified_habit = new Habit();

			int defaultNum = prefs.getInt("pref_default_habit_freq_num", modified_habit.freq_num);
			int defaultDen = prefs.getInt("pref_default_habit_freq_den", modified_habit.freq_den);
			int defaultColor = prefs.getInt("pref_default_habit_color", modified_habit.color);

			modified_habit.color = defaultColor;
			modified_habit.freq_num = defaultNum;
			modified_habit.freq_den = defaultDen;
        }
		else if(mode == EDIT_MODE)
		{
			originalHabit = Habit.get((Long) args.get("habitId"));
			modified_habit = new Habit(originalHabit);

			getDialog().setTitle("Edit habit");
			tvName.append(modified_habit.name);
			tvDescription.append(modified_habit.description);
		}

		tvFreqNum.append(modified_habit.freq_num.toString());
		tvFreqDen.append(modified_habit.freq_den.toString());

		changeColor(modified_habit.color);
		updateReminder();

		buttonPickColor.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				ColorPickerDialog picker = ColorPickerDialog.newInstance(
						R.string.color_picker_default_title,
						ColorHelper.palette, modified_habit.color, 4, ColorPickerDialog.SIZE_SMALL);

				picker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
				{
					public void onColorSelected(int color)
					{
						changeColor(color);
					}
				});
				picker.show(getFragmentManager(), "picker");
			}
		});

		return view;
	}

	private void changeColor(Integer color)
	{
		modified_habit.color = color;
		tvName.setTextColor(color);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("pref_default_habit_color", color);
		editor.apply();
	}

	private void updateReminder()
	{
		if(modified_habit.reminder_hour != null)
		{
			tvInputReminder.setTextColor(Color.BLACK);
			tvInputReminder.setText(String.format("%02d:%02d", modified_habit.reminder_hour,
					modified_habit.reminder_min));
		}
		else
		{
			tvInputReminder.setTextColor(Color.GRAY);
			tvInputReminder.setText("Off");
		}
	}

	public void setOnSavedListener(OnSavedListener onSavedListener)
	{
		this.onSavedListener = onSavedListener;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 *                                    Callback                                   *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		
		/* Due date spinner */
		if(id == R.id.input_reminder_time)
		{
			int default_hour = 8;
			int default_min  = 0;

			if(modified_habit.reminder_hour != null) {
				default_hour = modified_habit.reminder_hour;
				default_min = modified_habit.reminder_min;
			}

			TimePickerDialog timePicker = TimePickerDialog.newInstance(new OnTimeSetListener()
			{

				@Override
				public void onTimeSet(RadialPickerLayout view, int hour, int minute)
				{
					modified_habit.reminder_hour = hour;
					modified_habit.reminder_min = minute;
					updateReminder();
				}

				@Override
				public void onTimeCleared(RadialPickerLayout view)
				{
					modified_habit.reminder_hour = null;
					modified_habit.reminder_min = null;
					updateReminder();
				}
			}, default_hour, default_min, true);
			timePicker.show(getFragmentManager(), "timePicker");
		}

		/* Save button */
		if(id == R.id.buttonSave)
		{
			Command command = null;

			modified_habit.name = tvName.getText().toString().trim();
			modified_habit.description = tvDescription.getText().toString().trim();
			modified_habit.freq_num = Integer.parseInt(tvFreqNum.getText().toString());
			modified_habit.freq_den = Integer.parseInt(tvFreqDen.getText().toString());

			Boolean valid = true;

			if(modified_habit.name.length() == 0)
			{
				tvName.setError("Name cannot be blank.");
				valid = false;
			}

			if(modified_habit.freq_den <= 0)
			{
				tvFreqNum.setError("Number must be positive.");
				valid = false;
			}

			if(modified_habit.freq_num > modified_habit.freq_den)
			{
				tvFreqNum.setError("You can have at most one repetition per day");
				valid = false;
			}

			if(!valid)
				return;

			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("pref_default_habit_freq_num", modified_habit.freq_num);
			editor.putInt("pref_default_habit_freq_den", modified_habit.freq_den);
			editor.apply();

			Habit savedHabit = null;

			if(mode == EDIT_MODE)
			{
				command = originalHabit.new EditCommand(modified_habit);
				savedHabit = originalHabit;
			}

			if(mode == CREATE_MODE)
				command = new Habit.CreateCommand(modified_habit);

			if(onSavedListener != null)
				onSavedListener.onSaved(command, savedHabit);

			dismiss();
		}

		/* Discard button */
		if(id == R.id.buttonDiscard)
		{
			dismiss();
		}
	}
}
