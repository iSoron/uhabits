package org.isoron.uhabits.helpers;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

public class ListHabitsHelper
{
    public static final int INACTIVE_COLOR = Color.rgb(200, 200, 200);
    public static final int INACTIVE_CHECKMARK_COLOR = Color.rgb(230, 230, 230);

    private final Context context;
    private final HabitListLoader loader;

    public ListHabitsHelper(Context context, HabitListLoader loader)
    {
        this.context = context;
        this.loader = loader;
    }

    public int getButtonCount()
    {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        return Math.max(0, (int) ((width - 160) / 42.0));
    }

    public int getHabitNameWidth()
    {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / dm.density);
        return (int) ((width - 30 - getButtonCount() * 42) * dm.density);
    }

    public void updateCheckmarkButtons(Habit habit, LinearLayout llButtons)
    {
        int activeColor = getActiveColor(habit);
        int m = llButtons.getChildCount();
        Long habitId = habit.getId();

        int isChecked[] = loader.checkmarks.get(habitId);

        for (int i = 0; i < m; i++)
        {

            TextView tvCheck = (TextView) llButtons.getChildAt(i);
            tvCheck.setTag(R.string.habit_key, habitId);
            tvCheck.setTag(R.string.offset_key, i);
            if(isChecked.length > i)
                updateCheckmark(activeColor, tvCheck, isChecked[i]);
        }
    }

    public int getActiveColor(Habit habit)
    {
        int activeColor = habit.color;
        if(habit.isArchived()) activeColor = INACTIVE_COLOR;

        return activeColor;
    }

    public void updateNameAndIcon(Habit habit, TextView tvStar,
                                          TextView tvName)
    {
        int activeColor = getActiveColor(habit);

        tvName.setText(habit.name);
        tvName.setTextColor(activeColor);

        if (habit.isArchived())
        {
            tvStar.setText(context.getString(R.string.fa_archive));
            tvStar.setTextColor(activeColor);
        }
        else
        {
            int score = loader.scores.get(habit.getId());

            if (score < Score.HALF_STAR_CUTOFF)
            {
                tvStar.setText(context.getString(R.string.fa_star_o));
                tvStar.setTextColor(INACTIVE_COLOR);
            }
            else if (score < Score.FULL_STAR_CUTOFF)
            {
                tvStar.setText(context.getString(R.string.fa_star_half_o));
                tvStar.setTextColor(INACTIVE_COLOR);
            }
            else
            {
                tvStar.setText(context.getString(R.string.fa_star));
                tvStar.setTextColor(activeColor);
            }
        }
    }

    public void updateCheckmark(int activeColor, TextView tvCheck, int check)
    {
        switch (check)
        {
            case 2:
                tvCheck.setText(R.string.fa_check);
                tvCheck.setTextColor(activeColor);
                tvCheck.setTag(R.string.toggle_key, 2);
                break;

            case 1:
                tvCheck.setText(R.string.fa_check);
                tvCheck.setTextColor(INACTIVE_CHECKMARK_COLOR);
                tvCheck.setTag(R.string.toggle_key, 1);
                break;

            case 0:
                tvCheck.setText(R.string.fa_times);
                tvCheck.setTextColor(INACTIVE_CHECKMARK_COLOR);
                tvCheck.setTag(R.string.toggle_key, 0);
                break;
        }
    }
}
