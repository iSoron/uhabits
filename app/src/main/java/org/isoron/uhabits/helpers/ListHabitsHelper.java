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

package org.isoron.uhabits.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.models.Score;

import java.util.GregorianCalendar;

public class ListHabitsHelper
{
    public static final int INACTIVE_COLOR = Color.rgb(200, 200, 200);
    public static final int INACTIVE_CHECKMARK_COLOR = Color.rgb(230, 230, 230);

    private final Context context;
    private final HabitListLoader loader;
    private Typeface fontawesome;

    public ListHabitsHelper(Context context, HabitListLoader loader)
    {
        this.context = context;
        this.loader = loader;

        fontawesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
    }

    public Typeface getFontawesome()
    {
        return fontawesome;
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

    public void initializeLabelAndIcon(View itemView)
    {
        TextView tvStar = (TextView) itemView.findViewById(R.id.tvStar);
        tvStar.setTypeface(getFontawesome());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getHabitNameWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        itemView.findViewById(R.id.label).setLayoutParams(params);
    }

    public void updateNameAndIcon(Habit habit, TextView tvStar, TextView tvName)
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

    public void updateHabitBackground(View view, boolean isSelected)
    {
        if (isSelected)
            view.setBackgroundResource(R.drawable.selected_box);
        else
        {
            if (android.os.Build.VERSION.SDK_INT >= 21)
                view.setBackgroundResource(R.drawable.ripple_white);
            else view.setBackgroundResource(R.drawable.card_background);
        }
    }

    public void inflateCheckmarkButtons(View view, View.OnLongClickListener onLongClickListener,
                                        View.OnClickListener onClickListener, LayoutInflater inflater)
    {
        for (int i = 0; i < getButtonCount(); i++)
        {
            View check = inflater.inflate(R.layout.list_habits_item_check, null);
            TextView btCheck = (TextView) check.findViewById(R.id.tvCheck);
            btCheck.setTypeface(fontawesome);
            btCheck.setOnLongClickListener(onLongClickListener);
            btCheck.setOnClickListener(onClickListener);
            ((LinearLayout) view.findViewById(R.id.llButtons)).addView(check);
        }

        view.setTag(R.id.timestamp_key, DateHelper.getStartOfToday());
    }

    public void updateHeader(ViewGroup header)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        GregorianCalendar day = DateHelper.getStartOfTodayCalendar();
        header.removeAllViews();

        for (int i = 0; i < getButtonCount(); i++)
        {
            View tvDay = inflater.inflate(R.layout.list_habits_header_check, null);
            Button btCheck = (Button) tvDay.findViewById(R.id.tvCheck);
            btCheck.setText(DateHelper.formatHeaderDate(day));
            header.addView(tvDay);

            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    public void updateEmptyMessage(View view)
    {
        if (loader.getLastLoadTimestamp() == null) view.setVisibility(View.GONE);
        else view.setVisibility(loader.habits.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void toggleCheckmarkView(View v, Habit habit)
    {
        if (v.getTag(R.string.toggle_key).equals(2))
            updateCheckmark(habit.color, (TextView) v, 0);
        else
            updateCheckmark(habit.color, (TextView) v, 2);
    }
}
