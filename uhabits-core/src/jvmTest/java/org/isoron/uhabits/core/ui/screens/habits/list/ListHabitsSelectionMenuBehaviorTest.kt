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

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.callbacks.OnColorPickedCallback
import org.isoron.uhabits.core.ui.callbacks.OnConfirmedCallback
import org.junit.Test

class ListHabitsSelectionMenuBehaviorTest : BaseUnitTest() {
    private val screen: ListHabitsSelectionMenuBehavior.Screen = mock()

    private val adapter: ListHabitsSelectionMenuBehavior.Adapter = mock()
    private lateinit var behavior: ListHabitsSelectionMenuBehavior
    private lateinit var habit1: Habit
    private lateinit var habit2: Habit
    private lateinit var habit3: Habit

    private val colorPickerCallback: KArgumentCaptor<OnColorPickedCallback> = argumentCaptor()

    private val deleteCallback: KArgumentCaptor<OnConfirmedCallback> = argumentCaptor()

    @Test
    @Throws(Exception::class)
    fun canArchive() {
        whenever(adapter.getSelected()).thenReturn(listOf(habit1, habit2))
        assertFalse(behavior.canArchive())
        whenever(adapter.getSelected()).thenReturn(listOf(habit2, habit3))
        assertTrue(behavior.canArchive())
    }

    @Test
    @Throws(Exception::class)
    fun canEdit() {
        whenever(adapter.getSelected()).thenReturn(listOf(habit1))
        assertTrue(behavior.canEdit())
        whenever(adapter.getSelected()).thenReturn(listOf(habit1, habit2))
        assertFalse(behavior.canEdit())
    }

    @Test
    @Throws(Exception::class)
    fun canUnarchive() {
        whenever(adapter.getSelected()).thenReturn(listOf(habit1, habit2))
        assertFalse(behavior.canUnarchive())
        whenever(adapter.getSelected()).thenReturn(listOf(habit1))
        assertTrue(behavior.canUnarchive())
    }

    @Test
    @Throws(Exception::class)
    fun onArchiveHabits() {
        assertFalse(habit2.isArchived)
        whenever(adapter.getSelected()).thenReturn(listOf(habit2))
        behavior.onArchiveHabits()
        assertTrue(habit2.isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun onChangeColor() {
        assertThat(habit1.color, equalTo(PaletteColor(8)))
        assertThat(habit2.color, equalTo(PaletteColor(8)))
        whenever(adapter.getSelected()).thenReturn(listOf(habit1, habit2))
        behavior.onChangeColor()
        verify(screen)
            .showColorPicker(eq(PaletteColor(8)), colorPickerCallback.capture())
        colorPickerCallback.lastValue.onColorPicked(PaletteColor(30))
        assertThat(habit1.color, equalTo(PaletteColor(30)))
    }

    @Test
    @Throws(Exception::class)
    fun onDeleteHabits() {
        val id = habit1.id!!
        habitList.getById(id)!!
        whenever(adapter.getSelected()).thenReturn(listOf(habit1))
        behavior.onDeleteHabits()
        verify(screen).showDeleteConfirmationScreen(deleteCallback.capture(), eq(1))
        deleteCallback.lastValue.onConfirmed()
        assertNull(habitList.getById(id))
    }

    @Test
    @Throws(Exception::class)
    fun onEditHabits() {
        val selected: List<Habit> = listOf(habit1, habit2)
        whenever(adapter.getSelected()).thenReturn(selected)
        behavior.onEditHabits()
        verify(screen).showEditHabitsScreen(selected)
    }

    @Test
    @Throws(Exception::class)
    fun onUnarchiveHabits() {
        assertTrue(habit1.isArchived)
        whenever(adapter.getSelected()).thenReturn(listOf(habit1))
        behavior.onUnarchiveHabits()
        assertFalse(habit1.isArchived)
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
        behavior = ListHabitsSelectionMenuBehavior(
            habitList,
            screen,
            adapter,
            commandRunner
        )
    }
}
