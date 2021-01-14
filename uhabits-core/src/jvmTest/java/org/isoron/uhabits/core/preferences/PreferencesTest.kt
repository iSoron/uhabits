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

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.io.File

class PreferencesTest : BaseUnitTest() {
    private lateinit var prefs: Preferences

    @Mock
    private val listener: Preferences.Listener? = null
    private var storage: PropertiesStorage? = null

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val file = File.createTempFile("prefs", ".properties")
        file.deleteOnExit()
        storage = PropertiesStorage(file)
        prefs = Preferences(storage!!)
        prefs.addListener(listener)
    }

    @Test
    @Throws(Exception::class)
    fun testClear() {
        prefs.setDefaultHabitColor(99)
        prefs.clear()
        assertThat(prefs.getDefaultHabitColor(0), IsEqual.equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun testHabitColor() {
        assertThat(prefs.getDefaultHabitColor(999), IsEqual.equalTo(999))
        prefs.setDefaultHabitColor(10)
        assertThat(prefs.getDefaultHabitColor(999), IsEqual.equalTo(10))
    }

    @Test
    @Throws(Exception::class)
    fun testDefaultOrder() {
        assertThat(
            prefs.defaultPrimaryOrder,
            IsEqual.equalTo(HabitList.Order.BY_POSITION)
        )
        prefs.defaultPrimaryOrder = HabitList.Order.BY_SCORE_DESC
        assertThat(
            prefs.defaultPrimaryOrder,
            IsEqual.equalTo(HabitList.Order.BY_SCORE_DESC)
        )
        storage!!.putString("pref_default_order", "BOGUS")
        assertThat(
            prefs.defaultPrimaryOrder,
            IsEqual.equalTo(HabitList.Order.BY_POSITION)
        )
        assertThat(
            storage!!.getString("pref_default_order", ""),
            IsEqual.equalTo("BY_POSITION")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testScoreCardSpinnerPosition() {
        assertThat(prefs.scoreCardSpinnerPosition, IsEqual.equalTo(1))
        prefs.scoreCardSpinnerPosition = 4
        assertThat(prefs.scoreCardSpinnerPosition, IsEqual.equalTo(4))
        storage!!.putInt("pref_score_view_interval", 9000)
        assertThat(prefs.scoreCardSpinnerPosition, IsEqual.equalTo(4))
    }

    @Test
    @Throws(Exception::class)
    fun testLastHint() {
        assertThat(prefs.lastHintNumber, IsEqual.equalTo(-1))
        assertNull(prefs.lastHintTimestamp)
        prefs.updateLastHint(34, Timestamp.ZERO.plus(100))
        assertThat(prefs.lastHintNumber, IsEqual.equalTo(34))
        assertThat(prefs.lastHintTimestamp, IsEqual.equalTo(Timestamp.ZERO.plus(100)))
    }

    @Test
    @Throws(Exception::class)
    fun testTheme() {
        assertThat(prefs.theme, IsEqual.equalTo(ThemeSwitcher.THEME_AUTOMATIC))
        prefs.theme = ThemeSwitcher.THEME_DARK
        assertThat(prefs.theme, IsEqual.equalTo(ThemeSwitcher.THEME_DARK))
        assertFalse(prefs.isPureBlackEnabled)
        prefs.isPureBlackEnabled = true
        assertTrue(prefs.isPureBlackEnabled)
    }

    @Test
    @Throws(Exception::class)
    fun testNotifications() {
        assertFalse(prefs.shouldMakeNotificationsSticky())
        prefs.setNotificationsSticky(true)
        assertTrue(prefs.shouldMakeNotificationsSticky())
        assertFalse(prefs.shouldMakeNotificationsLed())
        prefs.setNotificationsLed(true)
        assertTrue(prefs.shouldMakeNotificationsLed())
        assertThat(prefs.snoozeInterval, IsEqual.equalTo(15L))
        prefs.setSnoozeInterval(30)
        assertThat(prefs.snoozeInterval, IsEqual.equalTo(30L))
    }

    @Test
    @Throws(Exception::class)
    fun testAppVersionAndLaunch() {
        assertThat(prefs.lastAppVersion, IsEqual.equalTo(0))
        prefs.lastAppVersion = 23
        assertThat(prefs.lastAppVersion, IsEqual.equalTo(23))
        assertTrue(prefs.isFirstRun)
        prefs.isFirstRun = false
        assertFalse(prefs.isFirstRun)
        assertThat(prefs.launchCount, IsEqual.equalTo(0))
        prefs.incrementLaunchCount()
        assertThat(prefs.launchCount, IsEqual.equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun testCheckmarks() {
        assertFalse(prefs.isCheckmarkSequenceReversed)
        prefs.isCheckmarkSequenceReversed = true
        assertTrue(prefs.isCheckmarkSequenceReversed)
        assertFalse(prefs.isShortToggleEnabled)
        prefs.isShortToggleEnabled = true
        assertTrue(prefs.isShortToggleEnabled)
    }

    @Test
    @Throws(Exception::class)
    fun testDeveloper() {
        assertFalse(prefs.isDeveloper)
        prefs.isDeveloper = true
        assertTrue(prefs.isDeveloper)
    }

    @Test
    @Throws(Exception::class)
    fun testFiltering() {
        assertFalse(prefs.showArchived)
        assertTrue(prefs.showCompleted)
        prefs.showArchived = true
        prefs.showCompleted = false
        assertTrue(prefs.showArchived)
        assertFalse(prefs.showCompleted)
    }
}
