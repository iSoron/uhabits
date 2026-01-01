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

import org.isoron.uhabits.activities.habits.progress.views.ProgressBarCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionBarCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionFrequencyCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionHistoryCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressCompletionStatsCardView
import org.isoron.uhabits.activities.habits.progress.views.ProgressDeltaCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressFrequencyCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressHistoryCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressRankCardState
import org.isoron.uhabits.activities.habits.progress.views.ProgressScoreCardView
import org.isoron.uhabits.activities.habits.progress.views.ProgressStatsCardView
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardState

data class ProgressState(
    val statsCard: ProgressStatsCardView.State,
    val scoreCard: ProgressScoreCardView.State?,
    val barCard: ProgressBarCardState?,
    val deltaCard: ProgressDeltaCardState?,
    val historyCard: ProgressHistoryCardState?,
    val streakCard: StreakCardState?,
    val frequencyCard: ProgressFrequencyCardState?,
    val completionStatsCard: ProgressCompletionStatsCardView.State?,
    val completionBarCard: ProgressCompletionBarCardState?,
    val completionHistoryCard: ProgressCompletionHistoryCardState?,
    val completionStreakCard: StreakCardState?,
    val completionFrequencyCard: ProgressCompletionFrequencyCardState?,
    val scoreRankCard: ProgressRankCardState?,
    val progressRankCard: ProgressRankCardState?,
    val completionRankCard: ProgressRankCardState?,
    val overallRankCard: ProgressRankCardState?,
    val isEmpty: Boolean = false
)
