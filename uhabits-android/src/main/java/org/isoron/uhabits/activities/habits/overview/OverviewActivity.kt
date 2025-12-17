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

package org.isoron.uhabits.activities.habits.overview

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.models.Score
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.utils.DateUtils
import kotlin.math.max
import kotlin.math.min

class OverviewActivity : AppCompatActivity() {

    private lateinit var chartView: AggregateScoreChart
    private lateinit var statsYesterday: TextView
    private lateinit var statsToday: TextView
    private lateinit var statsChange: TextView
    private lateinit var emptyStateView: TextView
    private lateinit var timeRangeSpinner: Spinner
    private lateinit var taskRunner: TaskRunner
    
    private var currentDays = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.overview)

        val app = applicationContext as HabitsApplication
        taskRunner = app.component.taskRunner

        chartView = findViewById(R.id.overviewChart)
        statsYesterday = findViewById(R.id.statsYesterday)
        statsToday = findViewById(R.id.statsToday)
        statsChange = findViewById(R.id.statsChange)
        emptyStateView = findViewById(R.id.emptyStateView)
        timeRangeSpinner = findViewById(R.id.timeRangeSpinner)

        setupTimeRangeSpinner()
        loadData(currentDays)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupTimeRangeSpinner() {
        val timeRanges = arrayOf(
            getString(R.string.last_7_days),
            getString(R.string.last_30_days),
            getString(R.string.last_60_days),
            getString(R.string.last_90_days),
            getString(R.string.last_180_days),
            getString(R.string.last_365_days),
            getString(R.string.all_time)
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeRangeSpinner.adapter = adapter
        
        timeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDays = when (position) {
                    0 -> 7
                    1 -> 30
                    2 -> 60
                    3 -> 90
                    4 -> 180
                    5 -> 365
                    6 -> -1 // all time
                    else -> 7
                }
                loadData(currentDays)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadData(days: Int) {
        taskRunner.run {
            val app = applicationContext as HabitsApplication
            val habitList = app.component.habitList
            
            // Get all active (non-archived) habits
            val matcher = HabitMatcher(isArchivedAllowed = false)
            val activeHabits = habitList.getFiltered(matcher)
            
            if (activeHabits.isEmpty()) {
                runOnUiThread {
                    showEmptyState()
                }
                return@run
            }

            // Calculate date range
            val today = DateUtils.getToday()
            val fromDate = if (days == -1) {
                // Find earliest habit creation date
                var earliest = today
                for (habit in activeHabits) {
                    val entries = habit.originalEntries.getKnown()
                    if (entries.isNotEmpty()) {
                        val firstEntry = entries.minByOrNull { it.timestamp.unixTime }
                        if (firstEntry != null && firstEntry.timestamp.isOlderThan(earliest)) {
                            earliest = firstEntry.timestamp
                        }
                    }
                }
                earliest
            } else {
                today.minus(days - 1)
            }

            // Compute aggregate scores per day
            val aggregateScores = mutableListOf<Score>()
            var current = fromDate
            
            while (!current.isNewerThan(today)) {
                var sumScore = 0.0
                var count = 0
                
                for (habit in activeHabits) {
                    val score = habit.scores[current]
                    sumScore += score.value
                    count++
                }
                
                val avgScore = if (count > 0) sumScore / count else 0.0
                aggregateScores.add(Score(current, avgScore))
                current = current.plus(1)
            }

            // Update UI
            runOnUiThread {
                if (aggregateScores.size >= 2) {
                    val todayScore = aggregateScores.last()
                    val yesterdayScore = aggregateScores[aggregateScores.size - 2]
                    val change = todayScore.value - yesterdayScore.value
                    
                    statsYesterday.text = String.format("%.2f%%", yesterdayScore.value * 100)
                    statsToday.text = String.format("%.2f%%", todayScore.value * 100)
                    
                    val changeText = if (change >= 0) {
                        String.format("+%.2f%% ↑", change * 100)
                    } else {
                        String.format("%.2f%% ↓", change * 100)
                    }
                    statsChange.text = changeText
                    statsChange.setTextColor(
                        if (change >= 0) 
                            getColor(android.R.color.holo_green_dark) 
                        else 
                            getColor(android.R.color.holo_red_dark)
                    )
                    
                    chartView.visibility = View.VISIBLE
                    emptyStateView.visibility = View.GONE
                    chartView.setScores(aggregateScores.reversed()) // Reversed for chart display
                } else {
                    showEmptyState()
                }
            }
        }
    }

    private fun showEmptyState() {
        chartView.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
        statsYesterday.text = "-"
        statsToday.text = "-"
        statsChange.text = "-"
    }
}
