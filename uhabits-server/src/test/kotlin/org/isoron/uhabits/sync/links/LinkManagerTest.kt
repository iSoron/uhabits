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

package org.isoron.uhabits.sync.links

import org.isoron.uhabits.sync.*
import org.junit.Test
import kotlin.test.*

class LinkManagerTest {

    @Test
    fun testUsage() {
        val manager = LinkManager(timeoutInMillis = 250)
        val originalLink = manager.register(syncKey = "SECRET")
        val retrievedLink = manager.get(originalLink.id)
        assertEquals(originalLink, retrievedLink)

        Thread.sleep(260) // wait until expiration
        assertFailsWith<KeyNotFoundException> {
            manager.get(originalLink.id)
        }

        assertFailsWith<KeyNotFoundException> {
            manager.get("INVALID")
        }
    }
}