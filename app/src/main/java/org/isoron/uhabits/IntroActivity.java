/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2
{
    @Override
    public void init(Bundle savedInstanceState)
    {
        showStatusBar(false);

        addSlide(AppIntroFragment.newInstance("Welcome",
                "Habits Tracker helps you create and maintain good habits.", R.drawable.tutorial_1,
                Color.parseColor("#194673")));

        addSlide(AppIntroFragment.newInstance("Create some new habits",
                "Every day, after performing your habit, put a checkmark on the app.",
                R.drawable.tutorial_2, Color.parseColor("#ffa726")));

        addSlide(AppIntroFragment.newInstance("Keep doing it",
                "Habits performed consistently for a long time will earn a full star.",
                R.drawable.tutorial_3, Color.parseColor("#7cb342")));

        addSlide(AppIntroFragment.newInstance("Track your progress",
                "Detailed graphs show you how your habits improved over time.",
                R.drawable.tutorial_4, Color.parseColor("#9575cd")));
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
