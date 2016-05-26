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

package org.isoron.uhabits.ui.settings;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FilePickerDialog implements AdapterView.OnItemClickListener
{
    private static final String PARENT_DIR = "..";

    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    public interface OnFileSelectedListener
    {
        void onFileSelected(File file);
    }

    private OnFileSelectedListener listener;

    public FilePickerDialog(Activity activity, File initialDirectory)
    {
        this.activity = activity;

        list = new ListView(activity);
        list.setOnItemClickListener(this);

        dialog = new Dialog(activity);
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        navigateTo(initialDirectory);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int which, long id)
    {
        String filename = (String) list.getItemAtPosition(which);
        File file;

        if (filename.equals(PARENT_DIR))
            file = currentPath.getParentFile();
        else
            file = new File(currentPath, filename);

        if (file.isDirectory())
        {
            navigateTo(file);
        }
        else
        {
            if (listener != null) listener.onFileSelected(file);
            dialog.dismiss();
        }
    }

    public void show()
    {
        dialog.show();
    }

    public void setListener(OnFileSelectedListener listener)
    {
        this.listener = listener;
    }

    private void navigateTo(File path)
    {
        if (!path.exists()) return;

        File[] dirs = path.listFiles(new ReadableDirFilter());
        File[] files = path.listFiles(new RegularReadableFileFilter());
        if(dirs == null || files == null) return;

        this.currentPath = path;
        dialog.setTitle(currentPath.getPath());
        list.setAdapter(new FilePickerAdapter(getFileList(path, dirs, files)));
    }

    @NonNull
    private String[] getFileList(File path, File[] dirs, File[] files)
    {
        int count = 0;
        int length = dirs.length + files.length;
        String[] fileList;

        if (path.getParentFile() == null || !path.getParentFile().canRead())
        {
            fileList = new String[length];
        }
        else
        {
            fileList = new String[length + 1];
            fileList[count++] = PARENT_DIR;
        }

        Arrays.sort(dirs);
        Arrays.sort(files);

        for (File dir : dirs)
            fileList[count++] = dir.getName();

        for (File file : files)
            fileList[count++] = file.getName();

        return fileList;
    }

    private class FilePickerAdapter extends ArrayAdapter<String>
    {
        public FilePickerAdapter(@NonNull String[] fileList)
        {
            super(FilePickerDialog.this.activity, android.R.layout.simple_list_item_1, fileList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent)
        {
            view = super.getView(pos, view, parent);
            TextView tv = (TextView) view;
            tv.setSingleLine(true);
            return view;
        }
    }

    private static class ReadableDirFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return (file.isDirectory() && file.canRead());
        }
    }

    private class RegularReadableFileFilter implements FileFilter
    {
        @Override
        public boolean accept(File file)
        {
            return !file.isDirectory() && file.canRead();
        }
    }
}