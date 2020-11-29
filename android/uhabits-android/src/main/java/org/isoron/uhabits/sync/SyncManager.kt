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
import android.net.*
import android.util.*
import kotlinx.coroutines.*
import org.isoron.androidbase.*
import org.isoron.uhabits.core.*
import org.isoron.uhabits.core.commands.*
import org.isoron.uhabits.core.preferences.*
import org.isoron.uhabits.core.tasks.*
import org.isoron.uhabits.tasks.*
import org.isoron.uhabits.utils.*
import java.io.*
import javax.inject.*

@AppScope
class SyncManager @Inject constructor(
        val preferences: Preferences,
        private val importDataTaskFactory: ImportDataTaskFactory,
        val commandRunner: CommandRunner,
        @AppContext val context: Context
) : Preferences.Listener, CommandRunner.Listener, ConnectivityManager.NetworkCallback() {

    private var connected = false

    private val server = RemoteSyncServer(baseURL = preferences.syncBaseURL)

    private val tmpFile = File.createTempFile("import", "", context.externalCacheDir)

    private var currVersion = 1L

    private var dirty = true

    private var taskRunner = SingleThreadTaskRunner()

    private lateinit var encryptionKey: EncryptionKey

    private lateinit var syncKey: String

    init {
        preferences.addListener(this)
        commandRunner.addListener(this)
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(NetworkRequest.Builder().build(), this)
    }

    fun sync() = CoroutineScope(Dispatchers.Main).launch {
        if (!preferences.isSyncEnabled) {
            Log.i("SyncManager", "Device sync is disabled. Skipping sync.")
            return@launch
        }

        encryptionKey = EncryptionKey.fromBase64(preferences.encryptionKey)
        syncKey = preferences.syncKey
        Log.i("SyncManager", "Starting sync (key: $syncKey)")

        try {
            pull()
            push()
        } catch (e: ConnectionLostException) {
            Log.i("SyncManager", "Network unavailable. Aborting sync.")
        } catch (e: ServiceUnavailable) {
            Log.i("SyncManager", "Sync service unavailable. Aborting sync.")
        } catch (e: Exception) {
            Log.e("SyncManager", "Unexpected sync exception. Disabling sync.", e)
            preferences.disableSync()
        }

        Log.i("SyncManager", "Sync finished successfully.")
    }

    private suspend fun push(depth: Int = 0) {
        if (depth >= 5) {
            throw RuntimeException()
        }

        if (!dirty) {
            Log.i("SyncManager", "Local database not modified. Skipping push.")
            return
        }

        Log.i("SyncManager", "Encrypting local database...")
        val db = DatabaseUtils.getDatabaseFile(context)
        val encryptedDB = db.encryptToString(encryptionKey)
        val size = encryptedDB.length / 1024

        try {
            Log.i("SyncManager", "Pushing local database (version $currVersion, $size KB)")
            assertConnected()
            server.put(preferences.syncKey, SyncData(currVersion, encryptedDB))
            dirty = false
        } catch (e: EditConflictException) {
            Log.i("SyncManager", "Sync conflict detected while pushing.")
            setCurrentVersion(0)
            pull()
            push(depth = depth + 1)
        }
    }

    private suspend fun pull() {
        Log.i("SyncManager", "Querying remote database version...")
        assertConnected()
        val remoteVersion = server.getDataVersion(syncKey)
        Log.i("SyncManager", "Remote database version: $remoteVersion")

        if (remoteVersion <= currVersion) {
            Log.i("SyncManager", "Local database is up-to-date. Skipping merge.")
        } else {
            Log.i("SyncManager", "Pulling remote database...")
            assertConnected()
            val data = server.getData(syncKey)
            val size = data.content.length / 1024
            Log.i("SyncManager", "Pulled remote database (version ${data.version}, $size KB)")
            Log.i("SyncManager", "Decrypting remote database and merging with local changes...")
            data.content.decryptToFile(encryptionKey, tmpFile)
            taskRunner.execute(importDataTaskFactory.create(tmpFile) { tmpFile.delete() })
            dirty = true
            setCurrentVersion(data.version + 1)
        }
    }

    fun onResume() = sync()

    fun onPause() = sync()

    override fun onSyncEnabled() {
        Log.i("SyncManager", "Sync enabled.")
        setCurrentVersion(1)
        dirty = true
        sync()
    }

    override fun onAvailable(network: Network) {
        Log.i("SyncManager", "Network available.")
        connected = true
        sync()
    }

    override fun onLost(network: Network) {
        Log.i("SyncManager", "Network unavailable.")
        connected = false
    }

    override fun onCommandExecuted(command: Command?, refreshKey: Long?) {
        if (!dirty) setCurrentVersion(currVersion + 1)
        dirty = true
    }

    private fun assertConnected() {
        if (!connected) throw ConnectionLostException()
    }

    private fun setCurrentVersion(v: Long) {
        currVersion = v
        Log.i("SyncManager", "Setting local database version: $currVersion")
    }
}

class ConnectionLostException : RuntimeException()
