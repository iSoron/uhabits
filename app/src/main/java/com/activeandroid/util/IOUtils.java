
package com.activeandroid.util;

/*
 * Copyright (C) 2014 Markus Pfeiffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;

import com.activeandroid.util.Log;


public class IOUtils {

    /**
     * <p>
     * Unconditionally close a {@link Closeable}.
     * </p>
     * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is
     * typically used in finally blocks.
     * @param closeable A {@link Closeable} to close.
     */
    public static void closeQuietly(final Closeable closeable) {

        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (final IOException e) {
            Log.e("Couldn't close closeable.", e);
        }
    }

    /**
     * <p>
     * Unconditionally close a {@link Cursor}.
     * </p>
     * Equivalent to {@link Cursor#close()}, except any exceptions will be ignored. This is
     * typically used in finally blocks.
     * @param cursor A {@link Cursor} to close.
     */
    public static void closeQuietly(final Cursor cursor) {

        if (cursor == null) {
            return;
        }

        try {
            cursor.close();
        } catch (final Exception e) {
            Log.e("Couldn't close cursor.", e);
        }
    }
}
