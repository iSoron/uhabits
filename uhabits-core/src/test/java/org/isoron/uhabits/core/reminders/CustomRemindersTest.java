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

package org.isoron.uhabits.core.reminders;

import org.hamcrest.*;
import org.isoron.uhabits.core.*;
import org.junit.*;

import static org.hamcrest.Matchers.*;

public class CustomRemindersTest extends BaseUnitTest
{
    @Test
    public void ModifyTest()
    {
        CustomReminders reminders = new CustomReminders();
        MatcherAssert.assertThat( reminders.get( 1L ), equalTo( null ));
        reminders.set( 1L, 1L );
        MatcherAssert.assertThat( reminders.get( 1L ), equalTo( 1L ));
        reminders.set( 2L, 2L );
        MatcherAssert.assertThat( reminders.get( 2L ), equalTo( 2L ));
        reminders.set( 1L, 3L );
        MatcherAssert.assertThat( reminders.get( 1L ), equalTo( 3L ));
        MatcherAssert.assertThat( reminders.get( 2L ), equalTo( 2L ));
        reminders.remove( 1L );
        MatcherAssert.assertThat( reminders.get( 1L ), equalTo( null ));
    }
}
