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

import org.isoron.uhabits.activities.habits.overview.views.OverviewBarCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewFrequencyCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewHistoryCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewScoreCardState
import org.isoron.uhabits.activities.habits.overview.views.OverviewStatsCardView
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardState

data class OverviewState(
    val statsCard: OverviewStatsCardView.State,
    val scoreCard: OverviewScoreCardState?,
    val barCard: OverviewBarCardState?,
    val historyCard: OverviewHistoryCardState?,
    val streakCard: StreakCardState?,
    val frequencyCard: OverviewFrequencyCardState?,
    val isEmpty: Boolean = false
)