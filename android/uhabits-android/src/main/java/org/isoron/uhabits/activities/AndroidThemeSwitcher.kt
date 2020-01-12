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

package org.isoron.uhabits.activities

import android.app.*
import android.content.res.Configuration.*
import android.os.Build.VERSION.*
import androidx.core.content.*
import org.isoron.androidbase.activities.*
import org.isoron.uhabits.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.ui.*
import javax.inject.*

@ActivityScope
class AndroidThemeSwitcher
constructor(
        private val activity: Activity,
        preferences: Preferences
) : ThemeSwitcher(preferences) {

    override fun getSystemTheme(): Int {
        if(SDK_INT < 29) return THEME_LIGHT;
        val uiMode = activity.resources.configuration.uiMode
        return if ((uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES) {
            THEME_DARK;
        } else {
            THEME_LIGHT;
        }
    }

    override fun applyDarkTheme() {
        activity.setTheme(R.style.AppBaseThemeDark)
        activity.window.navigationBarColor =
                ContextCompat.getColor(activity, R.color.grey_900)
    }

    override fun applyLightTheme() {
        activity.setTheme(R.style.AppBaseTheme)
    }

    override fun applyPureBlackTheme() {
        activity.setTheme(R.style.AppBaseThemeDark_PureBlack)
        activity.window.navigationBarColor =
                ContextCompat.getColor(activity, R.color.black)
    }

    fun getDialogTheme(): Int {
        return when {
            isNightMode -> R.style.DarkDialogWithTitle
            else -> R.style.DialogWithTitle
        }
    }
}
