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

import platform.Foundation.*

class IosResourceFile(val path: String) : ResourceFile {
    private val fileManager = NSFileManager.defaultManager()
    override fun readLines(): List<String> {
        val contents = NSString.stringWithContentsOfFile(path) as NSString
        return contents.componentsSeparatedByCharactersInSet(NSCharacterSet.newlineCharacterSet()) as List<String>
    }
}

class IosUserFile(val path: String) : UserFile {
    override fun exists(): Boolean {
        return NSFileManager.defaultManager().fileExistsAtPath(path)
    }

    override fun delete() {
        NSFileManager.defaultManager().removeItemAtPath(path, null)
    }
}

class IosFileOpener : FileOpener {
    override fun openResourceFile(filename: String): ResourceFile {
        val root = NSBundle.mainBundle.resourcePath!!
        return IosResourceFile("$root/$filename")
    }

    override fun openUserFile(filename: String): UserFile {
        val manager = NSFileManager.defaultManager()
        val root = manager.URLForDirectory(NSDocumentDirectory,
                                           NSUserDomainMask,
                                           null, false, null)!!.path
        return IosUserFile("$root/$filename")
    }
}