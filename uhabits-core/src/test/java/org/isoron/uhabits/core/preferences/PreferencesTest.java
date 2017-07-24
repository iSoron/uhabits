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

package org.isoron.uhabits.core.preferences;

import android.support.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.junit.*;
import org.mockito.*;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class PreferencesTest extends BaseUnitTest
{
    @NonNull
    private Preferences prefs;

    @Mock
    private Preferences.Listener listener;

    private PropertiesStorage storage;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        File file = File.createTempFile("prefs", ".properties");
        file.deleteOnExit();
        storage = new PropertiesStorage(file);
        prefs = new Preferences(storage);
        prefs.addListener(listener);
    }

    @Test
    public void testHabitColor() throws Exception
    {
        assertThat(prefs.getDefaultHabitColor(999), equalTo(999));
        prefs.setDefaultHabitColor(10);
        assertThat(prefs.getDefaultHabitColor(999), equalTo(10));
    }

    @Test
    public void testDefaultOrder() throws Exception
    {
        assertThat(prefs.getDefaultOrder(), equalTo(HabitList.Order.BY_POSITION));

        prefs.setDefaultOrder(HabitList.Order.BY_SCORE);
        assertThat(prefs.getDefaultOrder(), equalTo(HabitList.Order.BY_SCORE));

        storage.putString("pref_default_order", "BOGUS");
        assertThat(prefs.getDefaultOrder(), equalTo(HabitList.Order.BY_POSITION));
        assertThat(storage.getString("pref_default_order", ""), equalTo("BY_POSITION"));
    }

    @Test
    public void testDefaultSpinnerPosition() throws Exception
    {
        assertThat(prefs.getDefaultScoreSpinnerPosition(), equalTo(1));

        prefs.setDefaultScoreSpinnerPosition(4);
        assertThat(prefs.getDefaultScoreSpinnerPosition(), equalTo(4));

        storage.putInt("pref_score_view_interval", 9000);
        assertThat(prefs.getDefaultScoreSpinnerPosition(), equalTo(1));
        assertThat(storage.getInt("pref_score_view_interval", 0), equalTo(1));
    }
}
