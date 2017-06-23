/*
 * Copyright (C) 2015-2017 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.database;

import android.database.sqlite.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.database.*;
import org.junit.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;


public class AndroidDatabaseTest extends BaseAndroidTest
{
    private AndroidDatabase db;

    @Override
    public void setUp()
    {
        super.setUp();
        db = new AndroidDatabase(SQLiteDatabase.create(null));
        db.execute("create table test(color int, name string)");
    }

    @Test
    public void testInsert() throws Exception
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "asd");
        map.put("color", null);
        db.insert("test", map);

        Cursor c = db.query("select * from test");
        c.moveToNext();
        assertNull(c.getInt(0));
        assertThat(c.getString(1), equalTo("asd"));
    }
}
