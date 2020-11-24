/*
 * Copyright (C) 2016-2020 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.sync

import android.content.*
import android.util.*
import kotlinx.coroutines.*
import org.isoron.androidbase.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.tasks.*
import org.isoron.uhabits.utils.*
import java.io.*
import javax.inject.*

@AppScope
class SyncManager @Inject constructor(
        val preferences: Preferences,
        val taskRunner: TaskRunner,
        val importDataTaskFactory: ImportDataTaskFactory,
        @AppContext val context: Context
) : Preferences.Listener {

    private val server = RemoteSyncServer()
    private val tmpFile = File.createTempFile("import", "", context.externalCacheDir)
    private var currVersion = 0L

    init {
        preferences.addListener(this)
    }


    fun sync() {
        if(!preferences.isSyncEnabled) {
            Log.i("SyncManager", "Device sync is disabled. Skipping sync")
            return
        }
        taskRunner.execute {
            runBlocking {
                try {
                    Log.i("SyncManager", "Starting sync (key: ${preferences.syncKey})")
                    fetchAndMerge()
                    upload()
                    Log.i("SyncManager", "Sync finished")
                } catch (e: Exception) {
                    Log.e("SyncManager", "Unexpected sync exception. Disabling sync", e)
                    preferences.isSyncEnabled = false
                    preferences.syncKey = ""
                    preferences.encryptionKey = ""
                }
                return@runBlocking
            }
        }
    }

    suspend fun upload() {
        Log.i("SyncManager", "Encrypting database...")
        val db = DatabaseUtils.getDatabaseFile(context)
        val encryptedDB = db.encryptToString(preferences.encryptionKey)
        Log.i("SyncManager", "Uploading database (version ${currVersion}, ${encryptedDB.length / 1024} KB)")
        server.put(preferences.syncKey, SyncData(currVersion, encryptedDB))
    }

    suspend fun fetchAndMerge() {
        Log.i("SyncManager", "Fetching database from server...")
        val data = server.getData(preferences.syncKey)
        Log.i("SyncManager", "Fetched database (version ${data.version}, ${data.content.length / 1024} KB)")
        if (data.version <= currVersion) {
            Log.i("SyncManager", "Local version is up-to-date. Skipping merge.")
        } else {
            Log.i("SyncManager", "Decrypting and merging with local changes...")
            data.content.decryptToFile(preferences.encryptionKey, tmpFile)
            taskRunner.execute(importDataTaskFactory.create(tmpFile) { tmpFile.delete() })
        }
        currVersion = data.version + 1
    }

    fun onResume() = sync()
    fun onPause() = sync()
    override fun onSyncEnabled() = sync()
}