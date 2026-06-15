/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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
package org.isoron.uhabits.core.preferences

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getFirstWeekdayNumberAccordingToLocale
import org.isoron.platform.utils.StringUtils.Companion.joinLongs
import org.isoron.platform.utils.StringUtils.Companion.splitLongs
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.ui.ThemeSwitcher
import kotlin.math.max
import kotlin.math.min

open class Preferences(private val storage: Storage) {
    private val listeners: MutableList<Listener>
    private var shouldReverseCheckmarks: Boolean? = null
    open fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    open fun getDefaultHabitColor(fallbackColor: Int): Int {
        return storage.getInt(
            "pref_default_habit_palette_color",
            fallbackColor
        )
    }

    open var defaultPrimaryOrder: HabitList.Order
        get() {
            val name = storage.getString("pref_default_order", "BY_POSITION")
            return try {
                HabitList.Order.valueOf(name)
            } catch (e: IllegalArgumentException) {
                defaultPrimaryOrder = HabitList.Order.BY_POSITION
                HabitList.Order.BY_POSITION
            }
        }
        set(order) {
            storage.putString("pref_default_order", order.name)
        }
    open var defaultSecondaryOrder: HabitList.Order
        get() {
            val name = storage.getString("pref_default_secondary_order", "BY_NAME_ASC")
            return try {
                HabitList.Order.valueOf(name)
            } catch (e: IllegalArgumentException) {
                defaultSecondaryOrder = HabitList.Order.BY_NAME_ASC
                HabitList.Order.BY_POSITION
            }
        }
        set(order) {
            storage.putString("pref_default_secondary_order", order.name)
        }
    open var scoreCardSpinnerPosition: Int
        get() = min(4, max(0, storage.getInt("pref_score_view_interval", 1)))
        set(position) {
            storage.putInt("pref_score_view_interval", position)
        }
    open var barCardBoolSpinnerPosition: Int
        get() = min(3, max(0, storage.getInt("pref_bar_card_bool_spinner", 0)))
        set(position) {
            storage.putInt("pref_bar_card_bool_spinner", position)
        }
    open var barCardNumericalSpinnerPosition: Int
        get() = min(4, max(0, storage.getInt("pref_bar_card_numerical_spinner", 0)))
        set(position) {
            storage.putInt("pref_bar_card_numerical_spinner", position)
        }
    open val lastHintNumber: Int
        get() = storage.getInt("last_hint_number", -1)
    open val lastHintDate: LocalDate?
        get() {
            val unixTime = storage.getLong("last_hint_timestamp", -1)
            return if (unixTime < 0) null else LocalDate.fromUnixTime(unixTime)
        }

    open var showArchived: Boolean
        get() = storage.getBoolean("pref_show_archived", false)
        set(showArchived) {
            storage.putBoolean("pref_show_archived", showArchived)
        }
    open var showCompleted: Boolean
        get() = storage.getBoolean("pref_show_completed", true)
        set(showCompleted) {
            storage.putBoolean("pref_show_completed", showCompleted)
        }

    open var theme: Int
        get() = storage.getInt("pref_theme", ThemeSwitcher.THEME_AUTOMATIC)
        set(theme) {
            storage.putInt("pref_theme", theme)
        }

    open fun incrementLaunchCount() {
        storage.putInt("launch_count", launchCount + 1)
    }

    open val launchCount: Int
        get() = storage.getInt("launch_count", 0)
    open var isDeveloper: Boolean
        get() = storage.getBoolean("pref_developer", false)
        set(isDeveloper) {
            storage.putBoolean("pref_developer", isDeveloper)
        }
    open var isFirstRun: Boolean
        get() = storage.getBoolean("pref_first_run", true)
        set(isFirstRun) {
            storage.putBoolean("pref_first_run", isFirstRun)
        }
    open var isPureBlackEnabled: Boolean
        get() = storage.getBoolean("pref_pure_black", false)
        set(enabled) {
            storage.putBoolean("pref_pure_black", enabled)
        }
    open var isShortToggleEnabled: Boolean
        get() = storage.getBoolean("pref_short_toggle", false)
        set(enabled) {
            storage.putBoolean("pref_short_toggle", enabled)
        }

    open var isConfettiAnimationDisabled: Boolean
        get() = storage.getBoolean("pref_disable_animation", false)
        set(enabled) {
            storage.putBoolean("pref_disable_animation", enabled)
        }

    open fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    open fun clear() {
        storage.clear()
    }

    open fun setDefaultHabitColor(color: Int) {
        storage.putInt("pref_default_habit_palette_color", color)
    }

    open fun setNotificationsSticky(sticky: Boolean) {
        storage.putBoolean("pref_sticky_notifications", sticky)
        for (l in listeners) l.onNotificationsChanged()
    }

    open fun shouldMakeNotificationsSticky(): Boolean {
        return storage.getBoolean("pref_sticky_notifications", false)
    }

    open var isCheckmarkSequenceReversed: Boolean
        get() {
            if (shouldReverseCheckmarks == null) {
                shouldReverseCheckmarks =
                    storage.getBoolean("pref_checkmark_reverse_order", false)
            }
            return shouldReverseCheckmarks!!
        }
        set(reverse) {
            shouldReverseCheckmarks = reverse
            storage.putBoolean("pref_checkmark_reverse_order", reverse)
            for (l in listeners) l.onCheckmarkSequenceChanged()
        }

    open var isMidnightDelayEnabled: Boolean
        get() = storage.getBoolean("pref_midnight_delay", false)
        set(enabled) {
            storage.putBoolean("pref_midnight_delay", enabled)
            for (l in listeners) l.onCheckmarkSequenceChanged()
        }

    open val midnightDelayHours: Int
        get() = if (isMidnightDelayEnabled) MIDNIGHT_DELAY_HOURS else 0

    companion object {
        const val MIDNIGHT_DELAY_HOURS = 3
    }

    open fun updateLastHint(number: Int, date: LocalDate) {
        storage.putInt("last_hint_number", number)
        storage.putLong("last_hint_timestamp", date.unixTime)
    }

    open var lastAppVersion: Int
        get() = storage.getInt("last_version", 0)
        set(version) {
            storage.putInt("last_version", version)
        }
    open var widgetOpacity: Int
        get() = storage.getString("pref_widget_opacity", "255").toInt()
        set(value) {
            storage.putString("pref_widget_opacity", value.toString())
        }
    open var isSkipEnabled: Boolean
        get() = storage.getBoolean("pref_skip_enabled", false)
        set(value) {
            storage.putBoolean("pref_skip_enabled", value)
        }

    open var areQuestionMarksEnabled: Boolean
        get() = storage.getBoolean("pref_unknown_enabled", false)
        set(value) {
            storage.putBoolean("pref_unknown_enabled", value)
            for (l in listeners) l.onQuestionMarksChanged()
        }

    /**
     * @return An integer representing the first day of the week. Sunday
     * corresponds to 1, Monday to 2, and so on, until Saturday, which is
     * represented by 7. By default, this is based on the current system locale,
     * unless the user has changed this in the settings.
     */
    @get:Deprecated("")
    open val firstWeekdayInt: Int
        get() {
            val weekday = storage.getString("pref_first_weekday", "")
            return if (weekday.isEmpty()) getFirstWeekdayNumberAccordingToLocale() else weekday.toInt()
        }
    open val firstWeekday: DayOfWeek
        get() {
            var weekday = storage.getString("pref_first_weekday", "-1").toInt()
            if (weekday < 0) weekday = getFirstWeekdayNumberAccordingToLocale()
            return when (weekday) {
                1 -> DayOfWeek.SUNDAY
                2 -> DayOfWeek.MONDAY
                3 -> DayOfWeek.TUESDAY
                4 -> DayOfWeek.WEDNESDAY
                5 -> DayOfWeek.THURSDAY
                6 -> DayOfWeek.FRIDAY
                7 -> DayOfWeek.SATURDAY
                else -> throw IllegalArgumentException()
            }
        }

    interface Listener {
        fun onCheckmarkSequenceChanged() {}
        fun onNotificationsChanged() {}
        fun onQuestionMarksChanged() {}
    }

    interface Storage {
        fun clear()
        fun getBoolean(key: String, defValue: Boolean): Boolean
        fun getInt(key: String, defValue: Int): Int
        fun getLong(key: String, defValue: Long): Long
        fun getString(key: String, defValue: String): String
        fun onAttached(preferences: Preferences) {}
        fun putBoolean(key: String, value: Boolean)
        fun putInt(key: String, value: Int)
        fun putLong(key: String, value: Long)
        fun putString(key: String, value: String)
        fun remove(key: String)
        fun putLongArray(key: String, values: LongArray) {
            putString(key, joinLongs(values))
        }

        fun getLongArray(key: String, defValue: LongArray): LongArray {
            val string = getString(key, "")
            return if (string.isEmpty()) {
                defValue
            } else {
                splitLongs(
                    string
                )
            }
        }
    }

    init {
        listeners = mutableListOf()
        storage.onAttached(this)
    }
}
