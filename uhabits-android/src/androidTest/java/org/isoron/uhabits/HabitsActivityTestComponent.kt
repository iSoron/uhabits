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

import android.content.Context
import dev.mokkery.mock
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.activities.HabitsDirFinder
import org.isoron.uhabits.activities.habits.list.ListHabitsModule
import org.isoron.uhabits.activities.habits.list.ListHabitsScreen
import org.isoron.uhabits.activities.habits.list.views.CheckmarkButtonViewFactory
import org.isoron.uhabits.activities.habits.list.views.CheckmarkPanelViewFactory
import org.isoron.uhabits.activities.habits.list.views.HabitCardListAdapter
import org.isoron.uhabits.activities.habits.list.views.HabitCardViewFactory
import org.isoron.uhabits.activities.habits.list.views.NumberButtonViewFactory
import org.isoron.uhabits.activities.habits.list.views.NumberPanelViewFactory
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsMenuBehavior
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.inject.HabitsApplicationComponent

@ActivityScope
@Component
abstract class HabitsActivityTestComponent(
    @Component val parent: HabitsApplicationComponent,
    @get:Provides @get:ActivityContext
    val activityContext: Context
) {
    abstract fun getCheckmarkPanelViewFactory(): CheckmarkPanelViewFactory
    abstract fun getHabitCardViewFactory(): HabitCardViewFactory
    abstract fun getEntryButtonViewFactory(): CheckmarkButtonViewFactory
    abstract fun getNumberButtonViewFactory(): NumberButtonViewFactory
    abstract fun getNumberPanelViewFactory(): NumberPanelViewFactory

    @Provides
    open fun listHabitsBehavior(): ListHabitsBehavior = mock()

    @ActivityScope
    @Provides
    open fun themeSwitcher(
        @ActivityContext context: Context,
        prefs: Preferences
    ): ThemeSwitcher = AndroidThemeSwitcher(context, prefs)

    open val HabitCardListAdapter.bindAdapter: ListHabitsMenuBehavior.Adapter
        @Provides get() = this

    open val ListHabitsModule.bindBugReporter: ListHabitsBehavior.BugReporter
        @Provides get() = this

    open val ListHabitsScreen.bindMenuScreen: ListHabitsMenuBehavior.Screen
        @Provides get() = this

    open val ListHabitsScreen.bindScreen: ListHabitsBehavior.Screen
        @Provides get() = this

    open val HabitCardListAdapter.bindSelMenuAdapter: ListHabitsSelectionMenuBehavior.Adapter
        @Provides get() = this

    open val ListHabitsScreen.bindSelMenuScreen: ListHabitsSelectionMenuBehavior.Screen
        @Provides get() = this

    open val HabitsDirFinder.bindDirFinder: ListHabitsBehavior.DirFinder
        @Provides get() = this
}
