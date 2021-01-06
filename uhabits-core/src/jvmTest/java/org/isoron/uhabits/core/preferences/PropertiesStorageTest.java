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

package org.isoron.uhabits.core.preferences;

import org.isoron.uhabits.core.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

public class PropertiesStorageTest extends BaseUnitTest
{
    private PropertiesStorage storage;

    private File file;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        file = File.createTempFile("test", ".properties");
        file.deleteOnExit();

        storage = new PropertiesStorage(file);
    }

    @Test
    public void testPutGetRemove() throws Exception
    {
        storage.putBoolean("booleanKey", true);
        assertTrue(storage.getBoolean("booleanKey", false));
        assertFalse(storage.getBoolean("random", false));

        storage.putInt("intKey", 64);
        assertThat(storage.getInt("intKey", 200), equalTo(64));
        assertThat(storage.getInt("random", 200), equalTo(200));

        storage.putLong("longKey", 64L);
        assertThat(storage.getLong("intKey", 200L), equalTo(64L));
        assertThat(storage.getLong("random", 200L), equalTo(200L));

        storage.putString("stringKey", "Hello");
        assertThat(storage.getString("stringKey", ""), equalTo("Hello"));
        assertThat(storage.getString("random", ""), equalTo(""));

        storage.remove("stringKey");
        assertThat(storage.getString("stringKey", ""), equalTo(""));

        storage.clear();
        assertThat(storage.getLong("intKey", 200L), equalTo(200L));
        assertFalse(storage.getBoolean("booleanKey", false));
    }

    @Test
    public void testPersistence() throws Exception
    {
        storage.putBoolean("booleanKey", true);
        storage.putInt("intKey", 64);
        storage.putLong("longKey", 64L);
        storage.putString("stringKey", "Hello");

        PropertiesStorage storage2 = new PropertiesStorage(file);
        assertTrue(storage2.getBoolean("booleanKey", false));
        assertThat(storage2.getInt("intKey", 200), equalTo(64));
        assertThat(storage2.getLong("intKey", 200L), equalTo(64L));
        assertThat(storage2.getString("stringKey", ""), equalTo("Hello"));
    }

    @Test
    public void testLongArray() throws Exception
    {
        long[] expected1 = new long[]{1L, 2L, 3L, 5L};
        long[] expected2 = new long[]{1L};
        long[] expected3 = new long[]{};
        long[] expected4 = new long[]{};

        storage.putLongArray("key1", expected1);
        storage.putLongArray("key2", expected2);
        storage.putLongArray("key3", expected3);

        long[] actual1 = storage.getLongArray("key1", new long[]{});
        long[] actual2 = storage.getLongArray("key2", new long[]{});
        long[] actual3 = storage.getLongArray("key3", new long[]{});
        long[] actual4 = storage.getLongArray("invalidKey", new long[]{});

        assertTrue(Arrays.equals(actual1, expected1));
        assertTrue(Arrays.equals(actual2, expected2));
        assertTrue(Arrays.equals(actual3, expected3));
        assertTrue(Arrays.equals(actual4, expected4));

        assertEquals("1,2,3,5", storage.getString("key1", ""));
        assertEquals(1, storage.getLong("key2", -1));
    }
}
