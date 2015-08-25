package org.isoron.uhabits.dialogs;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.helpers.ColorHelper;
import org.isoron.uhabits.R;
import org.isoron.uhabits.ShowHabitActivity;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.views.HabitHistoryView;
import org.isoron.uhabits.views.HabitScoreView;
import org.isoron.uhabits.views.HabitStreakView;
import org.isoron.uhabits.views.RingView;

public class ShowHabitFragment extends Fragment
{
    protected ShowHabitActivity activity;

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
        Habit habit = activity.habit;

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            int darkerHabitColor = ColorHelper.mixColors(habit.color, Color.BLACK, 0.75f);
            activity.getWindow().setStatusBarColor(darkerHabitColor);
        }

        TextView tvHistory = (TextView) view.findViewById(R.id.tvHistory);
        TextView tvOverview = (TextView) view.findViewById(R.id.tvOverview);
        TextView tvStrength = (TextView) view.findViewById(R.id.tvStrength);
        TextView tvStreaks = (TextView) view.findViewById(R.id.tvStreaks);
        tvHistory.setTextColor(habit.color);
        tvOverview.setTextColor(habit.color);
        tvStrength.setTextColor(habit.color);
        tvStreaks.setTextColor(habit.color);

        LinearLayout llOverview = (LinearLayout) view.findViewById(R.id.llOverview);
        llOverview.addView(new RingView(activity,
                (int) activity.getResources().getDimension(R.dimen.small_square_size) * 4, habit.color,
                ((float) habit.getScore() / Habit.MAX_SCORE), "Habit strength"));

        LinearLayout llStrength = (LinearLayout) view.findViewById(R.id.llStrength);
        llStrength.addView(new HabitScoreView(activity, habit,
                (int) activity.getResources().getDimension(R.dimen.small_square_size)));

        LinearLayout llHistory = (LinearLayout) view.findViewById(R.id.llHistory);
        HabitHistoryView hhv = new HabitHistoryView(activity, habit,
                (int) activity.getResources().getDimension(R.dimen.small_square_size));
        llHistory.addView(hhv);

        LinearLayout llStreaks = (LinearLayout) view.findViewById(R.id.llStreaks);
        HabitStreakView hsv = new HabitStreakView(activity, habit,
                (int) activity.getResources().getDimension(R.dimen.small_square_size));
        llStreaks.addView(hsv);

        return view;
    }
}
