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
package org.isoron.uhabits.core.tasks

import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SingleThreadTaskRunnerTest : BaseUnitTest() {
    private lateinit var runner: SingleThreadTaskRunner
    private var task: Task = mock()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        runner = SingleThreadTaskRunner()
    }

    @Test
    fun test() {
        runner.execute(task)
        val inOrder = inOrder(task)
        inOrder.verify(task).onAttached(runner)
        inOrder.verify(task).onPreExecute()
        inOrder.verify(task).doInBackground()
        inOrder.verify(task).onPostExecute()
    }
}
