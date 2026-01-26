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

package org.isoron.uhabits.activities.habits.progress

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private lateinit var presenter: ProgressPresenter
    private lateinit var taskRunner: TaskRunner
    private lateinit var themeSwitcher: AndroidThemeSwitcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get dependencies
        val app = applicationContext as HabitsApplication
        taskRunner = app.component.taskRunner
        val habitList = app.component.habitList
        val preferences = app.component.preferences

        // Initialize theme BEFORE setting content view
        themeSwitcher = AndroidThemeSwitcher(this, preferences)
        themeSwitcher.apply()

        // Setup ViewBinding
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize presenter
        presenter = ProgressPresenter(this, habitList, themeSwitcher.currentTheme, preferences.firstWeekdayInt)

        // Setup UI
        setupToolbar()
        setupCardCallbacks()

        // Load initial data
        loadData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.progress)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupCardCallbacks() {
        binding.scoreCard.setOnSpinnerPositionChanged { position ->
            val app = applicationContext as HabitsApplication
            app.component.preferences.progressScoreSpinnerPosition = position
            loadData()
        }

        binding.barCard.setOnSpinnerPositionChanged { position ->
            val app = applicationContext as HabitsApplication
            app.component.preferences.progressBarSpinnerPosition = position
            loadData()
        }

        binding.deltaCard.setOnSpinnerPositionChanged { position ->
            val app = applicationContext as HabitsApplication
            app.component.preferences.progressDeltaSpinnerPosition = position
            loadData()
        }
    }

    private fun loadData() {
        taskRunner.run {
            val app = applicationContext as HabitsApplication
            val scoreSpinnerPosition = app.component.preferences.progressScoreSpinnerPosition
            val barSpinnerPosition = app.component.preferences.progressBarSpinnerPosition
            val deltaSpinnerPosition = app.component.preferences.progressDeltaSpinnerPosition
            val state = presenter.buildState(scoreSpinnerPosition, barSpinnerPosition, deltaSpinnerPosition)

            runOnUiThread {
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ProgressState) {
        applySectionHeaderColors()
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

            // Delta card
            if (state.deltaCard != null) {
                binding.deltaCard.visibility = View.VISIBLE
                binding.deltaCard.setState(state.deltaCard)
            } else {
                binding.deltaCard.visibility = View.GONE
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

            // Completion stats card
            if (state.completionStatsCard != null) {
                binding.completionStatsCard.visibility = View.VISIBLE
                binding.completionStatsCard.setState(state.completionStatsCard)
            } else {
                binding.completionStatsCard.visibility = View.GONE
            }

            // Completion bar card
            if (state.completionBarCard != null) {
                binding.completionBarCard.visibility = View.VISIBLE
                binding.completionBarCard.setState(state.completionBarCard)
            } else {
                binding.completionBarCard.visibility = View.GONE
            }

            // Completion history card
            if (state.completionHistoryCard != null) {
                binding.completionHistoryCard.visibility = View.VISIBLE
                binding.completionHistoryCard.setState(state.completionHistoryCard)
            } else {
                binding.completionHistoryCard.visibility = View.GONE
            }

            // Completion streak card
            if (state.completionStreakCard != null) {
                binding.completionStreakCard.visibility = View.VISIBLE
                binding.completionStreakCard.setState(state.completionStreakCard)
            } else {
                binding.completionStreakCard.visibility = View.GONE
            }

            // Completion frequency card
            if (state.completionFrequencyCard != null) {
                binding.completionFrequencyCard.visibility = View.VISIBLE
                binding.completionFrequencyCard.setState(state.completionFrequencyCard)
            } else {
                binding.completionFrequencyCard.visibility = View.GONE
            }

            if (state.scoreRankCard != null) {
                binding.scoreRankCard.visibility = View.VISIBLE
                binding.scoreRankCard.setState(state.scoreRankCard)
            } else {
                binding.scoreRankCard.visibility = View.GONE
            }

            if (state.progressRankCard != null) {
                binding.progressRankCard.visibility = View.VISIBLE
                binding.progressRankCard.setState(state.progressRankCard)
            } else {
                binding.progressRankCard.visibility = View.GONE
            }

            if (state.streakRankCard != null) {
                binding.streakRankCard.visibility = View.VISIBLE
                binding.streakRankCard.setState(state.streakRankCard)
            } else {
                binding.streakRankCard.visibility = View.GONE
            }

            if (state.completionRankCard != null) {
                binding.completionRankCard.visibility = View.VISIBLE
                binding.completionRankCard.setState(state.completionRankCard)
            } else {
                binding.completionRankCard.visibility = View.GONE
            }

            if (state.overallRankCard != null) {
                binding.overallRankCard.visibility = View.VISIBLE
                binding.overallRankCard.setState(state.overallRankCard)
            } else {
                binding.overallRankCard.visibility = View.GONE
            }
        }
    }

    private fun applySectionHeaderColors() {
        val theme = themeSwitcher.currentTheme
        binding.progressSectionTitle.setTextColor(theme.color(ProgressSectionColors.progress).toInt())
        binding.streakSectionTitle.setTextColor(theme.color(ProgressSectionColors.streak).toInt())
        binding.scoreSectionTitle.setTextColor(theme.color(ProgressSectionColors.score).toInt())
        binding.completionSectionTitle.setTextColor(theme.color(ProgressSectionColors.completion).toInt())
        binding.overallSectionTitle.setTextColor(theme.color(ProgressSectionColors.overall).toInt())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
