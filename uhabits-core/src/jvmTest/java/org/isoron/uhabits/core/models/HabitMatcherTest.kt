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
package org.isoron.uhabits.core.models

import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HabitMatcherTest : BaseUnitTest() {

    private fun buildHabit(
        name: String,
        question: String = "",
        description: String = ""
    ): Habit {
        val habit = modelFactory.buildHabit()
        habit.name = name
        habit.question = question
        habit.description = description
        return habit
    }

    @Test
    fun testSearchByName() {
        val habit = buildHabit("Yoga practice")
        assertTrue(HabitMatcher(searchQuery = "yoga").matches(habit))
    }

    @Test
    fun testSearchByQuestion() {
        val habit = buildHabit("Exercise", question = "Did you do yoga today?")
        assertTrue(HabitMatcher(searchQuery = "yoga").matches(habit))
    }

    @Test
    fun testSearchByNotes() {
        val habit = buildHabit("Exercise", description = "morning yoga routine")
        assertTrue(HabitMatcher(searchQuery = "yoga").matches(habit))
    }

    @Test
    fun testSearchCaseInsensitive() {
        val habit = buildHabit("yoga practice")
        assertTrue(HabitMatcher(searchQuery = "YOGA").matches(habit))
    }

    @Test
    fun testSearchNoMatch() {
        val habit = buildHabit("Running", question = "Did you run?", description = "daily jog")
        assertFalse(HabitMatcher(searchQuery = "yoga").matches(habit))
    }

    @Test
    fun testSearchEmptyQuery() {
        val habit = buildHabit("Running", description = "daily jog")
        assertTrue(HabitMatcher(searchQuery = "").matches(habit))
    }

    // --- Whitespace handling ---

    @Test
    fun testSearchTrimsLeadingSpaces() {
        val habit = buildHabit("Yoga practice")
        assertTrue(HabitMatcher(searchQuery = "  yoga").matches(habit))
    }

    @Test
    fun testSearchTrimsTrailingSpaces() {
        val habit = buildHabit("Yoga practice")
        assertTrue(HabitMatcher(searchQuery = "yoga  ").matches(habit))
    }

    @Test
    fun testSearchWhitespaceOnlyQueryMatchesAll() {
        val habit = buildHabit("Running")
        assertTrue(HabitMatcher(searchQuery = "   ").matches(habit))
    }

    // --- Accented / Unicode characters ---

    @Test
    fun testSearchAccentedCharacterInName() {
        val habit = buildHabit("Méditer")
        assertTrue(HabitMatcher(searchQuery = "méditer").matches(habit))
    }

    @Test
    fun testSearchCaseInsensitiveAccented() {
        val habit = buildHabit("méditer")
        assertTrue(HabitMatcher(searchQuery = "MÉDITER").matches(habit))
    }

    @Test
    fun testSearchUnaccentedQueryDoesNotMatchAccented() {
        // We do not normalise Unicode: "mediter" ≠ "méditer". Documented expected behaviour.
        val habit = buildHabit("Méditer")
        assertFalse(HabitMatcher(searchQuery = "mediter").matches(habit))
    }

    // --- Emoji, numbers, punctuation ---

    @Test
    fun testSearchEmojiDoesNotBlockTextMatch() {
        val habit = buildHabit("🧘 Yoga")
        assertTrue(HabitMatcher(searchQuery = "yoga").matches(habit))
    }

    @Test
    fun testSearchNumbersInName() {
        val habit = buildHabit("10k Run")
        assertTrue(HabitMatcher(searchQuery = "10k").matches(habit))
    }

    // --- Boundary / edge cases ---

    @Test
    fun testSearchSingleCharacter() {
        val habit = buildHabit("Yoga")
        assertTrue(HabitMatcher(searchQuery = "y").matches(habit))
    }

    @Test
    fun testSearchQueryLongerThanAllFields() {
        val habit = buildHabit("Run", question = "Did you run?", description = "jog")
        assertFalse(HabitMatcher(searchQuery = "a".repeat(100)).matches(habit))
    }

    @Test
    fun testSearchEmptyQuestionAndDescription() {
        // Only name is populated; match and non-match both work without crashing.
        val habit = buildHabit("Yoga")
        assertTrue(HabitMatcher(searchQuery = "yoga").matches(habit))
        assertFalse(HabitMatcher(searchQuery = "running").matches(habit))
    }
}
