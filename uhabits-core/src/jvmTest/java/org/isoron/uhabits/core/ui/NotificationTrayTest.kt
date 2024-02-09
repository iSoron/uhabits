package org.isoron.uhabits.core.ui

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.Timestamp
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.preferences.Preferences.Storage
import org.junit.Before
import org.junit.Test

class NotificationTrayTest : BaseUnitTest() {
    private val systemTray = object : NotificationTray.SystemTray {
        override fun removeNotification(notificationId: Int) {}

        override fun showNotification(
            habit: Habit,
            notificationId: Int,
            timestamp: Timestamp,
            reminderTime: Long,
            silent: Boolean
        ) {
        }

        override fun log(msg: String) {}
    }

    private var preferences = MockPreferences()
    private lateinit var notificationTray: NotificationTray

    class DummyStorage : Storage {
        override fun clear() {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun getInt(key: String, defValue: Int): Int {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun getLong(key: String, defValue: Long): Long {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun getString(key: String, defValue: String): String {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun onAttached(preferences: Preferences) {
        }

        override fun putBoolean(key: String, value: Boolean) {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun putInt(key: String, value: Int) {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun putLong(key: String, value: Long) {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun putString(key: String, value: String) {
            throw NotImplementedError("Mock implementation missing")
        }

        override fun remove(key: String) {
            throw NotImplementedError("Mock implementation missing")
        }
    }

    class MockPreferences : Preferences(DummyStorage()) {
        private var activeNotifications: HashMap<Habit, NotificationTray.NotificationData> =
            HashMap()

        override fun setActiveNotifications(activeNotifications: Map<Habit, NotificationTray.NotificationData>) {
            this.activeNotifications = HashMap(activeNotifications)
        }

        override fun getActiveNotifications(habitList: HabitList): HashMap<Habit, NotificationTray.NotificationData> {
            return activeNotifications
        }
    }

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        notificationTray =
            NotificationTray(taskRunner, commandRunner, preferences, systemTray, habitList)
    }

    @Test
    @Throws(Exception::class)
    fun testShow() {
        // Show a reminder for a habit
        val habit = fixtures.createEmptyHabit()
        habit.reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        val timestamp = Timestamp(System.currentTimeMillis())
        val reminderTime = System.currentTimeMillis()
        notificationTray.show(habit, timestamp, reminderTime)

        // Verify that the active notifications include exactly the one shown reminder
        // TODO are we guaranteed that task has executed?
        assertThat(preferences.getActiveNotifications(habitList).size, equalTo(1))
        assertThat(
            preferences.getActiveNotifications(habitList)[habit],
            equalTo(NotificationTray.NotificationData(timestamp, reminderTime))
        )

        // Remove the reminder from the notification tray and verify that active notifications are empty
        notificationTray.cancel(habit)
        assertThat(preferences.getActiveNotifications(habitList).size, equalTo(0))

        // TODO test cases where reminders should be removed (e.g. reshowAll)
    }
}
