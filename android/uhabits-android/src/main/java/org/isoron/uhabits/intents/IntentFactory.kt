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

package org.isoron.uhabits.intents

import android.content.*
import android.net.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.about.*
import org.isoron.uhabits.activities.habits.show.*
import org.isoron.uhabits.activities.intro.*
import org.isoron.uhabits.activities.settings.*
import org.isoron.uhabits.core.models.*
import javax.inject.*

class IntentFactory
@Inject constructor() {

    fun helpTranslate(context: Context) =
            buildViewIntent(context.getString(R.string.translateURL))

    fun openDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    fun rateApp(context: Context) =
            buildViewIntent(context.getString(R.string.playStoreURL))

    fun sendFeedback(context: Context) =
            buildSendToIntent(context.getString(R.string.feedbackURL))

    fun startAboutActivity(context: Context) =
            Intent(context, AboutActivity::class.java)

    fun startIntroActivity(context: Context) =
            Intent(context, IntroActivity::class.java)

    fun startSettingsActivity(context: Context) =
            Intent(context, SettingsActivity::class.java)

    fun startShowHabitActivity(context: Context, habit: Habit) =
            Intent(context, ShowHabitActivity::class.java).apply {
                data = Uri.parse(habit.uriString)
            }

    fun viewFAQ(context: Context) =
            buildViewIntent(context.getString(R.string.helpURL))

    fun viewSourceCode(context: Context) =
            buildViewIntent(context.getString(R.string.sourceCodeURL))

    private fun buildSendToIntent(url: String) = Intent().apply {
        action = Intent.ACTION_SENDTO
        data = Uri.parse(url)
    }

    private fun buildViewIntent(url: String) = Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(url)
    }
}
