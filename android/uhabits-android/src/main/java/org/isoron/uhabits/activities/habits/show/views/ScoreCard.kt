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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.core.utils.DateUtils.TruncateField.DAY
import org.isoron.uhabits.core.utils.DateUtils.TruncateField.MONTH
import org.isoron.uhabits.core.utils.DateUtils.TruncateField.QUARTER
import org.isoron.uhabits.core.utils.DateUtils.TruncateField.WEEK_NUMBER
import org.isoron.uhabits.core.utils.DateUtils.TruncateField.YEAR
import org.isoron.uhabits.databinding.ShowHabitScoreBinding
import org.isoron.uhabits.utils.toThemedAndroidColor

data class ScoreCardViewModel(
    val scores: List<Score>,
    val bucketSize: Int,
    val spinnerPosition: Int,
    val color: PaletteColor,
)

class ScoreCard(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private var binding = ShowHabitScoreBinding.inflate(LayoutInflater.from(context), this)

    var onSpinnerPosition: (position: Int) -> Unit = {}

    fun update(data: ScoreCardViewModel) {
        val androidColor = data.color.toThemedAndroidColor(context)
        binding.title.setTextColor(androidColor)
        binding.spinner.setSelection(data.spinnerPosition)
        binding.scoreView.setScores(data.scores)
        binding.scoreView.reset()
        binding.scoreView.setBucketSize(data.bucketSize)
        binding.scoreView.setColor(androidColor)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onSpinnerPosition(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}

class ScoreCardPresenter(
    val habit: Habit,
    val firstWeekday: Int,
) {
    companion object {
        val BUCKET_SIZES = intArrayOf(1, 7, 31, 92, 365)
        fun getTruncateField(bucketSize: Int): DateUtils.TruncateField {
            when (bucketSize) {
                1 -> return DAY
                7 -> return WEEK_NUMBER
                31 -> return MONTH
                92 -> return QUARTER
                365 -> return YEAR
                else -> return MONTH
            }
        }
    }

    fun present(spinnerPosition: Int): ScoreCardViewModel {
        val bucketSize = BUCKET_SIZES[spinnerPosition]
        val today = DateUtils.getTodayWithOffset()
        val oldest = habit.computedEntries.getKnown().lastOrNull()?.timestamp ?: today

        val field = getTruncateField(bucketSize)
        val scores = habit.scores.getByInterval(oldest, today).groupBy {
            DateUtils.truncate(field, it.timestamp, firstWeekday)
        }.map { (timestamp, scores) ->
            Score(
                timestamp,
                scores.map {
                    it.value
                }.average()
            )
        }.sortedBy {
            it.timestamp
        }.reversed()

        return ScoreCardViewModel(
            color = habit.color,
            scores = scores,
            bucketSize = bucketSize,
            spinnerPosition = spinnerPosition,
        )
    }
}
