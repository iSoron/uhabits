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
package org.isoron.uhabits.activities.common.views

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardPresenter.Companion.buildState
import org.isoron.uhabits.core.ui.views.LightTheme
import org.isoron.uhabits.utils.toFixedAndroidColor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ScoreChartTest : BaseViewTest() {
    private lateinit var habit: Habit
    private lateinit var view: ScoreChart

    @Before
    override fun setUp() {
        super.setUp()
        fixtures.purgeHabits(habitList)
        habit = fixtures.createLongHabit()
        val state = buildState(
            habit = habit,
            firstWeekday = prefs.firstWeekdayInt,
            spinnerPosition = 0,
            theme = LightTheme(),
        )
        view = ScoreChart(targetContext).apply {
            setScores(state.scores)
            setColor(state.color.toFixedAndroidColor())
            setBucketSize(state.bucketSize)
        }
        measureView(view, dpToPixels(300), dpToPixels(200))
    }

    @Test
    @Throws(Throwable::class)
    fun testRender() {
        assertRenders(view, BASE_PATH + "render.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withDataOffset() {
        view.onScroll(null, null, -dpToPixels(150), 0f)
        view.invalidate()
        assertRenders(view, BASE_PATH + "renderDataOffset.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withDifferentSize() {
        measureView(view, dpToPixels(200), dpToPixels(200))
        assertRenders(view, BASE_PATH + "renderDifferentSize.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withMonthlyBucket() {
        val (scores, bucketSize) = buildState(habit, prefs.firstWeekdayInt, 2, LightTheme())
        view.setScores(scores)
        view.setBucketSize(bucketSize)
        view.invalidate()
        assertRenders(view, BASE_PATH + "renderMonthly.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withTransparentBackground() {
        view.setIsTransparencyEnabled(true)
        assertRenders(view, BASE_PATH + "renderTransparent.png")
    }

    @Test
    @Throws(Throwable::class)
    fun testRender_withYearlyBucket() {
        val state = buildState(habit, prefs.firstWeekdayInt, 4, LightTheme())
        view.setScores(state.scores)
        view.setBucketSize(state.bucketSize)
        view.invalidate()
        assertRenders(view, BASE_PATH + "renderYearly.png")
    }

    companion object {
        private const val BASE_PATH = "common/ScoreChart/"
    }
}
