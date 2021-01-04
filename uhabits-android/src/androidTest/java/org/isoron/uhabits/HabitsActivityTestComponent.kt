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

package org.isoron.uhabits

import dagger.Component
import dagger.Module
import dagger.Provides
import org.isoron.uhabits.activities.habits.list.ListHabitsModule
import org.isoron.uhabits.activities.habits.list.views.CheckmarkButtonViewFactory
import org.isoron.uhabits.activities.habits.list.views.CheckmarkPanelViewFactory
import org.isoron.uhabits.activities.habits.list.views.HabitCardViewFactory
import org.isoron.uhabits.activities.habits.list.views.NumberButtonViewFactory
import org.isoron.uhabits.activities.habits.list.views.NumberPanelViewFactory
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.inject.ActivityContextModule
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.inject.HabitModule
import org.isoron.uhabits.inject.HabitsActivityModule
import org.isoron.uhabits.inject.HabitsApplicationComponent
import org.mockito.Mockito.mock

@Module
class TestModule {
    @Provides fun ListHabitsBehavior() = mock(ListHabitsBehavior::class.java)
}

@ActivityScope
@Component(
    modules = arrayOf(
        ActivityContextModule::class,
        HabitsActivityModule::class,
        ListHabitsModule::class,
        HabitModule::class,
        TestModule::class
    ),
    dependencies = arrayOf(HabitsApplicationComponent::class)
)
interface HabitsActivityTestComponent {
    fun getCheckmarkPanelViewFactory(): CheckmarkPanelViewFactory
    fun getHabitCardViewFactory(): HabitCardViewFactory
    fun getEntryButtonViewFactory(): CheckmarkButtonViewFactory
    fun getNumberButtonViewFactory(): NumberButtonViewFactory
    fun getNumberPanelViewFactory(): NumberPanelViewFactory
}
