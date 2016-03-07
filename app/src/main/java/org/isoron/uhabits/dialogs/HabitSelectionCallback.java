/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;

import org.isoron.helpers.ColorHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.commands.ArchiveHabitsCommand;
import org.isoron.uhabits.commands.ChangeHabitColorCommand;
import org.isoron.uhabits.commands.DeleteHabitsCommand;
import org.isoron.uhabits.commands.UnarchiveHabitsCommand;
import org.isoron.uhabits.fragments.EditHabitFragment;
import org.isoron.uhabits.io.CSVExporter;
import org.isoron.uhabits.loaders.HabitListLoader;
import org.isoron.uhabits.models.Habit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class HabitSelectionCallback implements ActionMode.Callback
{
    private HabitListLoader loader;
    private List<Integer> selectedPositions;
    private ReplayableActivity activity;
    private Listener listener;
    private DialogHelper.OnSavedListener onSavedListener;
    private ProgressBar progressBar;

    public interface Listener
    {
        void onActionModeDestroyed(ActionMode mode);
    }

    public HabitSelectionCallback(ReplayableActivity activity, HabitListLoader loader)
    {
        this.activity = activity;
        this.loader = loader;
        selectedPositions = new LinkedList<>();
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public void setProgressBar(ProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    public void setOnSavedListener(DialogHelper.OnSavedListener onSavedListener)
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
        activity.getMenuInflater().inflate(R.menu.list_habits_context, menu);
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
        boolean showColor = true;
        boolean showArchive = true;
        boolean showUnarchive = true;

        if (showEdit) showColor = false;
        for (int i : selectedPositions)
        {
            Habit h = loader.habitsList.get(i);
            if (h.isArchived())
            {
                showColor = false;
                showArchive = false;
            }
            else showUnarchive = false;
        }

        MenuItem itemEdit = menu.findItem(R.id.action_edit_habit);
        MenuItem itemColor = menu.findItem(R.id.action_color);
        MenuItem itemArchive = menu.findItem(R.id.action_archive_habit);
        MenuItem itemUnarchive = menu.findItem(R.id.action_unarchive_habit);

        itemEdit.setVisible(showEdit);
        itemColor.setVisible(showColor);
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
                EditHabitFragment frag = EditHabitFragment.editSingleHabitFragment(firstHabit.getId());
                frag.setOnSavedListener(onSavedListener);
                frag.show(activity.getFragmentManager(), "dialog");
                return true;
            }

            case R.id.action_color:
            {
                ColorPickerDialog picker = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                        ColorHelper.palette, firstHabit.color, 4, ColorPickerDialog.SIZE_SMALL);

                picker.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
                        {
                            public void onColorSelected(int color)
                            {
                                activity.executeCommand(
                                        new ChangeHabitColorCommand(selectedHabits, color), null);
                                mode.finish();
                            }
                        });
                picker.show(activity.getFragmentManager(), "picker");
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

            case R.id.action_export_csv:
            {
                onExportHabitsClick(selectedHabits);
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

    private void onExportHabitsClick(final LinkedList<Habit> selectedHabits)
    {
        new AsyncTask<Void, Void, Void>()
        {
            String filename;

            @Override
            protected void onPreExecute()
            {
                if(progressBar != null)
                {
                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if(filename != null)
                {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("application/zip");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filename)));

                    activity.startActivity(intent);
                }

                if(progressBar != null)
                    progressBar.setVisibility(View.GONE);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                CSVExporter exporter = new CSVExporter(activity, selectedHabits);
                filename = exporter.writeArchive();
                return null;
            }
        }.execute();
    }
}
