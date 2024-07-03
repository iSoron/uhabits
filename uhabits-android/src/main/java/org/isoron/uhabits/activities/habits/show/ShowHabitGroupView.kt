package org.isoron.uhabits.activities.habits.show

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitGroupPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitGroupState
import org.isoron.uhabits.databinding.ShowHabitGroupBinding
import org.isoron.uhabits.utils.setupToolbar

class ShowHabitGroupView(context: Context) : FrameLayout(context) {
    private val binding = ShowHabitGroupBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
    }

    fun setState(data: ShowHabitGroupState) {
        setupToolbar(
            binding.toolbar,
            title = data.title,
            color = data.color,
            theme = data.theme
        )
        binding.subtitleCard.setState(data.subtitle)
        binding.overviewCard.setState(data.overview)
        binding.notesCard.setState(data.notes)
        binding.streakCard.setState(data.streaks)
        binding.scoreCard.setState(data.scores)
    }

    fun setListener(presenter: ShowHabitGroupPresenter) {
        binding.scoreCard.setListener(presenter.scoreCardPresenter)
    }
}
