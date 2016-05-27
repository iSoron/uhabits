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

package org.isoron.uhabits.ui.habits.list;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;

import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.ArchiveHabitsCommand;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.ui.BaseActivity;
import org.isoron.uhabits.ui.habits.edit.EditHabitDialogFragment;
import org.isoron.uhabits.utils.ColorUtils;
import org.isoron.uhabits.utils.InterfaceUtils;

import java.util.LinkedList;
import java.util.List;

public class HabitListSelectionCallback implements ActionMode.Callback
{
    private HabitListLoader loader;
    private List<Integer> selectedPositions;
    private BaseActivity activity;
    private Listener listener;
    private InterfaceUtils.OnSavedListener onSavedListener;

    public interface Listener
    {
        void onActionModeDestroyed(ActionMode mode);
    }

    public HabitListSelectionCallback(BaseActivity activity, HabitListLoader loader)
    {
        this.activity = activity;
        this.loader = loader;
        selectedPositions = new LinkedList<>();
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public void setOnSavedListener(InterfaceUtils.OnSavedListener onSavedListener)
    {
        this.onSavedListener = onSavedListener;
    }

    public void setSelectedPositions(List<Integer> selectedPositions)
    {
        this.selectedPositions = selectedPositions;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        activity.getMenuInflater().inflate(R.menu.list_habits_selection, menu);
        updateTitle(mode);
        updateActions(menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        updateTitle(mode);
        updateActions(menu);
        return true;
    }

    private void updateActions(Menu menu)
    {
        boolean showEdit = (selectedPositions.size() == 1);
        boolean showArchive = true;
        boolean showUnarchive = true;
        for (int i : selectedPositions)
        {
            Habit h = loader.habitsList.get(i);
            if (h.isArchived()) showArchive = false;
            else showUnarchive = false;
        }

        MenuItem itemEdit = menu.findItem(R.id.action_edit_habit);
        MenuItem itemColor = menu.findItem(R.id.action_color);
        MenuItem itemArchive = menu.findItem(R.id.action_archive_habit);
        MenuItem itemUnarchive = menu.findItem(R.id.action_unarchive_habit);

        itemColor.setVisible(true);
        itemEdit.setVisible(showEdit);
        itemArchive.setVisible(showArchive);
        itemUnarchive.setVisible(showUnarchive);
    }

    private void updateTitle(ActionMode mode)
    {
        mode.setTitle("" + selectedPositions.size());
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item)
    {
        final LinkedList<Habit> selectedHabits = new LinkedList<>();
        for (int i : selectedPositions)
            selectedHabits.add(loader.habitsList.get(i));

        Habit firstHabit = selectedHabits.getFirst();

        switch (item.getItemId())
        {
            case R.id.action_archive_habit:
                activity.executeCommand(new ArchiveHabitsCommand(selectedHabits), null);
                mode.finish();
                return true;

            case R.id.action_unarchive_habit:
                activity.executeCommand(new UnarchiveHabitsCommand(selectedHabits), null);
                mode.finish();
                return true;

            case R.id.action_edit_habit:
            {
                EditHabitDialogFragment
                        frag = EditHabitDialogFragment.editSingleHabitFragment(firstHabit.getId());
                frag.setOnSavedListener(onSavedListener);
                frag.show(activity.getSupportFragmentManager(), "editHabit");
                return true;
            }

            case R.id.action_color:
            {
                int originalAndroidColor = ColorUtils.getColor(activity, firstHabit.color);

                ColorPickerDialog picker = ColorPickerDialog.newInstance(
                        R.string.color_picker_default_title, ColorUtils.getPalette(activity),
                        originalAndroidColor, 4, ColorPickerDialog.SIZE_SMALL);

                picker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
                        {
                            public void onColorSelected(int androidColor)
                            {
                                int paletteColor = ColorUtils.colorToPaletteIndex(activity,
                                        androidColor);
                                activity.executeCommand(new ChangeHabitColorCommand(selectedHabits,
                                        paletteColor), null);
                                mode.finish();
                            }
                        });
                picker.show(activity.getSupportFragmentManager(), "picker");
                return true;
            }

            case R.id.action_delete:
            {
                new AlertDialog.Builder(activity).setTitle(R.string.delete_habits)
                        .setMessage(R.string.delete_habits_message)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        activity.executeCommand(
                                                new DeleteHabitsCommand(selectedHabits), null);
                                        mode.finish();
                                    }
                                }).setNegativeButton(android.R.string.no, null)
                        .show();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode)
    {
        if(listener != null) listener.onActionModeDestroyed(mode);
    }
}
