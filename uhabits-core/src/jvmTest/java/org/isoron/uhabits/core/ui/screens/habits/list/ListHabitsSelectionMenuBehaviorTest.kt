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

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.junit.Test
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListHabitsSelectionMenuBehaviorTest : BaseUnitTest() {
    private val screen: ListHabitsSelectionMenuBehavior.Screen = mock()

    private val adapter: ListHabitsSelectionMenuBehavior.Adapter = mock()
    private lateinit var behavior: ListHabitsSelectionMenuBehavior
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit
    private lateinit var habit3: Habit

    private lateinit var hgr1: HabitGroup
    private lateinit var hgr2: HabitGroup
    private lateinit var hgr3: HabitGroup

    private val colorPickerCallback: KArgumentCaptor<OnColorPickedCallback> = argumentCaptor()

    private val deleteCallback: KArgumentCaptor<OnConfirmedCallback> = argumentCaptor()

    @Test
    @Throws(Exception::class)
    fun canArchive() {
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1, habit2))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1, hgr3))
        assertFalse(behavior.canArchive())
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit2, habit3))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1, hgr2))
        assertTrue(behavior.canArchive())
    }

    @Test
    @Throws(Exception::class)
    fun canEdit() {
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1))
        assertTrue(behavior.canEdit())
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1, habit2))
        assertFalse(behavior.canEdit())
    }

    @Test
    @Throws(Exception::class)
    fun canEditGroups() {
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1))
        assertTrue(behavior.canEdit())
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1, hgr2))
        assertFalse(behavior.canEdit())
    }

    @Test
    @Throws(Exception::class)
    fun canUnarchive() {
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1, habit2))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1, hgr2))
        assertFalse(behavior.canUnarchive())
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr3))
        assertTrue(behavior.canUnarchive())
    }

    @Test
    @Throws(Exception::class)
    fun onArchiveHabits() {
        assertFalse(habit2.isArchived)
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit2))
        behavior.onArchiveHabits()
        assertTrue(habit2.isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun onArchiveHabitGroups() {
        assertFalse(hgr2.isArchived)
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr2))
        behavior.onArchiveHabits()
        assertTrue(hgr2.isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun onChangeColor() {
        assertThat(habit1.color, equalTo(PaletteColor(8)))
        assertThat(habit2.color, equalTo(PaletteColor(8)))
        assertThat(hgr1.color, equalTo(PaletteColor(3)))
        assertThat(hgr2.color, equalTo(PaletteColor(3)))
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1, habit2))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr1, hgr2))
        behavior.onChangeColor()
        verify(screen)
            .showColorPicker(eq(PaletteColor(8)), colorPickerCallback.capture())
        colorPickerCallback.lastValue.onColorPicked(PaletteColor(30))
        assertThat(habit1.color, equalTo(PaletteColor(30)))
        assertThat(hgr1.color, equalTo(PaletteColor(30)))
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteHabits() {
        val id = habit1.id!!
        habitList.getById(id)!!
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1))
        behavior.onDeleteHabits()
        verify(screen).showDeleteConfirmationScreen(deleteCallback.capture(), eq(1))
        deleteCallback.lastValue.onConfirmed()
        assertNull(habitList.getById(id))
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteHabitGroups() {
        val id = hgr2.id!!
        val hgr = habitGroupList.getById(id)!!
        val hId = hgr.habitList.getByPosition(0).id!!
        habitGroupList.getHabitByID(hId)!!
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr2))
        behavior.onDeleteHabits()
        verify(screen).showDeleteConfirmationScreen(deleteCallback.capture(), eq(1))
        deleteCallback.lastValue.onConfirmed()
        assertNull(habitGroupList.getById(id))
        assertNull(habitGroupList.getHabitByID(hId))
    }

    @Test
    @Throws(Exception::class)
    fun onEditHabits() {
        val selected: List<Habit> = listOf(habit1, habit2)
        whenever(adapter.getSelectedHabits()).thenReturn(selected)
        behavior.onEditHabits()
        verify(screen).showEditHabitsScreen(selected)
    }

    @Test
    @Throws(Exception::class)
    fun onEditHabitGroup() {
        val selected: List<HabitGroup> = listOf(hgr1)
        whenever(adapter.getSelectedHabitGroups()).thenReturn(selected)
        behavior.onEditHabits()
        verify(screen).showEditHabitGroupScreen(selected)
    }

    @Test
    @Throws(Exception::class)
    fun onUnarchiveHabits() {
        assertTrue(habit1.isArchived)
        assertTrue(hgr3.isArchived)
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1))
        whenever(adapter.getSelectedHabitGroups()).thenReturn(listOf(hgr3))
        behavior.onUnarchiveHabits()
        assertFalse(habit1.isArchived)
        assertFalse(hgr3.isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun testSelectionType() {
        val subHabit = hgr2.habitList.getByPosition(0)
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(habit1, habit2, subHabit))
        assertTrue(behavior.areHabits())
        assertFalse(behavior.areSubHabits())
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(subHabit))
        assertTrue(behavior.areSubHabits())
    }

    @Test
    @Throws(Exception::class)
    fun testAddToGroup() {
        assertFalse(habit1.isSubHabit())
        val selected = listOf(habit1)
        whenever(adapter.getSelectedHabits()).thenReturn(selected)
        behavior.onAddToGroup()
        verify(screen).showHabitGroupPickerDialog(selected)
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveFromGroup() {
        val subHabit = hgr2.habitList.getByPosition(0)
        assertTrue(subHabit.isSubHabit())
        whenever(adapter.getSelectedHabits()).thenReturn(listOf(subHabit))
        behavior.onRemoveFromGroup()
        assertFalse(subHabit.isSubHabit())
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        habit1 = fixtures.createShortHabit()
        habit1.isArchived = true
        habit2 = fixtures.createShortHabit()
        habit3 = fixtures.createShortHabit()
        habitList.add(habit1)
        habitList.add(habit2)
        habitList.add(habit3)

        hgr1 = groupFixtures.createEmptyHabitGroup(id = 4L)
        hgr2 = groupFixtures.createGroupWithShortHabits(id = 5L)
        hgr3 = groupFixtures.createGroupWithShortHabits(id = 7L)
        hgr3.isArchived = true
        habitGroupList.add(hgr1)
        habitGroupList.add(hgr2)
        habitGroupList.add(hgr3)

        behavior = ListHabitsSelectionMenuBehavior(
            habitList,
            habitGroupList,
            screen,
            adapter,
            commandRunner
        )
    }
}
