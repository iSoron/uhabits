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

package org.isoron.uhabits.core.models;

import org.isoron.uhabits.core.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.number.IsCloseTo.*;
import static org.isoron.uhabits.core.models.Score.*;


public class ScoreTest extends BaseUnitTest
{
    private static final double E = 1e-6;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Test
    public void test_compute_withDailyHabit()
    {
        int check = 1;
        double freq = 1.0;
        assertThat(compute(freq, 0, check), closeTo(0.051922, E));
        assertThat(compute(freq, 0.5, check), closeTo(0.525961, E));
        assertThat(compute(freq, 0.75, check), closeTo(0.762981, E));

        check = 0;
        assertThat(compute(freq, 0, check), closeTo(0, E));
        assertThat(compute(freq, 0.5, check), closeTo(0.474039, E));
        assertThat(compute(freq, 0.75, check), closeTo(0.711058, E));
    }

    @Test
    public void test_compute_withNonDailyHabit()
    {
        int check = 1;
        double freq = 1 / 3.0;
        assertThat(compute(freq, 0, check), closeTo(0.017616, E));
        assertThat(compute(freq, 0.5, check), closeTo(0.508808, E));
        assertThat(compute(freq, 0.75, check), closeTo(0.754404, E));

        check = 0;
        assertThat(compute(freq, 0, check), closeTo(0.0, E));
        assertThat(compute(freq, 0.5, check), closeTo(0.491192, E));
        assertThat(compute(freq, 0.75, check), closeTo(0.736788, E));
    }


    @Test
    public void testToString() throws Exception
    {
        Score score = new Score(Timestamp.ZERO.plus(100), 150.0);
        assertThat(score.toString(), equalTo( "{timestamp: 1970-04-11, value: 150.0}"));
    }
}
