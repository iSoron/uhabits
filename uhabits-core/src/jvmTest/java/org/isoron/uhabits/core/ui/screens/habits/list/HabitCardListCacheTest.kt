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

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteHabitGroupsCommand
import org.isoron.uhabits.core.commands.DeleteHabitsCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class HabitCardListCacheTest : BaseUnitTest() {
    private lateinit var cache: HabitCardListCache
    private lateinit var listener: HabitCardListCache.Listener
    lateinit var today: Timestamp

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        today = getToday()
        habitList.removeAll()
        for (i in 0..9) {
            if (i == 3) habitList.add(fixtures.createLongHabit()) else habitList.add(fixtures.createShortHabit())
        }
        habitGroupList.removeAll()
        for (i in 0..2) {
            habitGroupList.add(groupFixtures.createEmptyHabitGroup(id = 10L + i.toLong()))
        }
        cache = HabitCardListCache(habitList, habitGroupList, commandRunner, taskRunner, mock())
        cache.setCheckmarkCount(10)
        cache.refreshAllHabits()
        val hgr = habitGroupList.getByPosition(2)
        val h = fixtures.createShortHabit()
        hgr.habitList.add(h)
        h.id = 13L
        h.groupId = hgr.id
        h.group = hgr
        h.groupUUID = hgr.uuid
        cache.refreshAllHabits()
        cache.onAttached()
        listener = mock()
        cache.setListener(listener)
    }

    override fun tearDown() {
        cache.onDetached()
    }

    @Test
    fun testCommandListener_delete_habit() {
        assertThat(cache.habitCount, equalTo(10))
        val h = habitList.getByPosition(0)
        commandRunner.run(
            DeleteHabitsCommand(habitList, listOf(h))
        )
        verify(listener).onItemRemoved(0)
        verify(listener).onRefreshFinished()
        assertThat(cache.habitCount, equalTo(9))
    }

    @Test
    fun testCommandListener_delete_hgr() {
        assertThat(cache.habitGroupCount, equalTo(3))
        assertThat(cache.subHabitCount, equalTo(1))
        val hgr = habitGroupList.getByPosition(0)
        commandRunner.run(
            DeleteHabitGroupsCommand(habitGroupList, listOf(hgr))
        )
        verify(listener).onItemRemoved(10)
        verify(listener).onRefreshFinished()
        assertThat(cache.habitGroupCount, equalTo(2))
        assertThat(cache.subHabitCount, equalTo(1))

        val hgr2 = habitGroupList.getByPosition(1)
        commandRunner.run(
            DeleteHabitGroupsCommand(habitGroupList, listOf(hgr2))
        )
        verify(listener).onItemRemoved(11)
        verify(listener).onItemRemoved(12)
        verify(listener, times(2)).onRefreshFinished()
        assertThat(cache.habitGroupCount, equalTo(1))
        assertThat(cache.subHabitCount, equalTo(0))
    }

    @Test
    fun testCommandListener_single() {
        val h2 = habitList.getByPosition(2)
        commandRunner.run(CreateRepetitionCommand(habitList, h2, today, Entry.NO, ""))
        verify(listener).onItemChanged(2)
        verify(listener).onRefreshFinished()
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testCommandListener_single_sub_habit() {
        val hgr2 = habitGroupList.getByPosition(2)
        val h2 = hgr2.habitList.getByPosition(0)
        commandRunner.run(CreateRepetitionCommand(hgr2.habitList, h2, today, Entry.NO, ""))
        verify(listener).onItemChanged(13)
        verify(listener).onRefreshFinished()
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testGet() {
        assertThat(cache.habitCount, equalTo(10))
        val h = habitList.getByPosition(3)
        val score = h.scores[today].value
        assertThat(cache.getHabitByPosition(3), equalTo(h))
        assertThat(cache.getScore(h.id!!), equalTo(score))
        val actualCheckmarks = cache.getCheckmarks(h.id!!)

        val expectedCheckmarks = h
            .computedEntries
            .getByInterval(today.minus(9), today)
            .map { it.value }.toIntArray()
        assertThat(actualCheckmarks, equalTo(expectedCheckmarks))
    }

    @Test
    fun testGetGroup() {
        assertThat(cache.habitGroupCount, equalTo(3))
        val hgr = habitGroupList.getByPosition(2)
        val score = hgr.scores[today].value
        assertThat(cache.getHabitGroupByPosition(12), equalTo(hgr))
        assertThat(cache.getScore(hgr.id!!), equalTo(score))

        val h = hgr.habitList.getByPosition(0)
        val score2 = h.scores[today].value
        assertThat(cache.getHabitByPosition(13), equalTo(h))
        assertThat(cache.getScore(h.id!!), equalTo(score2))
        val actualCheckmarks = cache.getCheckmarks(h.id!!)

        val expectedCheckmarks = h
            .computedEntries
            .getByInterval(today.minus(9), today)
            .map { it.value }.toIntArray()
        assertThat(actualCheckmarks, equalTo(expectedCheckmarks))
    }

    @Test
    fun testRemoval() {
        removeHabitAt(0)
        removeHabitAt(3)
        removeHabitGroupAt(0)
        cache.refreshAllHabits()
        verify(listener).onItemRemoved(0)
        verify(listener).onItemRemoved(3)
        verify(listener).onItemRemoved(8)
        verify(listener).onRefreshFinished()
        assertThat(cache.habitCount, equalTo(8))
        assertThat(cache.habitGroupCount, equalTo(2))
    }

    @Test
    fun testRefreshWithNoChanges() {
        cache.refreshAllHabits()
        verify(listener).onRefreshFinished()
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onCache() {
        val h2 = cache.getHabitByPosition(2)
        val h3 = cache.getHabitByPosition(3)
        val h7 = cache.getHabitByPosition(7)
        cache.reorder(2, 7)
        assertThat(cache.getHabitByPosition(2), equalTo(h3))
        assertThat(cache.getHabitByPosition(7), equalTo(h2))
        assertThat(cache.getHabitByPosition(6), equalTo(h7))
        verify(listener).onItemMoved(2, 7)
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onCache_Groups() {
        val hgr10 = cache.getHabitGroupByPosition(10)
        val hgr11 = cache.getHabitGroupByPosition(11)
        val hgr12 = cache.getHabitGroupByPosition(12)
        val h13 = cache.getHabitByPosition(13)
        cache.reorder(10, 12)
        assertThat(cache.getHabitGroupByPosition(10), equalTo(hgr11))
        assertThat(cache.getHabitGroupByPosition(11), equalTo(hgr12))
        assertThat(cache.getHabitGroupByPosition(13), equalTo(hgr10))
        assertThat(cache.getHabitByPosition(12), equalTo(h13))
        verify(listener).onItemMoved(10, 12)
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onList() {
        val h2 = habitList.getByPosition(2)
        val h3 = habitList.getByPosition(3)
        val h7 = habitList.getByPosition(7)
        assertThat(cache.getHabitByPosition(2), equalTo(h2))
        assertThat(cache.getHabitByPosition(7), equalTo(h7))
        reset(listener)
        habitList.reorder(h2, h7)
        cache.refreshAllHabits()
        assertThat(cache.getHabitByPosition(2), equalTo(h3))
        assertThat(cache.getHabitByPosition(7), equalTo(h2))
        assertThat(cache.getHabitByPosition(6), equalTo(h7))
        verify(listener).onItemMoved(3, 2)
        verify(listener).onItemMoved(4, 3)
        verify(listener).onItemMoved(5, 4)
        verify(listener).onItemMoved(6, 5)
        verify(listener).onItemMoved(7, 6)
        verify(listener).onRefreshFinished()
        verifyNoMoreInteractions(listener)
    }

    @Test
    fun testReorder_onList_Groups() {
        val hgr10 = habitGroupList.getByPosition(0)
        val hgr11 = habitGroupList.getByPosition(1)
        val hgr12 = habitGroupList.getByPosition(2)
        val h13 = hgr12.habitList.getByPosition(0)
        assertThat(cache.getHabitGroupByPosition(10), equalTo(hgr10))
        assertThat(cache.getHabitGroupByPosition(12), equalTo(hgr12))
        reset(listener)
        habitGroupList.reorder(hgr10, hgr12)
        cache.refreshAllHabits()
        assertThat(cache.getHabitGroupByPosition(10), equalTo(hgr11))
        assertThat(cache.getHabitGroupByPosition(11), equalTo(hgr12))
        assertThat(cache.getHabitByPosition(12), equalTo(h13))
        assertThat(cache.getHabitGroupByPosition(13), equalTo(hgr10))
        verify(listener).onItemMoved(11, 10)
        verify(listener).onItemMoved(12, 11)
        verify(listener).onRefreshFinished()
        verifyNoMoreInteractions(listener)
    }

    private fun removeHabitAt(position: Int) {
        val h = habitList.getByPosition(position)
        habitList.remove(h)
    }

    private fun removeHabitGroupAt(position: Int) {
        val hgr = habitGroupList.getByPosition(position)
        habitGroupList.remove(hgr)
    }
}
