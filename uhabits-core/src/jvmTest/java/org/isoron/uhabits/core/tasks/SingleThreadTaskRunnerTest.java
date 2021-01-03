/*
 * Copyright (C) 2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.core.tasks;

import org.isoron.uhabits.core.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mockito.*;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class SingleThreadTaskRunnerTest extends BaseUnitTest
{
    private SingleThreadTaskRunner runner;

    private Task task;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        runner = new SingleThreadTaskRunner();
        task = mock(Task.class);
    }

    @Test
    public void test()
    {
        runner.execute(task);

        InOrder inOrder = inOrder(task);
        inOrder.verify(task).onAttached(runner);
        inOrder.verify(task).onPreExecute();
        inOrder.verify(task).doInBackground();
        inOrder.verify(task).onPostExecute();
    }
}
