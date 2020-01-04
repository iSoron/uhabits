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

package org.isoron.uhabits.activities.habits.edit.views;

import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import org.isoron.uhabits.R;
import org.isoron.uhabits.core.models.*;

import butterknife.*;

import static org.isoron.uhabits.R.id.*;


public class FrequencyPanel extends FrameLayout
{
    @BindView(numerator)
    TextView tvNumerator;

    @BindView(R.id.denominator)
    TextView tvDenominator;

    @BindView(R.id.spinner)
    Spinner spinner;

    @BindView(R.id.customFreqPanel)
    ViewGroup customFreqPanel;

    public FrequencyPanel(@NonNull Context context,
                          @Nullable AttributeSet attrs)
    {
        super(context, attrs);

        View view = inflate(context, R.layout.edit_habit_frequency, null);
        ButterKnife.bind(this, view);
        addView(view);
    }

    @NonNull
    public Frequency getFrequency()
    {
        String freqNum = tvNumerator.getText().toString();
        String freqDen = tvDenominator.getText().toString();

        if (!freqNum.isEmpty() && !freqDen.isEmpty())
        {
            int numerator = Integer.parseInt(freqNum);
            int denominator = Integer.parseInt(freqDen);
            return new Frequency(numerator, denominator);
        }

        return Frequency.DAILY;
    }

    @SuppressLint("SetTextI18n")
    public void setFrequency(@NonNull Frequency freq)
    {
        int position = getQuickSelectPosition(freq);

        if (position >= 0) showSimplifiedFrequency(position);
        else showCustomFrequency();

        tvNumerator.setText(Integer.toString(freq.getNumerator()));
        tvDenominator.setText(Integer.toString(freq.getDenominator()));
    }

    @OnItemSelected(R.id.spinner)
    public void onFrequencySelected(int position)
    {
        if (position < 0 || position > 4) throw new IllegalArgumentException();
        int freqNums[] = { 1, 1, 2, 5, 3 };
        int freqDens[] = { 1, 7, 7, 7, 7 };
        setFrequency(new Frequency(freqNums[position], freqDens[position]));
    }

    public boolean validate()
    {
        boolean valid = true;
        Resources res = getResources();

        String freqNum = tvNumerator.getText().toString();
        String freqDen = tvDenominator.getText().toString();

        if (freqDen.isEmpty())
        {
            tvDenominator.setError(
                res.getString(R.string.validation_show_not_be_blank));
            valid = false;
        }

        if (freqNum.isEmpty())
        {
            tvNumerator.setError(
                res.getString(R.string.validation_show_not_be_blank));
            valid = false;
        }

        if (!valid) return false;

        int numerator = Integer.parseInt(freqNum);
        int denominator = Integer.parseInt(freqDen);

        if (numerator <= 0)
        {
            tvNumerator.setError(
                res.getString(R.string.validation_number_should_be_positive));
            valid = false;
        }

        if (numerator > denominator)
        {
            tvNumerator.setError(
                res.getString(R.string.validation_at_most_one_rep_per_day));
            valid = false;
        }

        return valid;
    }

    private int getQuickSelectPosition(@NonNull Frequency freq)
    {
        if (freq.equals(Frequency.DAILY)) return 0;
        if (freq.equals(Frequency.WEEKLY)) return 1;
        if (freq.equals(Frequency.TWO_TIMES_PER_WEEK)) return 2;
        if (freq.equals(Frequency.FIVE_TIMES_PER_WEEK)) return 3;
        return -1;
    }

    private void showCustomFrequency()
    {
        spinner.setVisibility(View.GONE);
        customFreqPanel.setVisibility(View.VISIBLE);
    }

    private void showSimplifiedFrequency(int quickSelectPosition)
    {
        spinner.setVisibility(View.VISIBLE);
        spinner.setSelection(quickSelectPosition);
        customFreqPanel.setVisibility(View.GONE);
    }
}
