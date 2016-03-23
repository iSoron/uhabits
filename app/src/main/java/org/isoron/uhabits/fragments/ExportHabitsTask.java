package org.isoron.uhabits.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.R;
import org.isoron.uhabits.io.HabitsExporter;
import org.isoron.uhabits.models.Habit;

import java.io.File;
import java.util.List;

public class ExportHabitsTask extends AsyncTask<Void, Void, Void>
{
    private final ReplayableActivity activity;
    private ProgressBar progressBar;
    private final List<Habit> selectedHabits;
    String archiveFilename;

    public ExportHabitsTask(ReplayableActivity activity, List<Habit> selectedHabits,
                            ProgressBar progressBar)
    {
        this.selectedHabits = selectedHabits;
        this.progressBar = progressBar;
        this.activity = activity;
    }

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
        if(archiveFilename != null)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(archiveFilename)));

            activity.startActivity(intent);
        }
        else
        {
            activity.showToast(R.string.could_not_export);
        }

        if(progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        String dirName = String.format("%s/export/", activity.getExternalCacheDir());
        HabitsExporter exporter = new HabitsExporter(selectedHabits, dirName);
        archiveFilename = exporter.writeArchive();

        return null;
    }
}
