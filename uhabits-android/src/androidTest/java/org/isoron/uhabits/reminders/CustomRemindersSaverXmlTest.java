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

package org.isoron.uhabits.reminders;

import android.support.test.filters.*;
import android.support.test.runner.*;

import org.isoron.uhabits.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CustomRemindersSaverXmlTest extends BaseAndroidTest
{
    @Test
    public void testSaveLoad()
    {
        CustomRemindersSaverXml saver = new CustomRemindersSaverXml( targetContext );
        HashMap< Long, Long > map = new HashMap< Long, Long >();
        map.put( 1L, 1L );
        map.put( 2L, 3L );
        map.put( 3L, 1L );
        saver.save( map );
        Map< Long, Long > map2 = saver.load();
        assertEquals( map, map2 );
    }
}
