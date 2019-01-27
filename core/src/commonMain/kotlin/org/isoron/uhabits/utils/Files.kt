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

package org.isoron.uhabits.utils

/**
 * Represents a file that was shipped with the application, such as migration
 * files or translations. These files cannot be deleted.
 */
interface ResourceFile {
    fun readLines(): List<String>
}

/**
 * Represents a file that was created after the application was installed, as a
 * result of some user action, such as databases and logs. These files can be
 * deleted.
 */
interface UserFile {
    fun delete()
    fun exists(): Boolean
}

interface FileOpener {
    /**
     * Opens a file which was shipped bundled with the application, such as a
     * migration file.
     *
     * The path is relative to the assets folder. For example, to open
     * assets/main/migrations/09.sql you should provide migrations/09.sql
     * as the filename.
     */
    fun openResourceFile(filename: String): ResourceFile

    /**
     * Opens a file which was not shipped with the application, such as
     * databases and logs.
     *
     * The path is relative to the user folder. For example, if the application
     * stores the user data at /home/user/.loop/ and you wish to open the file
     * /home/user/.loop/crash.log, you should provide crash.log as the filename.
     */
    fun openUserFile(filename: String): UserFile
}
