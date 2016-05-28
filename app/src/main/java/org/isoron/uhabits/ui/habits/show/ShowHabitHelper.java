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

package org.isoron.uhabits.ui.habits.show;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Score;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.DateUtils;
import org.isoron.uhabits.utils.InterfaceUtils;
import org.isoron.uhabits.views.RingView;

public class ShowHabitHelper
{
    private ShowHabitFragment fragment;

    public ShowHabitHelper(ShowHabitFragment fragment)
    {
        this.fragment = fragment;
    }

    String getFreqText()
    {
        if(fragment.habit == null) return "";

        Resources resources = fragment.getResources();
        Integer freqNum = fragment.habit.freqNum;
        Integer freqDen = fragment.habit.freqDen;

        if (freqNum.equals(freqDen)) return resources.getString(R.string.every_day);

        if (freqNum == 1)
        {
            if (freqDen == 7) return resources.getString(R.string.every_week);
            if (freqDen % 7 == 0) return resources.getString(R.string.every_x_weeks, freqDen / 7);
            return resources.getString(R.string.every_x_days, freqDen);
        }

        String times_every = resources.getString(R.string.time_every);
        return String.format("%d %s %d %s", freqNum, times_every, freqDen,
                resources.getString(R.string.days));
    }

    void updateScore(View view)
    {
        if (fragment.habit == null) return;
        if (view == null) return;

        float todayPercentage = fragment.todayScore / Score.MAX_VALUE;
        float monthDiff = todayPercentage - (fragment.lastMonthScore / Score.MAX_VALUE);
        float yearDiff = todayPercentage - (fragment.lastYearScore / Score.MAX_VALUE);

        RingView scoreRing = (RingView) view.findViewById(R.id.scoreRing);
        int androidColor = ColorUtils.getColor(fragment.getActivity(), fragment.habit.color);
        scoreRing.setColor(androidColor);
        scoreRing.setPercentage(todayPercentage);

        TextView scoreLabel = (TextView) view.findViewById(R.id.scoreLabel);
        TextView monthDiffLabel = (TextView) view.findViewById(R.id.monthDiffLabel);
        TextView yearDiffLabel = (TextView) view.findViewById(R.id.yearDiffLabel);

        scoreLabel.setText(String.format("%.0f%%", todayPercentage * 100));

        String minus = "\u2212";
        monthDiffLabel.setText(String.format("%s%.0f%%", (monthDiff >= 0 ? "+" : minus),
                Math.abs(monthDiff) * 100));
        yearDiffLabel.setText(
                String.format("%s%.0f%%", (yearDiff >= 0 ? "+" : minus), Math.abs(yearDiff) * 100));

        monthDiffLabel.setTextColor(monthDiff >= 0 ? fragment.activeColor : fragment.inactiveColor);
        yearDiffLabel.setTextColor(yearDiff >= 0 ? fragment.activeColor : fragment.inactiveColor);
    }

    void updateMainHeader(View view)
    {
        if (fragment.habit == null) return;

        TextView questionLabel = (TextView) view.findViewById(R.id.questionLabel);
        questionLabel.setTextColor(fragment.activeColor);
        questionLabel.setText(fragment.habit.description);

        TextView reminderLabel = (TextView) view.findViewById(R.id.reminderLabel);
        if (fragment.habit.hasReminder()) reminderLabel.setText(
                DateUtils.formatTime(fragment.getActivity(), fragment.habit.reminderHour,
                        fragment.habit.reminderMin));
        else reminderLabel.setText(fragment.getResources().getString(R.string.reminder_off));

        TextView frequencyLabel = (TextView) view.findViewById(R.id.frequencyLabel);
        frequencyLabel.setText(getFreqText());

        if (fragment.habit.description.isEmpty()) questionLabel.setVisibility(View.GONE);
    }

    void updateCardHeaders(View view)
    {
        updateColor(view, R.id.tvHistory);
        updateColor(view, R.id.tvOverview);
        updateColor(view, R.id.tvStrength);
        updateColor(view, R.id.tvStreaks);
        updateColor(view, R.id.tvWeekdayFreq);
        updateColor(view, R.id.scoreLabel);
    }

    void updateColor(View view, int viewId)
    {
        if(fragment.habit == null || fragment.activity == null) return;

        TextView textView = (TextView) view.findViewById(viewId);
        int androidColor = ColorUtils.getColor(fragment.activity, fragment.habit.color);
        textView.setTextColor(androidColor);
    }

    void updateColors()
    {
        fragment.activeColor = ColorUtils.getColor(fragment.getContext(), fragment.habit.color);
        fragment.inactiveColor = InterfaceUtils.getStyledColor(fragment.getContext(),
                R.attr.mediumContrastTextColor);
    }
}
