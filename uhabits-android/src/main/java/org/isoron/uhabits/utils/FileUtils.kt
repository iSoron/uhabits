/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun File.copyTo(dst: File) {
    val inStream = FileInputStream(this)
    val outStream = FileOutputStream(dst)
    inStream.copyTo(outStream)
}

fun InputStream.copyTo(dst: File) {
    val outStream = FileOutputStream(dst)
    this.copyTo(outStream)
}

fun InputStream.copyTo(out: OutputStream) {
    var numBytes: Int
    val buffer = ByteArray(1024)
    while (this.read(buffer).also { numBytes = it } != -1) {
        out.write(buffer, 0, numBytes)
    }
}

object FileUtils {
    @JvmStatic
    fun getDir(potentialParentDirs: Array<File>, relativePath: String): File? {
        val chosenDir: File? = potentialParentDirs.firstOrNull { dir -> dir.canWrite() }
        if (chosenDir == null) {
            Log.e("FileUtils", "getDir: all potential parents are null or non-writable")
            return null
        }
        val dir = File("${chosenDir.absolutePath}/$relativePath/")
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e("FileUtils", "getDir: chosen dir does not exist and cannot be created")
            return null
        }
        return dir
    }

    @JvmStatic
    fun getSDCardDir(relativePath: String): File? {
        val parents = arrayOf(Environment.getExternalStorageDirectory())
        return getDir(parents, relativePath)
    }
}
