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
package org.isoron.uhabits.activities.habits.show.views

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.views.ScoreCardPresenter.Companion.getTruncateField
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*

data class BarCardViewModel(
        val checkmarks: List<Checkmark>,
        val bucketSize: Int,
        val color: PaletteColor,
        val isNumerical: Boolean,
        val target: Double,
        val numericalSpinnerPosition: Int,
        val boolSpinnerPosition: Int,
)

class BarCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitBarBinding.inflate(LayoutInflater.from(context), this)
    var onNumericalSpinnerPosition: (position: Int) -> Unit = {}
    var onBoolSpinnerPosition: (position: Int) -> Unit = {}

    fun update(data: BarCardViewModel) {
        binding.barChart.setCheckmarks(data.checkmarks)
        binding.barChart.setBucketSize(data.bucketSize)
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.barChart.setColor(androidColor)
        if (data.isNumerical) {
            binding.boolSpinner.visibility = GONE
            binding.barChart.setTarget(data.target)
        } else {
            binding.numericalSpinner.visibility = GONE
            binding.barChart.setTarget(0.0)
        }

        binding.numericalSpinner.onItemSelectedListener = null
        binding.numericalSpinner.setSelection(data.numericalSpinnerPosition)
        binding.numericalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onNumericalSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.boolSpinner.onItemSelectedListener = null
        binding.boolSpinner.setSelection(data.boolSpinnerPosition)
        binding.boolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onBoolSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}

class BarCardPresenter(
        val habit: Habit,
        val firstWeekday: Int,
) {
    val numericalBucketSizes = intArrayOf(1, 7, 31, 92, 365)
    val boolBucketSizes = intArrayOf(7, 31, 92, 365)

    fun present(
            numericalSpinnerPosition: Int,
            boolSpinnerPosition: Int,
    ): BarCardViewModel {
        val bucketSize = if(habit.isNumerical) {
            numericalBucketSizes[numericalSpinnerPosition]
        } else {
            boolBucketSizes[boolSpinnerPosition]
        }
        val checkmarks = if (bucketSize == 1) {
            habit.checkmarks.all
        } else {
            habit.checkmarks.groupBy(getTruncateField(bucketSize), firstWeekday)
        }
        return BarCardViewModel(
                checkmarks = checkmarks,
                bucketSize = bucketSize,
                color = habit.color,
                isNumerical = habit.isNumerical,
                target = (habit.targetValue / habit.frequency.denominator * bucketSize),
                numericalSpinnerPosition = numericalSpinnerPosition,
                boolSpinnerPosition = boolSpinnerPosition,
        )
    }
}