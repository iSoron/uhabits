package org.isoron.uhabits.core.models

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.preferences.PropertiesStorage
import org.isoron.uhabits.core.ui.screens.habits.show.views.TargetCardPresenter
import org.isoron.uhabits.core.ui.views.LightTheme
import org.isoron.uhabits.core.utils.DateUtils.Companion.getToday
import org.junit.Test
import java.io.File

class SkipDaysTest : BaseUnitTest() {
    private lateinit var preferences: Preferences
    private lateinit var storage: PropertiesStorage
    private lateinit var today: Timestamp
    private lateinit var monday: Timestamp

//    private val themeSwitcher: ThemeSwitcher = mock()
    private val theme = LightTheme()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val file = File.createTempFile("prefs", ".properties")
        file.deleteOnExit()
        storage = PropertiesStorage(file)
        preferences = Preferences(storage)
        preferences.isSkipEnabled = true
        today = getToday()
        monday = today.plus(2 - today.weekday)
    }

    @Test
    @Throws(Exception::class)
    fun streakWithSkip() {
        val h = modelFactory.buildHabit()
        h.skipDays = SkipDays(true, WeekdayList.WEEKENDS)
        h.originalEntries.add(Entry(monday, Entry.YES_MANUAL))
        h.originalEntries.add(Entry(monday.minus(3), Entry.YES_MANUAL))
        h.recompute()
        assertThat(h.streaks.getBest(1)[0].length, equalTo(4))
    }

    @Test
    @Throws(Exception::class)
    fun ignoreSkipDays() {
        val h = modelFactory.buildHabit()
        h.skipDays = SkipDays(true, WeekdayList.WEEKENDS)
        h.originalEntries.add(Entry(monday.minus(1), Entry.YES_MANUAL))
        h.recompute()
        assertThat(h.computedEntries.getKnown().size, equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun targetCardWithSkips() {
        val h = Habit(
            skipDays = SkipDays(true, WeekdayList(booleanArrayOf(false, false, false, true, true, false, false))),
            type = HabitType.NUMERICAL,
            frequency = Frequency.WEEKLY,
            targetValue = 40.0,
            computedEntries = modelFactory.buildComputedEntries(),
            originalEntries = modelFactory.buildOriginalEntries(),
            streaks = modelFactory.buildStreakList(),
            scores = modelFactory.buildScoreList()
        )
        h.recompute()
        var target = TargetCardPresenter.buildState(
            habit = h,
            firstWeekday = preferences.firstWeekdayInt,
            theme = theme
        )
        assertThat(target.targets, equalTo(arrayListOf(40.0, 160.0, 520.0, 2080.0)))
        h.originalEntries.add(Entry(today, Entry.SKIP))
        h.recompute()
        target = TargetCardPresenter.buildState(
            habit = h,
            firstWeekday = preferences.firstWeekdayInt,
            theme = theme
        )
        assertThat(target.targets, equalTo(arrayListOf(32.0, 152.0, 512.0, 2072.0)))
    }
}
