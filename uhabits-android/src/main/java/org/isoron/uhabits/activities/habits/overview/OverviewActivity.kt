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
        presenter = OverviewPresenter(this, habitList, themeSwitcher.currentTheme)

        // Setup UI
        setupToolbar()
        setupCardCallbacks()
        
        // Load initial data
        loadData(currentTimeRangeDays)
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.overview)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupCardCallbacks() {
        binding.scoreCard.onTimeRangeChanged = { days ->
            currentTimeRangeDays = days
            loadData(days)
        }
    }

    private fun loadData(days: Int) {
        taskRunner.run {
            val state = presenter.buildState(days)
            
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

            // Update card states
            binding.statsCard.setState(state.statsCard)
            binding.scoreCard.setState(state.scoreCard)

            // Show/hide streak card based on availability
            if (state.streakCard != null) {
                binding.streakCard.visibility = View.VISIBLE
                binding.streakCard.setState(state.streakCard)
            } else {
                binding.streakCard.visibility = View.GONE
            }

            // Show/hide history card based on availability
            if (state.historyCard != null) {
                binding.historyCard.visibility = View.VISIBLE
                binding.historyCard.setState(state.historyCard)
            } else {
                binding.historyCard.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
