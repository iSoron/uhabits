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

import androidx.annotation.*;

import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.ui.*;
import org.junit.*;
import org.mockito.*;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void testClear() throws Exception
    {
        prefs.setDefaultHabitColor(99);
        prefs.clear();
        assertThat(prefs.getDefaultHabitColor(0), equalTo(0));
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

    @Test
    public void testLastHint() throws Exception
    {
        assertThat(prefs.getLastHintNumber(), equalTo(-1));
        assertNull(prefs.getLastHintTimestamp());

        prefs.updateLastHint(34, Timestamp.ZERO.plus(100));
        assertThat(prefs.getLastHintNumber(), equalTo(34));
        assertThat(prefs.getLastHintTimestamp(), equalTo(Timestamp.ZERO.plus(100)));
    }

    @Test
    public void testSync() throws Exception
    {
        assertThat(prefs.getLastSync(), equalTo(0L));
        prefs.setLastSync(100);
        assertThat(prefs.getLastSync(), equalTo(100L));

        assertThat(prefs.getSyncAddress(),
            equalTo(Preferences.DEFAULT_SYNC_SERVER));
        prefs.setSyncAddress("example");
        assertThat(prefs.getSyncAddress(), equalTo("example"));
        verify(listener).onSyncFeatureChanged();
        reset(listener);

        assertThat(prefs.getSyncKey(), equalTo(""));
        prefs.setSyncKey("123");
        assertThat(prefs.getSyncKey(), equalTo("123"));
        verify(listener).onSyncFeatureChanged();
        reset(listener);

        assertFalse(prefs.isSyncEnabled());
        prefs.setSyncEnabled(true);
        assertTrue(prefs.isSyncEnabled());
        verify(listener).onSyncFeatureChanged();
        reset(listener);

        String id = prefs.getSyncClientId();
        assertFalse(id.isEmpty());
        assertThat(prefs.getSyncClientId(), equalTo(id));
    }

    @Test
    public void testTheme() throws Exception
    {
        assertThat(prefs.getTheme(), equalTo(ThemeSwitcher.THEME_AUTOMATIC));
        prefs.setTheme(ThemeSwitcher.THEME_DARK);
        assertThat(prefs.getTheme(), equalTo(ThemeSwitcher.THEME_DARK));

        assertFalse(prefs.isPureBlackEnabled());
        prefs.setPureBlackEnabled(true);
        assertTrue(prefs.isPureBlackEnabled());
    }

    @Test
    public void testNotifications() throws Exception
    {
        assertFalse(prefs.shouldMakeNotificationsSticky());
        prefs.setNotificationsSticky(true);
        assertTrue(prefs.shouldMakeNotificationsSticky());

        assertFalse(prefs.shouldMakeNotificationsLed());
        prefs.setNotificationsLed(true);
        assertTrue(prefs.shouldMakeNotificationsLed());

        assertThat(prefs.getSnoozeInterval(), equalTo(15L));
        prefs.setSnoozeInterval(30);
        assertThat(prefs.getSnoozeInterval(), equalTo(30L));
    }

    @Test
    public void testAppVersionAndLaunch() throws Exception
    {
        assertThat(prefs.getLastAppVersion(), equalTo(0));
        prefs.setLastAppVersion(23);
        assertThat(prefs.getLastAppVersion(), equalTo(23));

        assertTrue(prefs.isFirstRun());
        prefs.setFirstRun(false);
        assertFalse(prefs.isFirstRun());

        assertThat(prefs.getLaunchCount(), equalTo(0));
        prefs.incrementLaunchCount();
        assertThat(prefs.getLaunchCount(), equalTo(1));
    }

    @Test
    public void testCheckmarks() throws Exception
    {
        assertFalse(prefs.isCheckmarkSequenceReversed());
        prefs.setCheckmarkSequenceReversed(true);
        assertTrue(prefs.isCheckmarkSequenceReversed());

        assertFalse(prefs.isShortToggleEnabled());
        prefs.setShortToggleEnabled(true);
        assertTrue(prefs.isShortToggleEnabled());
    }

    @Test
    public void testDeveloper() throws Exception
    {
        assertFalse(prefs.isDeveloper());
        prefs.setDeveloper(true);
        assertTrue(prefs.isDeveloper());
    }

    @Test
    public void testFiltering() throws Exception
    {
        assertFalse(prefs.getShowArchived());
        assertTrue(prefs.getShowCompleted());

        prefs.setShowArchived(true);
        prefs.setShowCompleted(false);

        assertTrue(prefs.getShowArchived());
        assertFalse(prefs.getShowCompleted());
    }
}
