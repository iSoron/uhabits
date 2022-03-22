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

import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Timestamp.Companion.ZERO
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.junit.Before
import org.junit.Test
import java.io.File

class PreferencesTest : BaseUnitTest() {
    private lateinit var prefs: Preferences

    private var listener: Preferences.Listener = mock()
    private lateinit var storage: PropertiesStorage

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val file = File.createTempFile("prefs", ".properties")
        file.deleteOnExit()
        storage = PropertiesStorage(file)
        prefs = Preferences(storage)
        prefs.addListener(listener)
    }

    @Test
    @Throws(Exception::class)
    fun testClear() {
        prefs.setDefaultHabitColor(99)
        prefs.clear()
        assertThat(prefs.getDefaultHabitColor(0), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun testHabitColor() {
        assertThat(prefs.getDefaultHabitColor(999), equalTo(999))
        prefs.setDefaultHabitColor(10)
        assertThat(prefs.getDefaultHabitColor(999), equalTo(10))
    }

    @Test
    @Throws(Exception::class)
    fun testDefaultOrder() {
        assertThat(prefs.defaultPrimaryOrder, equalTo(HabitList.Order.BY_POSITION))
        prefs.defaultPrimaryOrder = HabitList.Order.BY_SCORE_DESC
        assertThat(prefs.defaultPrimaryOrder, equalTo(HabitList.Order.BY_SCORE_DESC))
        storage.putString("pref_default_order", "BOGUS")
        assertThat(prefs.defaultPrimaryOrder, equalTo(HabitList.Order.BY_POSITION))
        assertThat(
            storage.getString("pref_default_order", ""),
            equalTo("BY_POSITION")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testScoreCardSpinnerPosition() {
        assertThat(prefs.scoreCardSpinnerPosition, equalTo(1))
        prefs.scoreCardSpinnerPosition = 4
        assertThat(prefs.scoreCardSpinnerPosition, equalTo(4))
        storage.putInt("pref_score_view_interval", 9000)
        assertThat(prefs.scoreCardSpinnerPosition, equalTo(4))
    }

    @Test
    @Throws(Exception::class)
    fun testLastHint() {
        assertThat(prefs.lastHintNumber, equalTo(-1))
        assertNull(prefs.lastHintTimestamp)
        prefs.updateLastHint(34, ZERO.plus(100))
        assertThat(prefs.lastHintNumber, equalTo(34))
        assertThat(prefs.lastHintTimestamp, equalTo(ZERO.plus(100)))
    }

    @Test
    @Throws(Exception::class)
    fun testTheme() {
        assertThat(prefs.theme, equalTo(ThemeSwitcher.THEME_AUTOMATIC))
        prefs.theme = ThemeSwitcher.THEME_DARK
        assertThat(prefs.theme, equalTo(ThemeSwitcher.THEME_DARK))
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
    }

    @Test
    @Throws(Exception::class)
    fun testAppVersionAndLaunch() {
        assertThat(prefs.lastAppVersion, equalTo(0))
        prefs.lastAppVersion = 23
        assertThat(prefs.lastAppVersion, equalTo(23))
        assertTrue(prefs.isFirstRun)
        prefs.isFirstRun = false
        assertFalse(prefs.isFirstRun)
        assertThat(prefs.launchCount, equalTo(0))
        prefs.incrementLaunchCount()
        assertThat(prefs.launchCount, equalTo(1))
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

    @Test
    @Throws(Exception::class)
    fun testMidnightDelay() {
        assertFalse(prefs.isMidnightDelayEnabled)
        prefs.isMidnightDelayEnabled = true
        assertTrue(prefs.isMidnightDelayEnabled)
    }
}
