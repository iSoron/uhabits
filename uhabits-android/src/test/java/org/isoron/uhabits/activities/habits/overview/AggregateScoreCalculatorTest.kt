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

import org.isoron.uhabits.BaseAndroidJVMTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AggregateScoreCalculatorTest : BaseAndroidJVMTest() {

    private lateinit var calculator: AggregateScoreCalculator

    @Before
    override fun setUp() {
        super.setUp()
        calculator = AggregateScoreCalculator()
    }

    @Test
    fun testComputeAggregateScores_withEmptyHabitList_returnsEmptyList() {
        val today = DateUtils.getToday()
        val lastWeek = today.minus(7)

        val result = calculator.computeAggregateScores(
            emptyList(),
            lastWeek,
            today
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun testComputeAggregateScores_withInvalidDateRange_returnsEmptyList() {
        val today = DateUtils.getToday()
        val tomorrow = today.plus(1)
        val habit = fixtures.createShortHabit()

        val result = calculator.computeAggregateScores(
            listOf(habit),
            tomorrow, // from is after to
            today
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun testComputeAggregateScores_withSingleHabit_returnsCorrectScores() {
        val habit = fixtures.createShortHabit()
        val today = DateUtils.getToday()
        val lastWeek = today.minus(7)

        // Add some entries to generate scores
        for (i in 0..6) {
            habit.originalEntries.add(
                Entry(today.minus(i), Entry.YES_MANUAL)
            )
        }
        habit.recompute()

        val result = calculator.computeAggregateScores(
            listOf(habit),
            lastWeek,
            today
        )

        // Should have 8 days of scores (lastWeek to today inclusive)
        assertEquals(8, result.size)

        // Scores should be in chronological order
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].timestamp.isOlderThan(result[i + 1].timestamp))
        }
    }

    @Test
    fun testComputeAggregateScores_withMultipleHabits_computesAverage() {
        val habit1 = fixtures.createShortHabit()
        val habit2 = fixtures.createShortHabit()
        val today = DateUtils.getToday()

        // Add entries to both habits to generate different scores
        habit1.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit1.originalEntries.add(Entry(today.minus(1), Entry.YES_MANUAL))
        habit1.recompute()

        // habit2 gets fewer entries, so lower score
        habit2.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit2.recompute()

        val result = calculator.computeAggregateScores(
            listOf(habit1, habit2),
            today.minus(1),
            today
        )

        assertEquals(2, result.size)

        // Verify the average is computed correctly
        val todayScore = result.last()
        val habit1Score = habit1.scores[today].value
        val habit2Score = habit2.scores[today].value
        val expectedAverage = (habit1Score + habit2Score) / 2.0

        assertEquals(expectedAverage, todayScore.value, 0.0001)
    }

    @Test
    fun testComputeAggregateScores_withSingleDayRange_returnsSingleScore() {
        val habit = fixtures.createShortHabit()
        val today = DateUtils.getToday()

        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.recompute()

        val result = calculator.computeAggregateScores(
            listOf(habit),
            today,
            today
        )

        assertEquals(1, result.size)
        assertEquals(today, result[0].timestamp)
    }

    @Test
    fun testFindEarliestHabitDate_withEmptyList_returnsDefault() {
        val today = DateUtils.getToday()

        val result = calculator.findEarliestHabitDate(emptyList(), today)

        assertEquals(today, result)
    }

    @Test
    fun testFindEarliestHabitDate_withHabitsWithoutEntries_returnsDefault() {
        val habit = fixtures.createEmptyHabit()
        val today = DateUtils.getToday()

        val result = calculator.findEarliestHabitDate(listOf(habit), today)

        assertEquals(today, result)
    }

    @Test
    fun testFindEarliestHabitDate_withSingleHabit_returnsFirstEntryDate() {
        val habit = fixtures.createShortHabit()
        val today = DateUtils.getToday()
        val tenDaysAgo = today.minus(10)

        habit.originalEntries.add(Entry(tenDaysAgo, Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))

        val result = calculator.findEarliestHabitDate(listOf(habit), today)

        assertEquals(tenDaysAgo, result)
    }

    @Test
    fun testFindEarliestHabitDate_withMultipleHabits_returnsEarliestDate() {
        val habit1 = fixtures.createShortHabit()
        val habit2 = fixtures.createShortHabit()
        val habit3 = fixtures.createShortHabit()
        val today = DateUtils.getToday()

        habit1.originalEntries.add(Entry(today.minus(5), Entry.YES_MANUAL))
        habit2.originalEntries.add(Entry(today.minus(30), Entry.YES_MANUAL)) // Earliest
        habit3.originalEntries.add(Entry(today.minus(10), Entry.YES_MANUAL))

        val result = calculator.findEarliestHabitDate(
            listOf(habit1, habit2, habit3),
            today
        )

        assertEquals(today.minus(30), result)
    }

    @Test
    fun testFindEarliestHabitDate_withMixedHabits_ignoresHabitsWithoutEntries() {
        val habitWithEntries = fixtures.createShortHabit()
        val habitWithoutEntries = fixtures.createShortHabit()
        val today = DateUtils.getToday()
        val twentyDaysAgo = today.minus(20)

        habitWithEntries.originalEntries.add(Entry(twentyDaysAgo, Entry.YES_MANUAL))
        // habitWithoutEntries has no entries

        val result = calculator.findEarliestHabitDate(
            listOf(habitWithEntries, habitWithoutEntries),
            today
        )

        assertEquals(twentyDaysAgo, result)
    }

    @Test
    fun testComputeAggregateScores_withLargeTimeRange_handlesCorrectly() {
        val habit = fixtures.createShortHabit()
        val today = DateUtils.getToday()
        val oneYearAgo = today.minus(365)

        // Add entries throughout the year
        for (i in 0..364 step 7) {
            habit.originalEntries.add(
                Entry(today.minus(i), Entry.YES_MANUAL)
            )
        }
        habit.recompute()

        val result = calculator.computeAggregateScores(
            listOf(habit),
            oneYearAgo,
            today
        )

        // Should have 366 days of scores
        assertEquals(366, result.size)

        // Verify first and last timestamps
        assertEquals(oneYearAgo, result.first().timestamp)
        assertEquals(today, result.last().timestamp)
    }

    @Test
    fun testComputeAggregateScores_allScoresBetweenZeroAndOne() {
        val habit1 = fixtures.createShortHabit()
        val habit2 = fixtures.createShortHabit()
        val today = DateUtils.getToday()
        val lastMonth = today.minus(30)

        // Add some entries
        for (i in 0..29 step 3) {
            habit1.originalEntries.add(Entry(today.minus(i), Entry.YES_MANUAL))
            if (i % 2 == 0) {
                habit2.originalEntries.add(Entry(today.minus(i), Entry.YES_MANUAL))
            }
        }
        habit1.recompute()
        habit2.recompute()

        val result = calculator.computeAggregateScores(
            listOf(habit1, habit2),
            lastMonth,
            today
        )

        // Verify all scores are in valid range
        for (score in result) {
            assertTrue("Score ${score.value} should be >= 0", score.value >= 0.0)
            assertTrue("Score ${score.value} should be <= 1", score.value <= 1.0)
        }
    }

    @Test
    fun testCalculateAggregateStreaks_withEmptyScores_returnsEmptyList() {
        val result = calculator.calculateAggregateStreaks(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateAggregateStreaks_withSingleScore_returnsEmptyList() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today, 0.5)
        )

        val result = calculator.calculateAggregateStreaks(scores)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateAggregateStreaks_withNoImprovements_returnsEmptyList() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.4),
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.3),
            org.isoron.uhabits.core.models.Score(today, 0.2)
        )

        val result = calculator.calculateAggregateStreaks(scores)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateAggregateStreaks_withEqualScores_doesNotCountAsImprovement() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.5),
            org.isoron.uhabits.core.models.Score(today, 0.5)
        )

        val result = calculator.calculateAggregateStreaks(scores)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCalculateAggregateStreaks_withSingleStreak_returnsCorrectStreak() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(4), 0.2),
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.3),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.7),
            org.isoron.uhabits.core.models.Score(today, 0.9)
        )

        val result = calculator.calculateAggregateStreaks(scores)

        assertEquals(1, result.size)
        assertEquals(today.minus(3), result[0].start)
        assertEquals(today, result[0].end)
        assertEquals(4, result[0].length)
    }

    @Test
    fun testCalculateAggregateStreaks_withMultipleStreaks_returnsAllStreaks() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(10), 0.1),
            org.isoron.uhabits.core.models.Score(today.minus(9), 0.2), // Streak 1: 2 days
            org.isoron.uhabits.core.models.Score(today.minus(8), 0.1), // Break
            org.isoron.uhabits.core.models.Score(today.minus(7), 0.2),
            org.isoron.uhabits.core.models.Score(today.minus(6), 0.3),
            org.isoron.uhabits.core.models.Score(today.minus(5), 0.4), // Streak 2: 3 days
            org.isoron.uhabits.core.models.Score(today.minus(4), 0.3), // Break
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.4),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.6),
            org.isoron.uhabits.core.models.Score(today, 0.7) // Streak 3: 4 days
        )

        val result = calculator.calculateAggregateStreaks(scores)

        assertEquals(3, result.size)
        
        // Verify first streak (most recent)
        assertEquals(today.minus(3), result[0].start)
        assertEquals(today, result[0].end)
        assertEquals(4, result[0].length)
        
        // Verify second streak
        assertEquals(today.minus(7), result[1].start)
        assertEquals(today.minus(5), result[1].end)
        assertEquals(3, result[1].length)
        
        // Verify third streak (oldest)
        assertEquals(today.minus(9), result[2].start)
        assertEquals(today.minus(9), result[2].end)
        assertEquals(1, result[2].length)
    }

    @Test
    fun testCalculateAggregateStreaks_withMoreThan10Streaks_returnsLast10() {
        val today = DateUtils.getToday()
        val scores = mutableListOf<org.isoron.uhabits.core.models.Score>()
        
        // Create 15 streaks, each 2 days long with 1 day break between
        for (i in 44 downTo 0 step 3) {
            scores.add(org.isoron.uhabits.core.models.Score(today.minus(i), 0.3))
            scores.add(org.isoron.uhabits.core.models.Score(today.minus(i - 1), 0.5)) // Improvement
            if (i > 0) {
                scores.add(org.isoron.uhabits.core.models.Score(today.minus(i - 2), 0.2)) // Break
            }
        }

        val result = calculator.calculateAggregateStreaks(scores)

        // Should only return the last 10 streaks
        assertEquals(10, result.size)
        
        // Verify the first streak is the most recent one (reversed order)
        assertEquals(today.minus(1), result.first().end)
    }

    @Test
    fun testCalculateAggregateStreaks_withStrictImprovementOnly() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(5), 0.500000),
            org.isoron.uhabits.core.models.Score(today.minus(4), 0.600000),
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.700000),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.700000), // Equal - breaks streak
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.800000),
            org.isoron.uhabits.core.models.Score(today, 0.900000)
        )

        val result = calculator.calculateAggregateStreaks(scores)

        // Should have 2 streaks because equal score breaks the streak
        assertEquals(2, result.size)
        
        // First streak (most recent): days -1 to today (improvements)
        assertEquals(today.minus(1), result[0].start)
        assertEquals(today, result[0].end)
        assertEquals(2, result[0].length)
        
        // Second streak (older): days -4 to -3 (improvements)
        assertEquals(today.minus(4), result[1].start)
        assertEquals(today.minus(3), result[1].end)
        assertEquals(2, result[1].length)
    }

    @Test
    fun testCalculateAggregateStreaks_withOngoingStreak_includesCurrentDay() {
        val today = DateUtils.getToday()
        val scores = listOf(
            org.isoron.uhabits.core.models.Score(today.minus(3), 0.5),
            org.isoron.uhabits.core.models.Score(today.minus(2), 0.6),
            org.isoron.uhabits.core.models.Score(today.minus(1), 0.7),
            org.isoron.uhabits.core.models.Score(today, 0.8) // Ongoing streak
        )

        val result = calculator.calculateAggregateStreaks(scores)

        assertEquals(1, result.size)
        assertEquals(today.minus(2), result[0].start)
        assertEquals(today, result[0].end)
        assertEquals(3, result[0].length)
    }

    @Test
    fun testFindEarliestHabitDate_withRecentEntries_doesNotUseFallback() {
        // Simulates real scenario: user started logging in Aug 2025, but fallback is Dec 2024
        // The function should return Aug 2025 (actual earliest), not Dec 2024 (fallback)
        val habit = fixtures.createShortHabit()
        val today = DateUtils.getToday() // Dec 18, 2025
        val firstEntry = today.minus(137) // Aug 3, 2025 (137 days ago from Dec 18)
        
        habit.originalEntries.add(Entry(firstEntry, Entry.YES_MANUAL))
        
        // BUG: If we pass today.minus(365) as fallback (Dec 18, 2024),
        // it's OLDER than Aug 2025, so function incorrectly returns Dec 2024
        val buggyResult = calculator.findEarliestHabitDate(
            listOf(habit),
            today.minus(365) // Dec 18, 2024
        )
        
        // CORRECT: Should return Aug 3, 2025 (actual earliest entry)
        assertEquals(firstEntry, buggyResult)
        
        // With proper fallback (today), it correctly finds Aug 2025
        val correctResult = calculator.findEarliestHabitDate(
            listOf(habit),
            today
        )
        
        assertEquals(firstEntry, correctResult)
    }
}
