/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.activities.habits.list.controllers;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.activities.habits.list.model.*;
import org.isoron.uhabits.activities.habits.list.views.*;
import org.junit.*;

import java.util.*;

import static org.mockito.Mockito.*;

public class HabitCardListControllerTest extends BaseUnitTest
{

    private LinkedList<Habit> habits;

    private HabitCardListView view;

    private HabitCardListAdapter adapter;

    private HabitCardListController controller;

    private HabitCardListController.HabitListener habitListener;

    private HabitCardListController.SelectionListener selectionListener;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        this.view = mock(HabitCardListView.class);
        this.adapter = mock(HabitCardListAdapter.class);
        this.habitListener = mock(HabitCardListController.HabitListener.class);
        this.selectionListener =
                mock(HabitCardListController.SelectionListener.class);

        habits = new LinkedList<>();
        for (int i = 0; i < 10; i++)
        {
            Habit mock = mock(Habit.class);
            habits.add(mock);

        }

        resetMocks();

        this.controller = new HabitCardListController(adapter);
        controller.setHabitListener(habitListener);
        controller.setSelectionListener(selectionListener);
        view.setController(controller);
    }

    @Test
    public void testClick_withSelection()
    {
        controller.onItemLongClick(0);
        verify(adapter).toggleSelection(0);
        verify(selectionListener).onSelectionStart();
        resetMocks();

        controller.onItemClick(1);
        verify(adapter).toggleSelection(1);
        verify(selectionListener).onSelectionChange();
        resetMocks();

        controller.onItemClick(1);
        verify(adapter).toggleSelection(1);
        verify(selectionListener).onSelectionChange();
        resetMocks();

        doReturn(true).when(adapter).isSelectionEmpty();
        controller.onItemClick(0);
        verify(adapter).toggleSelection(0);
        verify(selectionListener).onSelectionFinish();
    }

    @Test
    public void testClick_withoutSelection()
    {
        controller.onItemClick(0);
        verify(habitListener).onHabitClick(habits.get(0));
    }

    @Test
    public void testDragAndDrop_withSelection()
    {
        controller.onItemLongClick(0);
        verify(adapter).toggleSelection(0);
        verify(selectionListener).onSelectionStart();
        resetMocks();

        controller.startDrag(1);
        verify(selectionListener).onSelectionChange();
        verify(adapter).toggleSelection(1);
        resetMocks();

        controller.drop(1, 3);
        verify(habitListener).onHabitReorder(habits.get(1), habits.get(3));
        verify(selectionListener).onSelectionFinish();
        verify(adapter).performReorder(1, 3);
        resetMocks();
    }

    @Test
    public void testDragAndDrop_withoutSelection_distinctPlace()
    {
        controller.startDrag(0);
        verify(selectionListener).onSelectionStart();
        verify(adapter).toggleSelection(0);
        resetMocks();

        controller.drop(0, 3);
        verify(habitListener).onHabitReorder(habits.get(0), habits.get(3));
        verify(selectionListener).onSelectionFinish();
        verify(adapter).performReorder(0, 3);
        verify(adapter).clearSelection();
    }

    @Test
    public void testInvalidToggle()
    {
        controller.onInvalidToggle();
        verify(habitListener).onInvalidToggle();
    }

    @Test
    public void testLongClick_withSelection()
    {
        controller.onItemLongClick(0);
        verify(adapter).toggleSelection(0);
        verify(selectionListener).onSelectionStart();
        resetMocks();

        controller.onItemLongClick(1);
        verify(adapter).toggleSelection(1);
        verify(selectionListener).onSelectionChange();
    }

    @Test
    public void testToggle()
    {
        controller.onToggle(habits.getFirst(), 0);
        verify(habitListener).onToggle(habits.getFirst(), 0);
    }

    protected void resetMocks()
    {
        reset(adapter, habitListener, selectionListener);
        for (int i = 0; i < habits.size(); i++)
            doReturn(habits.get(i)).when(adapter).getItem(i);
    }
}