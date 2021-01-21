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
package org.isoron.uhabits.widgets

import org.isoron.uhabits.R

/**
 * Created by victoryu on 11/3/17.
 */
enum class StackWidgetType(val value: Int) {
    CHECKMARK(0), FREQUENCY(1), SCORE(2), // habit strength widget
    HISTORY(3), STREAKS(4), TARGET(5);

    companion object {
        fun getWidgetTypeFromValue(value: Int): StackWidgetType? {
            return when {
                CHECKMARK.value == value -> CHECKMARK
                FREQUENCY.value == value -> FREQUENCY
                SCORE.value == value -> SCORE
                HISTORY.value == value -> HISTORY
                STREAKS.value == value -> STREAKS
                TARGET.value == value -> TARGET
                else -> null
            }
        }

        fun getStackWidgetLayoutId(type: StackWidgetType?): Int {
            when (type) {
                CHECKMARK -> return R.layout.checkmark_stackview_widget
                FREQUENCY -> return R.layout.frequency_stackview_widget
                SCORE -> return R.layout.score_stackview_widget
                HISTORY -> return R.layout.history_stackview_widget
                STREAKS -> return R.layout.streak_stackview_widget
                TARGET -> return R.layout.target_stackview_widget
            }
            return 0
        }

        fun getStackWidgetAdapterViewId(type: StackWidgetType?): Int {
            when (type) {
                CHECKMARK -> return R.id.checkmarkStackWidgetView
                FREQUENCY -> return R.id.frequencyStackWidgetView
                SCORE -> return R.id.scoreStackWidgetView
                HISTORY -> return R.id.historyStackWidgetView
                STREAKS -> return R.id.streakStackWidgetView
                TARGET -> return R.id.targetStackWidgetView
            }
            return 0
        }

        fun getStackWidgetEmptyViewId(type: StackWidgetType?): Int {
            when (type) {
                CHECKMARK -> return R.id.checkmarkStackWidgetEmptyView
                FREQUENCY -> return R.id.frequencyStackWidgetEmptyView
                SCORE -> return R.id.scoreStackWidgetEmptyView
                HISTORY -> return R.id.historyStackWidgetEmptyView
                STREAKS -> return R.id.streakStackWidgetEmptyView
                TARGET -> return R.id.targetStackWidgetEmptyView
            }
            return 0
        }
    }
}
