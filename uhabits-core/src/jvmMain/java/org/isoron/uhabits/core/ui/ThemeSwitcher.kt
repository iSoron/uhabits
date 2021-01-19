/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.ui

import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.views.Theme

abstract class ThemeSwitcher(private val preferences: Preferences) {
    fun apply() {
        if (isNightMode) {
            if (preferences.isPureBlackEnabled) applyPureBlackTheme() else applyDarkTheme()
        } else {
            applyLightTheme()
        }
    }

    abstract fun applyDarkTheme()
    abstract fun applyLightTheme()
    abstract fun applyPureBlackTheme()
    abstract fun getSystemTheme(): Int
    abstract val currentTheme: Theme?
    val isNightMode: Boolean
        get() {
            val systemTheme = getSystemTheme()
            val userTheme = preferences.theme
            return userTheme == THEME_DARK ||
                systemTheme == THEME_DARK && userTheme == THEME_AUTOMATIC
        }

    fun toggleNightMode() {
        val systemTheme = getSystemTheme()
        val userTheme = preferences.theme
        if (userTheme == THEME_AUTOMATIC) {
            if (systemTheme == THEME_LIGHT) preferences.theme = THEME_DARK
            if (systemTheme == THEME_DARK) preferences.theme = THEME_LIGHT
        } else if (userTheme == THEME_LIGHT) {
            if (systemTheme == THEME_LIGHT) preferences.theme = THEME_DARK
            if (systemTheme == THEME_DARK) preferences.theme = THEME_AUTOMATIC
        } else if (userTheme == THEME_DARK) {
            if (systemTheme == THEME_LIGHT) preferences.theme = THEME_AUTOMATIC
            if (systemTheme == THEME_DARK) preferences.theme = THEME_LIGHT
        }
    }

    companion object {
        const val THEME_DARK = 1
        const val THEME_LIGHT = 2
        const val THEME_AUTOMATIC = 0
    }
}
