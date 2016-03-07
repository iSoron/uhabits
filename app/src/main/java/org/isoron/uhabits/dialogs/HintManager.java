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

package org.isoron.uhabits.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import org.isoron.helpers.DateHelper;
import org.isoron.uhabits.R;

public class HintManager
{
    private Context context;
    private SharedPreferences prefs;
    private View hintView;

    public HintManager(Context context, View hintView)
    {
        this.context = context;
        this.hintView = hintView;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void dismissHint()
    {
        hintView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                hintView.setVisibility(View.GONE);
            }
        });
    }

    public void showHintIfAppropriate()
    {
        Integer lastHintNumber = prefs.getInt("last_hint_number", -1);
        Long lastHintTimestamp = prefs.getLong("last_hint_timestamp", -1);

        if (DateHelper.getStartOfToday() > lastHintTimestamp) showHint(lastHintNumber + 1);
    }

    private void showHint(int hintNumber)
    {
        String[] hints = context.getResources().getStringArray(R.array.hints);
        if (hintNumber >= hints.length) return;

        prefs.edit().putInt("last_hint_number", hintNumber).apply();
        prefs.edit().putLong("last_hint_timestamp", DateHelper.getStartOfToday()).apply();

        TextView tvContent = (TextView) hintView.findViewById(R.id.hintContent);
        tvContent.setText(hints[hintNumber]);

        hintView.setAlpha(0.0f);
        hintView.setVisibility(View.VISIBLE);
        hintView.animate().alpha(1f).setDuration(500);
    }
}
