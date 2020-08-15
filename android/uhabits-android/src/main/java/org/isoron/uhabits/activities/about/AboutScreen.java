/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.about;

import android.widget.*;

import androidx.annotation.*;

import org.isoron.androidbase.activities.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.intents.*;

public class AboutScreen extends BaseScreen
{
    @NonNull
    private final Preferences prefs;

    private int developerCountdown = 5;

    @NonNull
    private final IntentFactory intents;

    public AboutScreen(@NonNull BaseActivity activity,
                       @NonNull IntentFactory intents,
                       @NonNull Preferences prefs)
    {
        super(activity);
        this.intents = intents;
        this.prefs = prefs;
    }

    public void showRateAppWebsite()
    {
        activity.startActivity(intents.rateApp(activity));
    }

    public void showSendFeedbackScreen()
    {
        activity.startActivity(intents.sendFeedback(activity));
    }

    public void showSourceCodeWebsite()
    {
        activity.startActivity(intents.viewSourceCode(activity));
    }

    public void showTranslationWebsite()
    {
        activity.startActivity(intents.helpTranslate(activity));
    }

    public void showPrivacyPolicyWebsite()
    {
        activity.startActivity(intents.privacyPolicy(activity));
    }

    public void showCodeContributorsWebsite()
    {
        activity.startActivity(intents.codeContributors(activity));
    }

    public void onPressDeveloperCountdown()
    {
        developerCountdown--;
        if (developerCountdown == 0) {
            prefs.setDeveloper(true);
            Toast.makeText(activity, "You are now a developer", Toast.LENGTH_LONG).show();
        }
    }
}
