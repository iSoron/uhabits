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
import platform.Foundation.*
import platform.UIKit.*

class IosFileOpener : FileOpener {
    override fun openResourceFile(path: String): ResourceFile {
        val resPath = NSBundle.mainBundle.resourcePath!!
        return IosFile("$resPath/$path")
    }

    override fun openUserFile(path: String): UserFile {
        val manager = NSFileManager.defaultManager
        val basePath = manager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val filePath = (basePath.first() as NSURL).URLByAppendingPathComponent(path)!!.path!!
        return IosFile(filePath)
    }
}

class IosFile(val path: String) : UserFile, ResourceFile {
    override suspend fun delete() {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }

    override suspend fun exists(): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    override suspend fun lines(): List<String> {
        if (!exists()) throw Exception("File not found: $path")
        val contents = NSString.stringWithContentsOfFile(path)
        return contents.toString().lines()
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override suspend fun copyTo(dest: UserFile) {
        val destPath = (dest as IosFile).path
        val manager = NSFileManager.defaultManager
        val destParentPath = (destPath as NSString).stringByDeletingLastPathComponent
        NSFileManager.defaultManager.createDirectoryAtPath(destParentPath, true, null, null)
        manager.copyItemAtPath(path, destPath, null)
    }

    override suspend fun toImage(): Image {
        return IosImage(UIImage.imageWithContentsOfFile(path)!!)
    }
}