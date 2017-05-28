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

package org.isoron.uhabits.notifications;

import android.content.*;
import android.media.*;
import android.net.*;
import android.preference.*;
import android.provider.*;
import android.support.annotation.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.*;

import javax.inject.*;

import static android.media.RingtoneManager.*;

@AppScope
public class RingtoneManager
{
    private Context context;

    @Inject
    public RingtoneManager(@AppContext @NonNull Context context)
    {
        this.context = context;
    }

    @Nullable
    public static String getRingtoneName(Context context)
    {
        try
        {
            Uri ringtoneUri = getRingtoneUri(context);
            String ringtoneName =
                context.getResources().getString(R.string.none);

            if (ringtoneUri != null)
            {
                Ringtone ringtone = getRingtone(context, ringtoneUri);
                if (ringtone != null)
                {
                    ringtoneName = ringtone.getTitle(context);
                    ringtone.stop();
                }
            }

            return ringtoneName;
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Uri getRingtoneUri(Context context)
    {
        Uri ringtoneUri = null;
        Uri defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String prefRingtoneUri =
            prefs.getString("pref_ringtone_uri", defaultRingtoneUri.toString());
        if (prefRingtoneUri.length() > 0)
            ringtoneUri = Uri.parse(prefRingtoneUri);

        return ringtoneUri;
    }

    public static void parseRingtoneData(Context context, @Nullable Intent data)
    {
        if (data == null) return;

        Uri ringtoneUri = data.getParcelableExtra(EXTRA_RINGTONE_PICKED_URI);

        if (ringtoneUri != null)
        {
            SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
            prefs
                .edit()
                .putString("pref_ringtone_uri", ringtoneUri.toString())
                .apply();
        }
        else
        {
            String off = context.getResources().getString(R.string.none);
            SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString("pref_ringtone_uri", "").apply();
        }
    }

}
