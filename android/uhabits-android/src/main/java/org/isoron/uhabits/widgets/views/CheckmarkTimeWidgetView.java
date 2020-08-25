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

package org.isoron.uhabits.widgets.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.isoron.uhabits.R;
import org.isoron.uhabits.activities.common.views.RingView;
import org.isoron.uhabits.activities.habits.list.views.NumberButtonViewKt;
import org.isoron.uhabits.core.models.Checkmark;
import org.isoron.uhabits.core.models.Repetition;
import org.isoron.uhabits.core.models.Timestamp;
import org.isoron.uhabits.utils.PaletteUtils;

public class CheckmarkTimeWidgetView extends CheckmarkWidgetView {
    protected Repetition newest;

    public CheckmarkTimeWidgetView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkTimeWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void setNewest(Repetition newest) {
        this.newest = newest;
    }

    @Override
    protected String getText()
    {
        if (isNumerical) return NumberButtonViewKt.toShortString(checkmarkValue / 1000.0);
        switch (checkmarkState) {
            case Checkmark.CHECKED_EXPLICITLY:
                return getResources().getString(R.string.fa_check);
            case Checkmark.CHECKED_IMPLICITLY:
                return newest == null ? getResources().getString(R.string.fa_check) : newest.getTimestamp().daysUntil(new Timestamp(System.currentTimeMillis())) + getResources().getString(R.string.fa_check);
            case Checkmark.SKIPPED:
                return getResources().getString(R.string.fa_skipped);
            case Checkmark.UNCHECKED:
            default:
                return newest == null ? "0" : String.valueOf(newest.getTimestamp().daysUntil(new Timestamp(System.currentTimeMillis())));
        }
    }

    private void init()
    {
        ring = (RingView) findViewById(R.id.scoreRing);
        label = (TextView) findViewById(R.id.label);

        if (ring != null) ring.setIsTransparencyEnabled(true);

        if (isInEditMode())
        {
            percentage = 0.75f;
            name = "Wake up early";
            activeColor = PaletteUtils.getAndroidTestColor(6);
            checkmarkValue = Checkmark.CHECKED_EXPLICITLY;
            refresh();
        }
    }
}