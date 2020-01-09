package org.isoron.uhabits.activities.habits.show.views

import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class NotesCardTest: BaseViewTest() {

    val PATH = "habits/show/NotesCard/"

    private lateinit var view: SubtitleCard

    private lateinit var habit: Habit

    @Before
    override fun setUp() {
        super.setUp()
        habit = fixtures.createLongHabit()
        habit.setReminder(Reminder(8, 30, WeekdayList.EVERY_DAY))
        view = LayoutInflater
                .from(targetContext)
                .inflate(R.layout.show_habit, null)
                .findViewById<View>(R.id.subtitleCard) as SubtitleCard
        view.apply {
            habit = habit
            refreshData()
            measureView(this, 800f, 200f)
        }
    }

    @Ignore("how do I generate these shots?")
    @Test
    @Throws(Exception::class)
    fun testRender() {
        assertRenders(view, SubtitleCardTest.PATH + "render.png")
    }
}