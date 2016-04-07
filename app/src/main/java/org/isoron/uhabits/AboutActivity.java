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

package org.isoron.uhabits;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.isoron.uhabits.helpers.ColorHelper;

public class AboutActivity extends Activity implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            int color = getResources().getColor(R.color.blue_700);
            int darkerColor = ColorHelper.mixColors(color, Color.BLACK, 0.75f);
            getActionBar().setBackgroundDrawable(new ColorDrawable(color));
            getWindow().setStatusBarColor(darkerColor);
        }

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        TextView tvRate = (TextView) findViewById(R.id.tvRate);
        TextView tvFeedback = (TextView) findViewById(R.id.tvFeedback);
        TextView tvSource = (TextView) findViewById(R.id.tvSource);

        tvVersion.setText(String.format(getResources().getString(R.string.version_n),
                BuildConfig.VERSION_NAME));
        tvRate.setOnClickListener(this);
        tvFeedback.setOnClickListener(this);
        tvSource.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvRate:
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.playStoreURL)));
                startActivity(intent);
                break;
            }

            case R.id.tvFeedback:
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(getString(R.string.feedbackURL)));
                startActivity(intent);
                break;
            }

            case R.id.tvSource:
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.sourceCodeURL)));
                startActivity(intent);
                break;
            }
        }
    }
}
