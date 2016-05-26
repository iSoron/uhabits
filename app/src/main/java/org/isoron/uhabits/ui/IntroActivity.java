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

package org.isoron.uhabits.ui;

import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.isoron.uhabits.R;

public class IntroActivity extends AppIntro2
{
    @Override
    public void init(Bundle savedInstanceState)
    {
        showStatusBar(false);

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_1),
                getString(R.string.intro_description_1), R.drawable.intro_icon_1,
                Color.parseColor("#194673")));

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_2),
                getString(R.string.intro_description_2), R.drawable.intro_icon_2,
                Color.parseColor("#ffa726")));

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_4),
                getString(R.string.intro_description_4), R.drawable.intro_icon_4,
                Color.parseColor("#9575cd")));
    }

    @Override
    public void onNextPressed()
    {
    }

    @Override
    public void onDonePressed()
    {
        finish();
    }

    @Override
    public void onSlideChanged()
    {
    }
}
