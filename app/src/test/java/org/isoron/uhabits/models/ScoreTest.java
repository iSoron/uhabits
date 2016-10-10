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

package org.isoron.uhabits.models;

import org.isoron.uhabits.BaseUnitTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ScoreTest extends BaseUnitTest
{
    @Override
    @Before
    public void setUp()
    {
        super.setUp();
    }

    @Test
    public void test_compute_withDailyHabit()
    {
        int checkmark = Checkmark.UNCHECKED;
        assertThat(Score.compute(1, 0, checkmark), equalTo(0));
        assertThat(Score.compute(1, 5000000, checkmark), equalTo(4740387));
        assertThat(Score.compute(1, 10000000, checkmark), equalTo(9480775));
        assertThat(Score.compute(1, Score.MAX_VALUE, checkmark),
            equalTo(18259478));

        checkmark = Checkmark.CHECKED_IMPLICITLY;
        assertThat(Score.compute(1, 0, checkmark), equalTo(0));
        assertThat(Score.compute(1, 5000000, checkmark), equalTo(4740387));
        assertThat(Score.compute(1, 10000000, checkmark), equalTo(9480775));
        assertThat(Score.compute(1, Score.MAX_VALUE, checkmark),
            equalTo(18259478));

        checkmark = Checkmark.CHECKED_EXPLICITLY;
        assertThat(Score.compute(1, 0, checkmark), equalTo(1000000));
        assertThat(Score.compute(1, 5000000, checkmark), equalTo(5740387));
        assertThat(Score.compute(1, 10000000, checkmark), equalTo(10480775));
        assertThat(Score.compute(1, Score.MAX_VALUE, checkmark),
            equalTo(Score.MAX_VALUE));
    }

    @Test
    public void test_compute_withNonDailyHabit()
    {
        int checkmark = Checkmark.CHECKED_EXPLICITLY;
        assertThat(Score.compute(1 / 3.0, 0, checkmark), equalTo(1000000));
        assertThat(Score.compute(1 / 3.0, 5000000, checkmark),
            equalTo(5916180));
        assertThat(Score.compute(1 / 3.0, 10000000, checkmark),
            equalTo(10832360));
        assertThat(Score.compute(1 / 3.0, Score.MAX_VALUE, checkmark),
            equalTo(Score.MAX_VALUE));

        assertThat(Score.compute(1 / 7.0, 0, checkmark), equalTo(1000000));
        assertThat(Score.compute(1 / 7.0, 5000000, checkmark),
            equalTo(5964398));
        assertThat(Score.compute(1 / 7.0, 10000000, checkmark),
            equalTo(10928796));
        assertThat(Score.compute(1 / 7.0, Score.MAX_VALUE, checkmark),
            equalTo(Score.MAX_VALUE));
    }
}
