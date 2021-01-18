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
package org.isoron.uhabits.core.preferences

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Arrays

class PropertiesStorageTest : BaseUnitTest() {
    private lateinit var storage: PropertiesStorage
    private lateinit var file: File

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        file = File.createTempFile("test", ".properties")
        file.deleteOnExit()
        storage = PropertiesStorage(file)
    }

    @Test
    @Throws(Exception::class)
    fun testPutGetRemove() {
        storage.putBoolean("booleanKey", true)
        assertTrue(storage.getBoolean("booleanKey", false))
        assertFalse(storage.getBoolean("random", false))
        storage.putInt("intKey", 64)
        assertThat(storage.getInt("intKey", 200), equalTo(64))
        assertThat(storage.getInt("random", 200), equalTo(200))
        storage.putLong("longKey", 64L)
        assertThat(storage.getLong("intKey", 200L), equalTo(64L))
        assertThat(storage.getLong("random", 200L), equalTo(200L))
        storage.putString("stringKey", "Hello")
        assertThat(storage.getString("stringKey", ""), equalTo("Hello"))
        assertThat(storage.getString("random", ""), equalTo(""))
        storage.remove("stringKey")
        assertThat(storage.getString("stringKey", ""), equalTo(""))
        storage.clear()
        assertThat(storage.getLong("intKey", 200L), equalTo(200L))
        assertFalse(storage.getBoolean("booleanKey", false))
    }

    @Test
    @Throws(Exception::class)
    fun testPersistence() {
        storage.putBoolean("booleanKey", true)
        storage.putInt("intKey", 64)
        storage.putLong("longKey", 64L)
        storage.putString("stringKey", "Hello")
        val storage2 = PropertiesStorage(file)
        assertTrue(storage2.getBoolean("booleanKey", false))
        assertThat(storage2.getInt("intKey", 200), equalTo(64))
        assertThat(storage2.getLong("intKey", 200L), equalTo(64L))
        assertThat(storage2.getString("stringKey", ""), equalTo("Hello"))
    }

    @Test
    @Throws(Exception::class)
    fun testLongArray() {
        val expected1 = longArrayOf(1L, 2L, 3L, 5L)
        val expected2 = longArrayOf(1L)
        val expected3 = longArrayOf()
        val expected4 = longArrayOf()
        storage.putLongArray("key1", expected1)
        storage.putLongArray("key2", expected2)
        storage.putLongArray("key3", expected3)
        val actual1 = storage.getLongArray("key1", longArrayOf())
        val actual2 = storage.getLongArray("key2", longArrayOf())
        val actual3 = storage.getLongArray("key3", longArrayOf())
        val actual4 = storage.getLongArray("invalidKey", longArrayOf())
        assertTrue(Arrays.equals(actual1, expected1))
        assertTrue(Arrays.equals(actual2, expected2))
        assertTrue(Arrays.equals(actual3, expected3))
        assertTrue(Arrays.equals(actual4, expected4))
        assertEquals("1,2,3,5", storage.getString("key1", ""))
        assertEquals(1, storage.getLong("key2", -1))
    }
}
