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

import android.view.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.utils.*;

public class ShowHabitHelper
{
    private ShowHabitFragment fragment;

    public ShowHabitHelper(ShowHabitFragment fragment)
    {
        this.fragment = fragment;
    }

    void updateCardHeaders(View view)
    {
        updateColor(view, R.id.tvHistory);
        updateColor(view, R.id.tvStrength);
        updateColor(view, R.id.tvStreaks);
        updateColor(view, R.id.tvWeekdayFreq);
        updateColor(view, R.id.scoreLabel);
    }

    void updateColor(View view, int viewId)
    {
        if (fragment.habit == null || fragment.activity == null) return;

        TextView textView = (TextView) view.findViewById(viewId);
        int androidColor =
            ColorUtils.getColor(fragment.activity, fragment.habit.getColor());
        textView.setTextColor(androidColor);
    }

    void updateColors()
    {
        fragment.activeColor = ColorUtils.getColor(fragment.getContext(),
            fragment.habit.getColor());
        fragment.inactiveColor =
            InterfaceUtils.getStyledColor(fragment.getContext(),
                R.attr.mediumContrastTextColor);
    }
}
