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

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.ui.views.Theme

data class FrequencyCardState(
    val color: PaletteColor,
    val firstWeekday: Int,
    val frequency: HashMap<Timestamp, Array<Int>>,
    val theme: Theme,
    val isNumerical: Boolean
)

class FrequencyCardPresenter {
    companion object {
        fun buildState(
            habit: Habit,
            firstWeekday: Int,
            theme: Theme
        ) = FrequencyCardState(
            color = habit.color,
            isNumerical = habit.isNumerical,
            frequency = habit.computedEntries.computeWeekdayFrequency(
                isNumerical = habit.isNumerical
            ),
            firstWeekday = firstWeekday,
            theme = theme
        )

        fun buildState(
            habitGroup: HabitGroup,
            firstWeekday: Int,
            theme: Theme
        ): FrequencyCardState {
            val frequencies = if (habitGroup.habitList.isEmpty) {
                hashMapOf<Timestamp, Array<Int>>()
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

        fun getFrequenciesFromHabitGroup(habitGroup: HabitGroup): HashMap<Timestamp, Array<Int>> {
            val normalizedEntries = habitGroup.habitList.map {
                it.computedEntries.normalizeEntries(it.isNumerical, it.frequency, it.targetValue)
            }
            val frequencies = normalizedEntries.map {
                it.computeWeekdayFrequency(isNumerical = true)
            }.reduce { acc, hashMap ->
                mergeMaps(acc, hashMap) { value1, value2 -> addArray(value1, value2) }
            }
            return frequencies
        }

        private fun <K, V> mergeMaps(map1: HashMap<K, V>, map2: HashMap<K, V>, mergeFunction: (V, V) -> V): HashMap<K, V> {
            val result = map1 // Step 1
            for ((key, value) in map2) { // Step 2
                result[key] = result[key]?.let { existingValue ->
                    mergeFunction(existingValue, value) // Step 3 (merge logic)
                } ?: value
            }
            return result // Step 4
        }

        private fun addArray(array1: Array<Int>, array2: Array<Int>): Array<Int> {
            return array1.zip(array2) { a, b -> a + b }.toTypedArray()
        }
    }
}
