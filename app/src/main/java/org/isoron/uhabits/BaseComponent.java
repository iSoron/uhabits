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

package org.isoron.uhabits;

import org.isoron.uhabits.commands.ArchiveHabitsCommand;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.commands.CreateHabitCommand;
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.commands.EditHabitCommand;
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.io.AbstractImporter;
import org.isoron.uhabits.io.HabitsCSVExporter;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.tasks.ToggleRepetitionTask;
import org.isoron.uhabits.ui.BaseSystem;
import org.isoron.uhabits.ui.habits.edit.BaseDialogFragment;
import org.isoron.uhabits.ui.habits.edit.HistoryEditorDialog;
import org.isoron.uhabits.ui.habits.list.ListHabitsActivity;
import org.isoron.uhabits.ui.habits.list.ListHabitsController;
import org.isoron.uhabits.ui.habits.list.ListHabitsSelectionMenu;
import org.isoron.uhabits.ui.habits.list.controllers.CheckmarkButtonController;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListAdapter;
import org.isoron.uhabits.ui.habits.list.model.HabitCardListCache;
import org.isoron.uhabits.ui.habits.list.model.HintList;
import org.isoron.uhabits.ui.habits.list.views.CheckmarkPanelView;
import org.isoron.uhabits.ui.habits.show.ShowHabitActivity;
import org.isoron.uhabits.widgets.BaseWidgetProvider;
import org.isoron.uhabits.widgets.HabitPickerDialog;

/**
 * Base component for dependency injection.
 */
public interface BaseComponent
{
    void inject(CheckmarkButtonController checkmarkButtonController);

    void inject(ListHabitsController listHabitsController);

    void inject(CheckmarkPanelView checkmarkPanelView);

    void inject(ToggleRepetitionTask toggleRepetitionTask);

    void inject(HabitCardListCache habitCardListCache);

    void inject(HabitBroadcastReceiver habitBroadcastReceiver);

    void inject(ListHabitsSelectionMenu listHabitsSelectionMenu);

    void inject(HintList hintList);

    void inject(HabitCardListAdapter habitCardListAdapter);

    void inject(ArchiveHabitsCommand archiveHabitsCommand);

    void inject(ChangeHabitColorCommand changeHabitColorCommand);

    void inject(UnarchiveHabitsCommand unarchiveHabitsCommand);

    void inject(EditHabitCommand editHabitCommand);

    void inject(CreateHabitCommand createHabitCommand);

    void inject(HabitPickerDialog habitPickerDialog);

    void inject(BaseWidgetProvider baseWidgetProvider);

    void inject(ShowHabitActivity showHabitActivity);

    void inject(DeleteHabitsCommand deleteHabitsCommand);

    void inject(ListHabitsActivity listHabitsActivity);

    void inject(BaseSystem baseSystem);

    void inject(HistoryEditorDialog historyEditorDialog);

    void inject(HabitsApplication application);

    void inject(Habit habit);

    void inject(AbstractImporter abstractImporter);

    void inject(HabitsCSVExporter habitsCSVExporter);

    void inject(BaseDialogFragment baseDialogFragment);
}
