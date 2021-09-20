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

package org.isoron.uhabits.core.ui.screens.habits.show.views

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.NumericalHabitType
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.core.ui.views.OnDateClickedListener
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.core.utils.DateUtils
import kotlin.math.max
import kotlin.math.roundToInt

data class HistoryCardState(
    val color: PaletteColor,
    val firstWeekday: DayOfWeek,
    val series: List<HistoryChart.Square>,
    val defaultSquare: HistoryChart.Square,
    val hasNotes: List<Boolean>,
    val theme: Theme,
    val today: LocalDate,
)

class HistoryCardPresenter(
    val commandRunner: CommandRunner,
    val habit: Habit,
    val habitList: HabitList,
    val preferences: Preferences,
    val screen: Screen,
) : OnDateClickedListener {

    override fun onDateClicked(date: LocalDate, isLongClick: Boolean) {
        val timestamp = Timestamp.fromLocalDate(date)
        screen.showFeedback()
        val entries = habit.computedEntries
        val oldValue = entries.get(timestamp).value
        val notes = entries.get(timestamp).notes
        if (habit.isNumerical) {
            screen.showNumberPicker(oldValue / 1000.0, habit.unit, notes) { newValue: Double, newNotes: String ->
                val thousands = (newValue * 1000).roundToInt()
                commandRunner.run(
                    CreateRepetitionCommand(
                        habitList,
                        habit,
                        timestamp,
                        thousands,
                        newNotes,
                    ),
                )
            }
        } else {
            if (!isLongClick) {
                val nextValue = Entry.nextToggleValue(
                    value = oldValue,
                    isSkipEnabled = preferences.isSkipEnabled,
                    areQuestionMarksEnabled = preferences.areQuestionMarksEnabled
                )
                commandRunner.run(
                    CreateRepetitionCommand(
                        habitList,
                        habit,
                        timestamp,
                        nextValue,
                        notes,
                    ),
                )
                return
            }
            screen.showCheckmarkDialog(notes) { newNotes ->
                commandRunner.run(
                    CreateRepetitionCommand(
                        habitList,
                        habit,
                        timestamp,
                        oldValue,
                        newNotes,
                    ),
                )
            }
        }
    }

    fun onClickEditButton() {
        screen.showHistoryEditorDialog(this)
    }

    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: DayOfWeek,
            theme: Theme,
        ): HistoryCardState {
            val today = DateUtils.getTodayWithOffset()
            val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today
            val entries = habit.computedEntries.getByInterval(oldest, today)
            val series = if (habit.isNumerical) {
                if (habit.targetType == NumericalHabitType.AT_LEAST) {
                    entries.map {
                        when (max(0, it.value)) {
                            0 -> HistoryChart.Square.OFF
                            else -> HistoryChart.Square.ON
                        }
                    }
                } else {
                    entries.map {
                        when {
                            max(0.0, it.value / 1000.0) <= habit.targetValue -> HistoryChart.Square.ON
                            else -> HistoryChart.Square.OFF
                        }
                    }
                }
            } else {
                entries.map {
                    when (it.value) {
                        YES_MANUAL -> HistoryChart.Square.ON
                        YES_AUTO -> HistoryChart.Square.DIMMED
                        SKIP -> HistoryChart.Square.HATCHED
                        else -> HistoryChart.Square.OFF
                    }
                }
            }
            val defaultSquare = if (habit.isNumerical && habit.targetType == NumericalHabitType.AT_MOST)
                HistoryChart.Square.ON
            else
                HistoryChart.Square.OFF

            val hasNotes = entries.map {
                when (it.notes) {
                    "" -> false
                    else -> true
                }
            }

            return HistoryCardState(
                color = habit.color,
                firstWeekday = firstWeekday,
                today = today.toLocalDate(),
                theme = theme,
                series = series,
                defaultSquare = defaultSquare
                hasNotes = hasNotes,
            )
        }
    }

    interface Screen {
        fun showHistoryEditorDialog(listener: OnDateClickedListener)
        fun showFeedback()
        fun showNumberPicker(
            value: Double,
            unit: String,
            notes: String,
            callback: ListHabitsBehavior.NumberPickerCallback,
        )
        fun showCheckmarkDialog(
            notes: String,
            callback: ListHabitsBehavior.CheckMarkDialogCallback,
        )
    }
}
