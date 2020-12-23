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
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import kotlinx.coroutines.*
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.habits.show.views.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.databinding.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.*

class ShowHabitActivity : AppCompatActivity(), CommandRunner.Listener {
    private lateinit var commandRunner: CommandRunner
    private lateinit var preferences: Preferences
    private lateinit var presenter: ShowHabitPresenter
    private lateinit var view: ShowHabitView
    private lateinit var widgetUpdater: WidgetUpdater

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appComponent = (applicationContext as HabitsApplication).component
        val habitList = appComponent.habitList
        val habit = habitList.getById(ContentUris.parseId(intent.data!!))!!
        preferences = appComponent.preferences
        commandRunner = appComponent.commandRunner
        widgetUpdater = appComponent.widgetUpdater

        view = ShowHabitView(this)
        presenter = ShowHabitPresenter(
                habit = habit,
                context = this,
                preferences = appComponent.preferences
        )

        view.onBucketSizeSelected = { position ->
            preferences.defaultScoreSpinnerPosition = position
            widgetUpdater.updateWidgets(habit.id)
            updateViews()
        }

        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        commandRunner.addListener(this)
        updateViews()
    }

    override fun onPause() {
        commandRunner.removeListener(this)
        super.onPause()
    }

    override fun onCommandExecuted(command: Command?, refreshKey: Long?) {
        updateViews()
    }

    private fun updateViews() {
        scope.launch {
            view.update(presenter.present())
        }
    }
}

data class ShowHabitViewModel(
        val title: String = "",
        val isNumerical: Boolean = false,
        val color: PaletteColor = PaletteColor(1),
        val subtitle: SubtitleCardViewModel,
        val overview: OverviewCardViewModel,
        val notes: NotesCardViewModel,
        val target: TargetCardViewModel,
        val streaks: StreakCardViewModel,
        val scores: ScoreCardViewModel,
        val frequency: FrequencyCardViewModel,
)

class ShowHabitView(context: Context) : FrameLayout(context) {
    private val binding = ShowHabitBinding.inflate(LayoutInflater.from(context))

    var onBucketSizeSelected: (position: Int) -> Unit = {}

    init {
        addView(binding.root)
        binding.scoreCard.onBucketSizeSelected = { position -> onBucketSizeSelected(position) }
    }

    fun update(data: ShowHabitViewModel) {
        setupToolbar(binding.toolbar, title = data.title, color = data.color)
        binding.subtitleCard.update(data.subtitle)
        binding.overviewCard.update(data.overview)
        binding.notesCard.update(data.notes)
        binding.targetCard.update(data.target)
        binding.streakCard.update(data.streaks)
        binding.scoreCard.update(data.scores)
        binding.frequencyCard.update(data.frequency)
        if (data.isNumerical) {
            binding.overviewCard.visibility = GONE
            binding.streakCard.visibility = GONE
        } else {
            binding.targetCard.visibility = GONE
        }
    }

    fun setController(controller: Controller) {
        binding.historyCard.setController(controller)
    }

    interface Controller : HistoryCard.Controller
}

class ShowHabitPresenter(
        val habit: Habit,
        val context: Context,
        val preferences: Preferences,
) {
    private val subtitleCardPresenter = SubtitleCardPresenter(habit, context)
    private val overviewCardPresenter = OverviewCardPresenter(habit)
    private val notesCardPresenter = NotesCardPresenter(habit)
    private val targetCardPresenter = TargetCardPresenter(habit = habit,
                                                          firstWeekday = preferences.firstWeekday,
                                                          resources = context.resources)
    private val streakCartPresenter = StreakCartPresenter(habit)
    private val scoreCardPresenter = ScoreCardPresenter(habit = habit,
                                                        firstWeekday = preferences.firstWeekday)
    private val frequencyCardPresenter = FrequencyCardPresenter(habit = habit,
                                                                firstWeekday = preferences.firstWeekday)

    suspend fun present(): ShowHabitViewModel {
        return ShowHabitViewModel(
                title = habit.name,
                color = habit.color,
                isNumerical = habit.isNumerical,
                subtitle = subtitleCardPresenter.present(),
                overview = overviewCardPresenter.present(),
                notes = notesCardPresenter.present(),
                target = targetCardPresenter.present(),
                streaks = streakCartPresenter.present(),
                scores = scoreCardPresenter.present(preferences.defaultScoreSpinnerPosition),
                frequency = frequencyCardPresenter.present(),
        )
    }
}