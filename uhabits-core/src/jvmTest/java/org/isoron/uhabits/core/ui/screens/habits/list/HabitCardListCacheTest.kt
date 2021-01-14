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
package org.isoron.uhabits.core.ui.screens.habits.list

import junit.framework.Assert.assertNotNull
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test
import org.mockito.Mockito

class HabitCardListCacheTest : BaseUnitTest() {
    private var cache: HabitCardListCache? = null
    private var listener: HabitCardListCache.Listener? = null
    var today = getToday()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habitList.removeAll()
        for (i in 0..9) {
            if (i == 3) habitList.add(fixtures.createLongHabit()) else habitList.add(fixtures.createShortHabit())
        }
        cache = HabitCardListCache(habitList, commandRunner, taskRunner)
        cache!!.setCheckmarkCount(10)
        cache!!.refreshAllHabits()
        cache!!.onAttached()
        listener = Mockito.mock(
            HabitCardListCache.Listener::class.java
        )
        cache!!.setListener(listener!!)
    }

    override fun tearDown() {
        cache!!.onDetached()
    }

    @Test
    fun testCommandListener_all() {
        assertThat(cache!!.habitCount, IsEqual.equalTo(10))
        val h = habitList.getByPosition(0)
        commandRunner.run(
            DeleteHabitsCommand(habitList, listOf(h))
        )
        Mockito.verify(listener)!!.onItemRemoved(0)
        Mockito.verify(listener)!!.onRefreshFinished()
        assertThat(cache!!.habitCount, IsEqual.equalTo(9))
    }

    @Test
    fun testCommandListener_single() {
        val h2 = habitList.getByPosition(2)
        commandRunner.run(CreateRepetitionCommand(habitList, h2, today, Entry.NO))
        Mockito.verify(listener)!!.onItemChanged(2)
        Mockito.verify(listener)!!.onRefreshFinished()
        Mockito.verifyNoMoreInteractions(listener)
    }

    @Test
    fun testGet() {
        assertThat(cache!!.habitCount, IsEqual.equalTo(10))
        val h = habitList.getByPosition(3)
        assertNotNull(h.id)
        val score = h.scores[today].value
        assertThat(cache!!.getHabitByPosition(3), IsEqual.equalTo(h))
        assertThat(cache!!.getScore(h.id!!), IsEqual.equalTo(score))
        val actualCheckmarks = cache!!.getCheckmarks(h.id!!)

        val expectedCheckmarks = h
            .computedEntries
            .getByInterval(today.minus(9), today)
            .map { it.value }.toIntArray()
        assertThat(actualCheckmarks, IsEqual.equalTo(expectedCheckmarks))
    }

    @Test
    fun testRemoval() {
        removeHabitAt(0)
        removeHabitAt(3)
        cache!!.refreshAllHabits()
        Mockito.verify(listener)!!.onItemRemoved(0)
        Mockito.verify(listener)!!.onItemRemoved(3)
        Mockito.verify(listener)!!.onRefreshFinished()
        assertThat(cache!!.habitCount, IsEqual.equalTo(8))
    }

    @Test
    fun testRefreshWithNoChanges() {
        cache!!.refreshAllHabits()
        Mockito.verify(listener)!!.onRefreshFinished()
        Mockito.verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onCache() {
        val h2 = cache!!.getHabitByPosition(2)
        val h3 = cache!!.getHabitByPosition(3)
        val h7 = cache!!.getHabitByPosition(7)
        cache!!.reorder(2, 7)
        assertThat(cache!!.getHabitByPosition(2), IsEqual.equalTo(h3))
        assertThat(cache!!.getHabitByPosition(7), IsEqual.equalTo(h2))
        assertThat(cache!!.getHabitByPosition(6), IsEqual.equalTo(h7))
        Mockito.verify(listener)!!.onItemMoved(2, 7)
        Mockito.verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onList() {
        val h2 = habitList.getByPosition(2)
        val h3 = habitList.getByPosition(3)
        val h7 = habitList.getByPosition(7)
        assertThat(cache!!.getHabitByPosition(2), IsEqual.equalTo(h2))
        assertThat(cache!!.getHabitByPosition(7), IsEqual.equalTo(h7))
        Mockito.reset(listener)
        habitList.reorder(h2, h7)
        cache!!.refreshAllHabits()
        assertThat(cache!!.getHabitByPosition(2), IsEqual.equalTo(h3))
        assertThat(cache!!.getHabitByPosition(7), IsEqual.equalTo(h2))
        assertThat(cache!!.getHabitByPosition(6), IsEqual.equalTo(h7))
        Mockito.verify(listener)!!.onItemMoved(3, 2)
        Mockito.verify(listener)!!.onItemMoved(4, 3)
        Mockito.verify(listener)!!.onItemMoved(5, 4)
        Mockito.verify(listener)!!.onItemMoved(6, 5)
        Mockito.verify(listener)!!.onItemMoved(7, 6)
        Mockito.verify(listener)!!.onRefreshFinished()
        Mockito.verifyNoMoreInteractions(listener)
    }

    private fun removeHabitAt(position: Int) {
        val h = habitList.getByPosition(position)
        assertNotNull(h)
        habitList.remove(h)
    }
}
