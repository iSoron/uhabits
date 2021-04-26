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
package org.isoron.uhabits

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.HabitCardListCache
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.isoron.uhabits.inject.HabitsApplicationComponent
import org.junit.After
import org.junit.Before

open class BaseUserInterfaceTest {
    private lateinit var component: HabitsApplicationComponent
    private lateinit var habitList: HabitList
    private lateinit var prefs: Preferences
    private lateinit var fixtures: HabitFixtures
    private lateinit var cache: HabitCardListCache
    @Before
    @Throws(Exception::class)
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val app =
            ApplicationProvider.getApplicationContext<Context>().applicationContext as HabitsApplication
        component = app.component
        habitList = component.habitList
        prefs = component.preferences
        cache = component.habitCardListCache
        fixtures = HabitFixtures(component.modelFactory, habitList)
        resetState()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        device.pressBack()
        device.pressBack()
    }

    @Throws(Exception::class)
    private fun resetState() {
        prefs.clear()
        prefs.isFirstRun = false
        prefs.updateLastHint(100, getToday())
        habitList.removeAll()
        cache.refreshAllHabits()
        Thread.sleep(1000)
        val h1 = fixtures.createEmptyHabit()
        h1.name = "Wake up early"
        h1.question = "Did you wake up early today?"
        h1.description = "test description 1"
        h1.color = PaletteColor(5)
        habitList.update(h1)
        val h2 = fixtures.createShortHabit()
        h2.name = "Track time"
        h2.question = "Did you track your time?"
        h2.description = "test description 2"
        h2.color = PaletteColor(5)
        habitList.update(h2)
        val h3 = fixtures.createLongHabit()
        h3.name = "Meditate"
        h3.question = "Did meditate today?"
        h3.description = "test description 3"
        h3.color = PaletteColor(10)
        habitList.update(h3)
        val h4 = fixtures.createEmptyHabit()
        h4.name = EMPTY_DESCRIPTION_HABIT_NAME
        h4.question = "Did you read books today?"
        h4.description = ""
        h4.color = PaletteColor(2)
        habitList.update(h4)
    }

    @Throws(Exception::class)
    protected fun rotateDevice() {
        device.setOrientationLeft()
        device.setOrientationNatural()
    }

    companion object {
        private const val PKG = "org.isoron.uhabits"
        const val EMPTY_DESCRIPTION_HABIT_NAME = "Read books"
        lateinit var device: UiDevice
        fun startActivity(cls: Class<*>) {
            val intent = Intent()
            intent.component = ComponentName(PKG, cls.canonicalName!!)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ApplicationProvider.getApplicationContext<Context>().startActivity(intent)
        }
    }
}
