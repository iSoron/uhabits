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
import java.lang.IllegalStateException

enum class StackWidgetType(val value: Int) {
    CHECKMARK(0), FREQUENCY(1), SCORE(2), // habit strength widget
    HISTORY(3), STREAKS(4), TARGET(5);

    companion object {
        fun getWidgetTypeFromValue(value: Int): StackWidgetType? {
            return when (value) {
                CHECKMARK.value -> CHECKMARK
                FREQUENCY.value -> FREQUENCY
                SCORE.value -> SCORE
                HISTORY.value -> HISTORY
                STREAKS.value -> STREAKS
                TARGET.value -> TARGET
                else -> null
            }
        }

        fun getStackWidgetLayoutId(type: StackWidgetType?): Int {
            return when (type) {
                CHECKMARK -> R.layout.checkmark_stackview_widget
                FREQUENCY -> R.layout.frequency_stackview_widget
                SCORE -> R.layout.score_stackview_widget
                HISTORY -> R.layout.history_stackview_widget
                STREAKS -> R.layout.streak_stackview_widget
                TARGET -> R.layout.target_stackview_widget
                else -> throw IllegalStateException()
            }
        }

        fun getStackWidgetAdapterViewId(type: StackWidgetType?): Int {
            return when (type) {
                CHECKMARK -> R.id.checkmarkStackWidgetView
                FREQUENCY -> R.id.frequencyStackWidgetView
                SCORE -> R.id.scoreStackWidgetView
                HISTORY -> R.id.historyStackWidgetView
                STREAKS -> R.id.streakStackWidgetView
                TARGET -> R.id.targetStackWidgetView
                else -> throw IllegalStateException()
            }
        }

        fun getStackWidgetEmptyViewId(type: StackWidgetType?): Int {
            return when (type) {
                CHECKMARK -> R.id.checkmarkStackWidgetEmptyView
                FREQUENCY -> R.id.frequencyStackWidgetEmptyView
                SCORE -> R.id.scoreStackWidgetEmptyView
                HISTORY -> R.id.historyStackWidgetEmptyView
                STREAKS -> R.id.streakStackWidgetEmptyView
                TARGET -> R.id.targetStackWidgetEmptyView
                else -> throw IllegalStateException()
            }
        }
    }
}
