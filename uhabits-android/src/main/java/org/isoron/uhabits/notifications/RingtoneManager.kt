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

package org.isoron.uhabits.notifications

import android.content.*
import android.media.RingtoneManager.*
import android.net.*
import android.preference.*
import android.provider.*
import org.isoron.androidbase.*
import org.isoron.uhabits.R
import org.isoron.uhabits.core.*
import javax.inject.*

@AppScope
class RingtoneManager
@Inject constructor(@AppContext private val context: Context) {

    val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    fun getName(): String? {
        try {
            var ringtoneName = context.resources.getString(R.string.none)
            val ringtoneUri = getURI()
            if (ringtoneUri != null) {
                val ringtone = getRingtone(context, ringtoneUri)
                if (ringtone != null) ringtoneName = ringtone.getTitle(context)
            }
            return ringtoneName
        } catch (e: RuntimeException) {
            e.printStackTrace()
            return null
        }
    }

    fun getURI(): Uri? {
        var ringtoneUri: Uri? = null
        val defaultRingtoneUri = Settings.System.DEFAULT_NOTIFICATION_URI
        val prefRingtoneUri = prefs.getString("pref_ringtone_uri",
                                              defaultRingtoneUri.toString())
        if (prefRingtoneUri.isNotEmpty())
            ringtoneUri = Uri.parse(prefRingtoneUri)

        return ringtoneUri
    }

    fun update(data: Intent?) {
        if (data == null) return
        val ringtoneUri = data.getParcelableExtra<Uri>(EXTRA_RINGTONE_PICKED_URI)
        if (ringtoneUri != null) {
            prefs.edit().putString("pref_ringtone_uri", ringtoneUri.toString()).apply()
        } else {
            prefs.edit().putString("pref_ringtone_uri", "").apply()
        }
    }
}
