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
package org.isoron.uhabits.core.models

import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.number.IsCloseTo
import org.hamcrest.number.OrderingComparison
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

open class BaseScoreListTest : BaseUnitTest() {
    protected lateinit var habit: Habit
    protected lateinit var today: Timestamp

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        today = getToday()
    }

    protected fun checkScoreValues(expectedValues: DoubleArray) {
        var current = today
        val scores = habit.scores
        for (expectedValue in expectedValues) {
            assertThat(scores[current].value, IsCloseTo.closeTo(expectedValue, E))
            current = current.minus(1)
        }
    }

    companion object {
        const val E = 1e-6
    }
}

class YesNoScoreListTest : BaseScoreListTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyHabit()
    }

    @Test
    fun test_getValue() {
        check(0, 20)
        val expectedValues = doubleArrayOf(
            0.655747,
            0.636894,
            0.617008,
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067,
            0.147820,
            0.101149,
            0.051922,
            0.000000,
            0.000000,
            0.000000
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun test_getValueWithSkip() {
        check(0, 20)
        addSkip(5)
        addSkip(10)
        addSkip(11)
        habit.recompute()
        val expectedValues = doubleArrayOf(
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244,
            0.347244,
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067,
            0.147820,
            0.101149,
            0.051922,
            0.000000,
            0.000000,
            0.000000
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun test_getValueWithSkip2() {
        check(5)
        addSkip(4)
        habit.recompute()
        val expectedValues = doubleArrayOf(
            0.041949,
            0.044247,
            0.046670,
            0.049226,
            0.051922,
            0.051922,
            0.0
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun test_imperfectNonDaily() {
        // If the habit should be performed 3 times per week and the user misses 1 repetition
        // each week, score should converge to 66%.
        habit.frequency = Frequency(3, 7)
        val values = ArrayList<Int>()
        for (k in 0..99) {
            values.add(Entry.YES_MANUAL)
            values.add(Entry.YES_MANUAL)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
        }
        check(values)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(2 / 3.0, E))

        // Missing 2 repetitions out of 4 per week, the score should converge to 50%
        habit.frequency = Frequency(4, 7)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.5, E))
    }

    @Test
    fun test_irregularNonDaily() {
        // If the user performs habit perfectly each week, but on different weekdays,
        // score should still converge to 100%
        habit.frequency = Frequency(1, 7)
        val values = ArrayList<Int>()
        for (k in 0..99) {
            // Week 0
            values.add(Entry.YES_MANUAL)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)

            // Week 1
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.NO)
            values.add(Entry.YES_MANUAL)
        }
        check(values)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(1.0, 1e-3))
    }

    @Test
    fun shouldAchieveHighScoreInReasonableTime() {
        // Daily habits should achieve at least 99% in 3 months
        habit = fixtures.createEmptyHabit()
        habit.frequency = Frequency.DAILY
        for (i in 0..89) check(i)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))

        // Weekly habits should achieve at least 99% in 9 months
        habit = fixtures.createEmptyHabit()
        habit.frequency = Frequency.WEEKLY
        for (i in 0..38) check(7 * i)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))

        // Monthly habits should achieve at least 99% in 18 months
        habit.frequency = Frequency(1, 30)
        for (i in 0..17) check(30 * i)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))
    }

    @Test
    fun test_recompute() {
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.0, E))
        check(0, 2)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.101149, E))
        habit.frequency = Frequency(1, 2)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.054816, E))
    }

    @Test
    fun test_addThenRemove() {
        val habit = fixtures.createEmptyHabit()
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.0, E))
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.051922, E))
        habit.originalEntries.add(Entry(today, Entry.UNKNOWN))
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.0, E))
    }

    private fun check(offset: Int) {
        val entries = habit.originalEntries
        entries.add(Entry(today.minus(offset), Entry.YES_MANUAL))
    }

    private fun check(from: Int, to: Int) {
        val entries = habit.originalEntries
        for (i in from until to) entries.add(Entry(today.minus(i), Entry.YES_MANUAL))
        habit.recompute()
    }

    private fun check(values: ArrayList<Int>) {
        val entries = habit.originalEntries
        for (i in values.indices) if (values[i] == Entry.YES_MANUAL) entries.add(
            Entry(
                today.minus(i),
                Entry.YES_MANUAL
            )
        )
        habit.recompute()
    }

    private fun addSkip(day: Int) {
        val entries = habit.originalEntries
        entries.add(Entry(today.minus(day), Entry.SKIP))
    }
}

open class NumericalScoreListTest : BaseScoreListTest() {
    protected fun addEntry(day: Int, value: Int) {
        val entries = habit.originalEntries
        entries.add(Entry(today.minus(day), value))
    }

    protected fun addEntries(from: Int, to: Int, value: Int) {
        val entries = habit.originalEntries
        for (i in from until to) entries.add(Entry(today.minus(i), value))
        habit.recompute()
    }
}

class NumericalAtLeastScoreListTest : NumericalScoreListTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_LEAST)
    }

    @Test
    fun test_withZeroTarget() {
        habit = fixtures.createNumericalHabit()
        habit.targetValue = 0.0
        habit.recompute()
        assertTrue(habit.scores[today].value.isFinite())
    }

    @Test
    fun test_getValue() {
        addEntries(0, 20, 2000)
        val expectedValues = doubleArrayOf(
            0.655747,
            0.636894,
            0.617008,
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067,
            0.147820,
            0.101149,
            0.051922,
            0.000000,
            0.000000,
            0.000000
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun test_recompute() {
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.0, E))
        addEntries(0, 2, 2000)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.101149, E))
        habit.frequency = Frequency(1, 2)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.072631, E))
    }

    @Test
    fun shouldAchieveHighScoreInReasonableTime() {
        // Daily habits should achieve at least 99% in 3 months
        habit = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_LEAST)
        habit.frequency = Frequency.DAILY
        for (i in 0..89) addEntry(i, 2000)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))

        // Weekly habits should achieve at least 99% in 9 months
        habit = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_LEAST)
        habit.frequency = Frequency.WEEKLY
        for (i in 0..38) addEntry(7 * i, 2000)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))

        // Monthly habits should achieve at least 99% in 18 months
        habit.frequency = Frequency(1, 30)
        for (i in 0..17) addEntry(30 * i, 2000)
        habit.recompute()
        assertThat(habit.scores[today].value, OrderingComparison.greaterThan(0.99))
    }

    @Test
    fun shouldAchieveComparableScoreToProgress() {
        addEntries(0, 500, 1000)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.5, E))

        addEntries(0, 500, 500)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.25, E))
    }

    @Test
    fun overeachievingIsntRelevant() {
        addEntry(0, 10000000)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.051922, E))
    }
}

class NumericalAtLeastScoreListWithSkipTest : NumericalScoreListTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_LEAST)
    }

    @Test
    fun test_getValue() {
        addEntries(0, 10, 2000)
        addEntries(10, 11, SKIP)
        addEntries(11, 15, 2000)
        addEntries(15, 16, SKIP)
        addEntries(16, 20, 2000)
        val expectedValues = doubleArrayOf(
            0.617008,
            0.596033,
            0.573910,
            0.550574,
            0.525961,
            0.500000,
            0.472617,
            0.443734,
            0.413270,
            0.381137,
            0.347244, // skipped day should have the same score as the previous day
            0.347244,
            0.311495,
            0.273788,
            0.234017,
            0.192067, // skipped day should have the same score as the previous day
            0.192067,
            0.147820,
            0.101149,
            0.051922,
            0.000000,
            0.000000,
            0.000000
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun skipsShouldNotAffectScore() {
        addEntries(0, 500, 1000)
        val initialScore = habit.scores[today].value

        addEntries(500, 1000, SKIP)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(initialScore, E))

        addEntries(0, 300, 1000)
        addEntries(300, 500, SKIP)
        addEntries(500, 700, 1000)

        // skipped days should be treated as if they never existed
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(initialScore, E))
    }
}

class NumericalAtMostScoreListTest : NumericalScoreListTest() {
    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit = fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_MOST)
    }

    @Test
    fun test_withZeroTarget() {
        habit = fixtures.createNumericalHabit()
        habit.targetType = NumericalHabitType.AT_MOST
        habit.targetValue = 0.0
        habit.recompute()
        assertTrue(habit.scores[today].value.isFinite())
    }

    @Test
    fun test_getValue() {
        addEntry(20, 1000)
        addEntries(0, 20, 5000)
        val expectedValues = doubleArrayOf(
            0.344253,
            0.363106,
            0.382992,
            0.403967,
            0.426090,
            0.449426,
            0.474039,
            0.500000,
            0.527383,
            0.556266,
            0.586730,
            0.618863,
            0.652756,
            0.688505,
            0.726212,
            0.765983,
            0.807933,
            0.852180,
            0.898851,
            0.948078,
            1.0,
            0.0,
            0.0
        )
        checkScoreValues(expectedValues)
    }

    @Test
    fun test_recompute() {
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(1.0, E))
        addEntries(0, 2, 5000)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.898850, E))
        habit.frequency = Frequency(1, 2)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.927369, E))
    }

    @Test
    fun shouldAchieveComparableScoreToProgress() {
        addEntries(0, 500, 3000)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.5, E))

        addEntries(0, 500, 3500)
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.25, E))
    }

    @Test
    fun undereachievingIsntRelevant() {
        addEntry(1, 10000000)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.950773, E))
    }

    @Test
    fun overeachievingIsntRelevant() {
        addEntry(0, 5000)

        addEntry(1, 0)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.948077, E))

        addEntry(1, 1000)
        habit.recompute()
        assertThat(habit.scores[today].value, IsCloseTo.closeTo(0.948077, E))
    }
}
