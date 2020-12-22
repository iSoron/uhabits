/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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
package org.isoron.uhabits.activities.habits.show

import android.content.*
import android.view.*
import org.isoron.androidbase.activities.*
import org.isoron.androidbase.utils.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.habits.show.views.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*
import javax.inject.*

@ActivityScope
class ShowHabitRootView
@Inject constructor(
        @ActivityContext context: Context,
        private val habit: Habit,
        private val presenter: ShowHabitPresenter,
) : BaseRootView(context), ShowHabitPresenter.Listener {

    private var controller: Controller = object : Controller {}
    private var binding = ShowHabitBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        displayHomeAsUp = true

        binding.subtitleCard.habit = habit
        binding.notesCard.habit = habit
        binding.overviewCard.habit = habit
        binding.scoreCard.habit = habit
        binding.historyCard.habit = habit
        binding.streakCard.habit = habit
        binding.frequencyCard.habit = habit
        binding.barCard.habit = habit
        binding.targetCard.habit = habit

        initToolbar()
    }

    override fun getToolbarColor(): Int {
        val res = StyledResources(context)
        return if (!res.getBoolean(R.attr.useHabitColorAsPrimary)) super.getToolbarColor()
        else habit.color.toThemedAndroidColor(context)
    }

    fun setController(controller: Controller) {
        this.controller = controller
        binding.historyCard.setController(controller)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.addListener(this)
        presenter.requestData(this)
    }

    override fun onDetachedFromWindow() {
        presenter.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onData(data: ShowHabitViewModel) {
        binding.toolbar.title = data.title
        if (data.isNumerical) {
            binding.overviewCard.visibility = GONE
            binding.streakCard.visibility = GONE
        } else {
            binding.targetCard.visibility = GONE
        }
        controller.onToolbarChanged()
    }

    interface Controller : HistoryCard.Controller {
        fun onToolbarChanged() {}
    }

}