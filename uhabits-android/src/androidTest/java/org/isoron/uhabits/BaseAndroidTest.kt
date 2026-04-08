/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
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

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Looper
import androidx.annotation.StyleRes
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.models.HabitGroupList
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.computeToday
import org.isoron.platform.time.getToday
import org.isoron.platform.time.setToday
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.ModelFactory
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.inject.create
import org.isoron.uhabits.utils.DatabaseUtils.getDatabaseFile
import org.isoron.uhabits.utils.InterfaceUtils.setFixedResolution
import org.isoron.uhabits.utils.StyledResources.Companion.setFixedTheme
import org.isoron.uhabits.widgets.BaseWidgetProvider
import org.junit.Before
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.LinkedList
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.CountDownLatch

@MediumTest
abstract class BaseAndroidTest : TestCase() {
    @JvmField
    protected var testContext: Context = InstrumentationRegistry.getInstrumentation().context

    @JvmField
    protected var targetContext: Context =
        InstrumentationRegistry.getInstrumentation().targetContext
    protected lateinit var prefs: Preferences

    protected lateinit var habitList: HabitList
    protected lateinit var habitGroupList: HabitGroupList
    protected lateinit var taskRunner: TaskRunner
    protected lateinit var fixtures: HabitFixtures
    protected lateinit var groupFixtures: HabitGroupFixtures
    protected lateinit var latch: CountDownLatch
    protected lateinit var appComponent: HabitsApplicationTestComponent
    protected lateinit var modelFactory: ModelFactory
    protected lateinit var component: HabitsActivityTestComponent
    private lateinit var device: UiDevice

    @Before
    public override fun setUp() {
        if (Looper.myLooper() == null) Looper.prepare()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        setResolution(2.0f)
        setTheme(R.style.AppBaseTheme)
        setLocale("en", "US")
        latch = CountDownLatch(1)
        val context = targetContext.applicationContext
        val dbFile = getDatabaseFile(context)
        appComponent = HabitsApplicationTestComponent::class.create(
            appContext = context,
            dbFile = dbFile
        )
        HabitsApplication.component = appComponent
        prefs = appComponent.preferences
        habitList = appComponent.habitList
        habitGroupList = appComponent.habitGroupList
        taskRunner = appComponent.taskRunner
        setToday(computeToday(appComponent.preferences.midnightDelayHours, 0))
        modelFactory = appComponent.modelFactory
        prefs.clear()
        fixtures = HabitFixtures(modelFactory, habitList)
        fixtures.purgeHabits(habitList)
        groupFixtures = HabitGroupFixtures(modelFactory, habitList, habitGroupList)
        groupFixtures.purgeHabitGroups(habitGroupList)
        component = HabitsActivityTestComponent::class.create(
            parent = appComponent,
            activityContext = targetContext
        )
    }

    protected fun assertWidgetProviderIsInstalled(componentClass: Class<out BaseWidgetProvider?>?) {
        val provider = ComponentName(targetContext, componentClass!!)
        val manager = AppWidgetManager.getInstance(targetContext)
        val installedProviders: MutableList<ComponentName> = LinkedList()
        for (info in manager.installedProviders) installedProviders.add(info.provider)
        assertThat<List<ComponentName>>(
            installedProviders,
            hasItems(provider)
        )
    }

    protected fun setLocale(language: String, country: String) {
        val locale = Locale(language, country)
        Locale.setDefault(locale)
        val res = targetContext.resources
        val config = res.configuration
        config.setLocale(locale)
    }

    protected fun setResolution(r: Float) {
        val dm = targetContext.resources.displayMetrics
        dm.density = r
        dm.scaledDensity = r
        setFixedResolution(r)
    }

    protected fun setTheme(@StyleRes themeId: Int) {
        targetContext.setTheme(themeId)
        setFixedTheme(themeId)
    }

    protected fun sleep(time: Int) {
        try {
            Thread.sleep(time.toLong())
        } catch (e: InterruptedException) {
            fail()
        }
    }

    protected fun day(offset: Int): LocalDate {
        return getToday().minus(offset)
    }

    @Throws(Exception::class)
    fun setSystemTime(
        tz: String?,
        year: Int,
        javaMonth: Int,
        day: Int,
        hourOfDay: Int,
        minute: Int
    ) {
        val cal = GregorianCalendar(year, javaMonth, day, hourOfDay, minute, 0)
        cal.timeZone = TimeZone.getTimeZone(tz)
        setSystemTime(cal)
    }

    @Throws(Exception::class)
    private fun setSystemTime(cal: GregorianCalendar) {
        val tz = cal.timeZone.toZoneId()

        // Set time zone (API < 28)
        var command = String.format(Locale.US, "service call alarm 3 s16 %s", tz)
        device.executeShellCommand(command)

        // Set time zone (API >= 28)
        device.executeShellCommand("cmd alarm set-timezone $tz")

        // Set time zone (permanent)
        command = String.format(Locale.US, "setprop persist.sys.timezone %s", tz)
        device.executeShellCommand(command)

        // Set time
        val date = String.format(
            Locale.US,
            "%02d%02d%02d%02d%04d.%02d",
            cal[Calendar.MONTH] + 1,
            cal[Calendar.DAY_OF_MONTH],
            cal[Calendar.HOUR_OF_DAY],
            cal[Calendar.MINUTE],
            cal[Calendar.YEAR],
            cal[Calendar.SECOND]
        )

        // Set time (method 1)
        // Run twice to override daylight saving time
        device.executeShellCommand("date $date")
        device.executeShellCommand("date $date")

        // Set time (method 2)
        // Run in addition to the method above because one of these mail fail, depending
        // on the Android API version.
        command = String.format(Locale.US, "date -u @%d", cal.timeInMillis / 1000)
        device.executeShellCommand(command)

        // Set time (method 3 - API >= 28)
        device.executeShellCommand("cmd alarm set-time ${cal.timeInMillis}")

        // Wait for system events to settle
        Thread.sleep(2001L)
    }

    private lateinit var savedCalendar: GregorianCalendar
    fun saveSystemTime() {
        savedCalendar = GregorianCalendar()
    }

    @Throws(Exception::class)
    fun restoreSystemTime() {
        setSystemTime(savedCalendar)
    }

    companion object
}
