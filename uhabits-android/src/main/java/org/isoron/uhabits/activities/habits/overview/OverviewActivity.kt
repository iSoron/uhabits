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
import androidx.appcompat.app.AppCompatActivity
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.databinding.ActivityOverviewBinding

class OverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOverviewBinding
    private lateinit var presenter: OverviewPresenter
    private lateinit var taskRunner: TaskRunner
    private lateinit var themeSwitcher: AndroidThemeSwitcher

    private var currentTimeRangeDays = 7 // Default to 7 days

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get dependencies
        val app = applicationContext as HabitsApplication
        taskRunner = app.component.taskRunner
        val habitList = app.component.habitList
        val preferences = app.component.preferences
        
        // Initialize theme
        themeSwitcher = AndroidThemeSwitcher(this, preferences)
        themeSwitcher.apply()

        // Initialize presenter
        presenter = OverviewPresenter(this, habitList, themeSwitcher.currentTheme, preferences.firstWeekdayInt)

        // Setup UI
        setupToolbar()
        setupCardCallbacks()
        
        // Load initial data
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.overview)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupCardCallbacks() {
        binding.scoreCard.setOnSpinnerPositionChanged { position ->
            val app = applicationContext as HabitsApplication
            app.component.preferences.overviewScoreSpinnerPosition = position
            loadData()
        }
        
        binding.barCard.setOnSpinnerPositionChanged { position ->
            val app = applicationContext as HabitsApplication
            app.component.preferences.overviewBarSpinnerPosition = position
            loadData()
        }
    }

    private fun loadData() {
        taskRunner.run {
            val app = applicationContext as HabitsApplication
            val scoreSpinnerPosition = app.component.preferences.overviewScoreSpinnerPosition
            val barSpinnerPosition = app.component.preferences.overviewBarSpinnerPosition
            val state = presenter.buildState(scoreSpinnerPosition, barSpinnerPosition)
            
            runOnUiThread {
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: OverviewState) {
        if (state.isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE

            // Update card states (in order matching individual habit detail view)
            binding.statsCard.setState(state.statsCard)
            
            // Score card
            if (state.scoreCard != null) {
                binding.scoreCard.visibility = View.VISIBLE
                binding.scoreCard.setState(state.scoreCard)
            } else {
                binding.scoreCard.visibility = View.GONE
            }

            // Bar card
            if (state.barCard != null) {
                binding.barCard.visibility = View.VISIBLE
                binding.barCard.setState(state.barCard)
            } else {
                binding.barCard.visibility = View.GONE
            }

            // History (Calendar) card
            if (state.historyCard != null) {
                binding.historyCard.visibility = View.VISIBLE
                binding.historyCard.setState(state.historyCard)
            } else {
                binding.historyCard.visibility = View.GONE
            }

            // Streaks card
            if (state.streakCard != null) {
                binding.streakCard.visibility = View.VISIBLE
                binding.streakCard.setState(state.streakCard)
            } else {
                binding.streakCard.visibility = View.GONE
            }

            // Frequency card
            if (state.frequencyCard != null) {
                binding.frequencyCard.visibility = View.VISIBLE
                binding.frequencyCard.setState(state.frequencyCard)
            } else {
                binding.frequencyCard.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
