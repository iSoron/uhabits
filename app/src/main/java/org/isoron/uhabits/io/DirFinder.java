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

package org.isoron.uhabits.io;

import android.content.*;
import android.os.*;
import android.support.annotation.*;
import android.util.*;

import org.isoron.uhabits.*;

import java.io.*;

import javax.inject.*;

import static android.support.v4.content.ContextCompat.*;

/**
 * A DirFinder locates suitable directories for storing user files.
 */
public class DirFinder
{
    private static final String TAG = "DirFinder";

    private final Context context;

    @Inject
    public DirFinder(@AppContext Context context)
    {
        this.context = context;
    }

    @Nullable
    public File findSDCardDir(@Nullable String subpath)
    {
        File parents[] = new File[]{
            Environment.getExternalStorageDirectory()
        };

        return findDir(parents, subpath);
    }

    @Nullable
    public File findStorageDir(@Nullable String relativePath)
    {
        File potentialParents[] = getExternalFilesDirs(context, null);

        if (potentialParents == null)
        {
            Log.e(TAG, "getFilesDir: getExternalFilesDirs returned null");
            return null;
        }

        return findDir(potentialParents, relativePath);
    }

    @Nullable
    private File findDir(@NonNull File potentialParents[],
                         @Nullable String relativePath)
    {
        if (relativePath == null) relativePath = "";

        File chosenDir = null;
        for (File dir : potentialParents)
        {
            if (dir == null || !dir.canWrite()) continue;
            chosenDir = dir;
            break;
        }

        if (chosenDir == null)
        {
            Log.e(TAG,
                "getDir: all potential parents are null or non-writable");
            return null;
        }

        File dir = new File(
            String.format("%s/%s/", chosenDir.getAbsolutePath(), relativePath));
        if (!dir.exists() && !dir.mkdirs())
        {
            Log.e(TAG,
                "getDir: chosen dir does not exist and cannot be created");
            return null;
        }

        return dir;
    }
}
