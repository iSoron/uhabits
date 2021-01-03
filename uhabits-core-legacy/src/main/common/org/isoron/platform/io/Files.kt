/*
 * Copyright (C) 2016-2019 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.platform.io

import org.isoron.platform.gui.*

interface FileOpener {
    /**
     * Opens a file which was shipped bundled with the application, such as a
     * migration file.
     *
     * The path is relative to the assets folder. For example, to open
     * assets/main/migrations/09.sql you should provide migrations/09.sql
     * as the path.
     *
     * This function always succeed, even if the file does not exist.
     */
    fun openResourceFile(path: String): ResourceFile

    /**
     * Opens a file which was not shipped with the application, such as
     * databases and logs.
     *
     * The path is relative to the user folder. For example, if the application
     * stores the user data at /home/user/.loop/ and you wish to open the file
     * /home/user/.loop/crash.log, you should provide crash.log as the path.
     *
     * This function always succeed, even if the file does not exist.
     */
    fun openUserFile(path: String): UserFile
}

/**
 * Represents a file that was created after the application was installed, as a
 * result of some user action, such as databases and logs.
 */
interface UserFile {
    /**
     * Deletes the user file. If the file does not exist, nothing happens.
     */
    suspend fun delete()

    /**
     * Returns true if the file exists.
     */
    suspend fun exists(): Boolean

    /**
     * Returns the lines of the file. If the file does not exist, throws an
     * exception.
     */
    suspend fun lines(): List<String>
}

/**
 * Represents a file that was shipped with the application, such as migration
 * files or database templates.
 */
interface ResourceFile {
    /**
     * Copies the resource file to the specified user file. If the user file
     * already exists, it is replaced. If not, a new file is created.
     */
    suspend fun copyTo(dest: UserFile)

    /**
     * Returns the lines of the resource file. If the file does not exist,
     * throws an exception.
     */
    suspend fun lines(): List<String>

    /**
     * Returns true if the file exists.
     */
    suspend fun exists(): Boolean

    /**
     * Loads resource file as an image.
     */
    suspend fun toImage(): Image
}