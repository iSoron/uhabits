package org.isoron.uhabits.core.ui.screens.habits.show

import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.models.HabitGroup
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.NotesCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.ScoreCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.StreakCartPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.views.SubtitleCardState
import org.isoron.uhabits.core.ui.views.Theme

data class ShowHabitGroupState(
    val title: String = "",
    val color: PaletteColor = PaletteColor(1),
    val subtitle: SubtitleCardState,
    val overview: OverviewCardState,
    val notes: NotesCardState,
    val streaks: StreakCardState,
    val scores: ScoreCardState,
    val theme: Theme
)

class ShowHabitGroupPresenter(
    val habitGroup: HabitGroup,
    val preferences: Preferences,
    val screen: Screen,
    val commandRunner: CommandRunner
) {
    val scoreCardPresenter = ScoreCardPresenter(
        preferences = preferences,
        screen = screen
    )

    companion object {
        fun buildState(
            habitGroup: HabitGroup,
            preferences: Preferences,
            theme: Theme
        ): ShowHabitGroupState {
            return ShowHabitGroupState(
                title = habitGroup.name,
                color = habitGroup.color,
                theme = theme,
                subtitle = SubtitleCardPresenter.buildState(
                    habitGroup = habitGroup,
                    theme = theme
                ),
                overview = OverviewCardPresenter.buildState(
                    habitGroup = habitGroup,
                    theme = theme
                ),
                notes = NotesCardPresenter.buildState(
                    habitGroup = habitGroup
                ),
                streaks = StreakCartPresenter.buildState(
                    habitGroup = habitGroup,
                    theme = theme
                ),
                scores = ScoreCardPresenter.buildState(
                    spinnerPosition = preferences.scoreCardSpinnerPosition,
                    habitGroup = habitGroup,
                    firstWeekday = preferences.firstWeekdayInt,
                    theme = theme
                )
            )
        }
    }

    interface Screen :
        ScoreCardPresenter.Screen
}
