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

package org.isoron.uhabits.intents;

import android.content.*;
import android.net.*;
import android.support.annotation.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.activities.about.*;
import org.isoron.uhabits.activities.habits.show.*;
import org.isoron.uhabits.activities.intro.*;
import org.isoron.uhabits.activities.settings.*;
import org.isoron.uhabits.models.*;

import javax.inject.*;

public class IntentFactory
{
    @Inject
    public IntentFactory()
    {
    }

    public Intent helpTranslate(Context context)
    {
        String url = context.getString(R.string.translateURL);
        return buildViewIntent(url);
    }

    public Intent rateApp(Context context)
    {
        String url = context.getString(R.string.playStoreURL);
        return buildViewIntent(url);
    }

    public Intent sendFeedback(Context context)
    {
        String url = context.getString(R.string.feedbackURL);
        return buildSendToIntent(url);
    }

    public Intent startAboutActivity(Context context)
    {
        return new Intent(context, AboutActivity.class);
    }

    public Intent startIntroActivity(Context context)
    {
        return new Intent(context, IntroActivity.class);
    }

    public Intent startSettingsActivity(Context context)
    {
        return new Intent(context, SettingsActivity.class);
    }

    public Intent startShowHabitActivity(Context context, Habit habit)
    {
        Intent intent = new Intent(context, ShowHabitActivity.class);
        intent.setData(habit.getUri());
        return intent;
    }

    public Intent viewFAQ(Context context)
    {
        String url = context.getString(R.string.helpURL);
        return buildViewIntent(url);
    }

    public Intent viewSourceCode(Context context)
    {
        String url = context.getString(R.string.sourceCodeURL);
        return buildViewIntent(url);
    }

    @NonNull
    private Intent buildSendToIntent(String url)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(url));
        return intent;
    }

    @NonNull
    private Intent buildViewIntent(String url)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        return intent;
    }
}
