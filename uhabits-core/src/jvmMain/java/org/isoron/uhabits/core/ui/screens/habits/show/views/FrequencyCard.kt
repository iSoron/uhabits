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

package org.isoron.uhabits.core.ui.screens.habits.show.views

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.Theme

data class FrequencyCardState(
    val color: PaletteColor,
    val firstWeekday: DayOfWeek,
    val frequency: HashMap<LocalDate, Array<Int>>,
    val theme: Theme,
    val isNumerical: Boolean
)

class FrequencyCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: DayOfWeek,
            theme: Theme
        ) = FrequencyCardState(
            color = habit.color,
            isNumerical = habit.isNumerical,
            frequency = habit.originalEntries.computeWeekdayFrequency(
                isNumerical = habit.isNumerical
            ),
            firstWeekday = firstWeekday,
            theme = theme
        )

        fun buildState(
            habitGroup: HabitGroup,
            firstWeekday: DayOfWeek,
            theme: Theme
        ): FrequencyCardState {
            val frequencies = if (habitGroup.habitList.isEmpty) {
                hashMapOf<LocalDate, Array<Int>>()
            } else {
                getFrequenciesFromHabitGroup(habitGroup)
            }

            return FrequencyCardState(
                color = habitGroup.color,
                isNumerical = true,
                frequency = frequencies,
                firstWeekday = firstWeekday,
                theme = theme
            )
        }

        fun getFrequenciesFromHabitGroup(habitGroup: HabitGroup): HashMap<LocalDate, Array<Int>> {
            val normalizedEntries = habitGroup.habitList.map {
                it.originalEntries.normalizeEntries(it.isNumerical, it.frequency, it.targetValue)
            }

            val frequenciesSeparate = normalizedEntries.map {
                it.computeWeekdayFrequency(isNumerical = true)
            }

            val frequencies = reduceMaps(frequenciesSeparate) { arr1, arr2 -> addArrays(arr1, arr2) }

            return frequencies
        }

        fun <K, V> reduceMaps(
            maps: Iterable<Map<K, V>>,
            combine: (V, V) -> V
        ): HashMap<K, V> {
            val result = HashMap<K, V>()

            for (map in maps) {
                for ((key, value) in map) {
                    val existing = result[key]
                    if (existing != null) {
                        // If the key exists, pass both values through your combiner function
                        result[key] = combine(existing, value)
                    } else {
                        // Otherwise, simply add the value to the map
                        result[key] = value
                    }
                }
            }

            return result
        }

        fun addArrays(a: Array<Int>, b: Array<Int>): Array<Int> {
            val maxSize = maxOf(a.size, b.size)

            return Array<Int>(maxSize) { index ->
                val valA = if (index < a.size) a[index] else 0
                val valB = if (index < b.size) b[index] else 0
                valA + valB
            }
        }
    }
}
