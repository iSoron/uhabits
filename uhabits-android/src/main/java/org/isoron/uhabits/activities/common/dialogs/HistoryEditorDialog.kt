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
package org.isoron.uhabits.activities.common.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import org.isoron.platform.gui.AndroidDataView
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.show.views.HistoryCardPresenter
import org.isoron.uhabits.core.ui.views.HistoryChart
import org.isoron.uhabits.core.ui.views.LightTheme
import org.isoron.uhabits.core.ui.views.OnDateClickedListener
import org.isoron.uhabits.core.utils.DateUtils
import java.util.Locale
import kotlin.math.min

class HistoryEditorDialog : AppCompatDialogFragment(), CommandRunner.Listener {

    private lateinit var commandRunner: CommandRunner
    private lateinit var habit: Habit
    private lateinit var preferences: Preferences
    lateinit var dataView: AndroidDataView

    private var chart: HistoryChart? = null
    private var onDateClickedListener: OnDateClickedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val component = (activity!!.application as HabitsApplication).component
        commandRunner = component.commandRunner
        habit = component.habitList.getById(arguments!!.getLong("habit"))!!
        preferences = component.preferences

        val themeSwitcher = AndroidThemeSwitcher(activity!!, preferences)
        themeSwitcher.apply()

        chart = HistoryChart(
            dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
            firstWeekday = preferences.firstWeekday,
            paletteColor = habit.color,
            series = emptyList(),
            defaultSquare = HistoryChart.Square.OFF,
            notesIndicators = emptyList(),
            theme = themeSwitcher.currentTheme,
            today = DateUtils.getTodayWithOffset().toLocalDate(),
            onDateClickedListener = onDateClickedListener ?: object : OnDateClickedListener {},
            padding = 10.0,
        )
        dataView = AndroidDataView(context!!, null)
        dataView.view = chart!!

        return Dialog(context!!).apply {
            val metrics = resources.displayMetrics
            val maxHeight = resources.getDimensionPixelSize(R.dimen.history_editor_max_height)
            setContentView(dataView)
            window!!.setLayout(metrics.widthPixels, min(metrics.heightPixels, maxHeight))
        }
    }

    override fun onResume() {
        super.onResume()
        commandRunner.addListener(this)
        refreshData()
    }

    override fun onPause() {
        commandRunner.removeListener(this)
        super.onPause()
    }

    fun setOnDateClickedListener(listener: OnDateClickedListener) {
        onDateClickedListener = listener
        chart?.onDateClickedListener = listener
    }

    private fun refreshData() {
        val model = HistoryCardPresenter.buildState(
            habit,
            preferences.firstWeekday,
            theme = LightTheme()
        )
        chart?.series = model.series
        chart?.defaultSquare = model.defaultSquare
        chart?.notesIndicators = model.notesIndicators
        dataView.postInvalidate()
    }

    override fun onCommandFinished(command: Command) {
        refreshData()
    }
}
