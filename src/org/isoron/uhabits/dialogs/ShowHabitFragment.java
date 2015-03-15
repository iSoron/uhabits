package org.isoron.uhabits.dialogs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ShowHabitActivity;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.HabitHistoryView;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ShowHabitFragment extends Fragment
{
	protected ShowHabitActivity activity;
	private Habit habit;

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		Log.d("ShowHabitActivity", "Creating view...");

		View view = inflater.inflate(R.layout.show_habit, container, false);
		activity = (ShowHabitActivity) getActivity();
		habit = activity.habit;

		int darkerHabitColor = ColorHelper.mixColors(habit.color, Color.BLACK, 0.75f);
		activity.getWindow().setStatusBarColor(darkerHabitColor);

		TextView tvHistory = (TextView) view.findViewById(R.id.tvHistory);
		TextView tvOverview = (TextView) view.findViewById(R.id.tvOverview);
		tvHistory.setTextColor(habit.color);
		tvOverview.setTextColor(habit.color);
		
		TextView tvStrength = (TextView) view.findViewById(R.id.tvStrength);
		tvStrength.setText(String.format("%.2f%%", ((float) habit.getScore() / Habit.MAX_SCORE) * 100));
		
		LinearLayout llHistory = (LinearLayout) view.findViewById(R.id.llHistory);

		HabitHistoryView hhv = new HabitHistoryView(activity, habit, 40);
		llHistory.addView(hhv);
			
		return view;
	}
}
