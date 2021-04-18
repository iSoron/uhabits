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

package org.isoron.uhabits.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.commands.Command
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.database.Database
import org.isoron.uhabits.core.io.Logging
import org.isoron.uhabits.core.io.LoopDBImporter
import org.isoron.uhabits.core.preferences.Preferences
import java.io.File
import javax.inject.Inject

@AppScope
class SyncManager @Inject constructor(
    val preferences: Preferences,
    val commandRunner: CommandRunner,
    val logging: Logging,
    val networkManager: NetworkManager,
    val server: AbstractSyncServer,
    val db: Database,
    val dbImporter: LoopDBImporter,
) : Preferences.Listener, CommandRunner.Listener, NetworkManager.Listener {

    private var logger = logging.getLogger("SyncManager")
    private var connected = false
    private val tmpFile = File.createTempFile("import", "")
    private var currVersion = 1L
    private var dirty = true
    private lateinit var encryptionKey: EncryptionKey
    private lateinit var syncKey: String

    init {
        preferences.addListener(this)
        commandRunner.addListener(this)
        networkManager.addListener(this)
    }

    fun sync() = CoroutineScope(Dispatchers.Main).launch {
        if (!preferences.isSyncEnabled) {
            logger.info("Device sync is disabled. Skipping sync.")
            return@launch
        }

        encryptionKey = EncryptionKey.fromBase64(preferences.encryptionKey)
        syncKey = preferences.syncKey
        logger.info("Starting sync (key: $syncKey)")

        try {
            pull()
            push()
            logger.info("Sync finished successfully.")
        } catch (e: ConnectionLostException) {
            logger.info("Network unavailable. Aborting sync.")
        } catch (e: ServiceUnavailable) {
            logger.info("Sync service unavailable. Aborting sync.")
        } catch (e: Exception) {
            logger.error("Unexpected sync exception. Disabling sync.")
            logger.error(e)
            preferences.disableSync()
        }
    }

    private suspend fun push(depth: Int = 0) {
        if (depth >= 5) {
            throw RuntimeException()
        }

        if (!dirty) {
            logger.info("Local database not modified. Skipping push.")
            return
        }

        logger.info("Encrypting local database...")
        val encryptedDB = db.file!!.encryptToString(encryptionKey)
        val size = encryptedDB.length / 1024

        try {
            logger.info("Pushing local database (version $currVersion, $size KB)")
            assertConnected()
            server.put(preferences.syncKey, SyncData(currVersion, encryptedDB))
            dirty = false
        } catch (e: EditConflictException) {
            logger.info("Sync conflict detected while pushing.")
            setCurrentVersion(0)
            pull()
            push(depth = depth + 1)
        }
    }

    private suspend fun pull() = Dispatchers.IO {
        logger.info("Querying remote database version...")
        assertConnected()
        val remoteVersion = server.getDataVersion(syncKey)
        logger.info("Remote database version: $remoteVersion")

        if (remoteVersion <= currVersion) {
            logger.info("Local database is up-to-date. Skipping merge.")
        } else {
            logger.info("Pulling remote database...")
            assertConnected()
            val data = server.getData(syncKey)
            val size = data.content.length / 1024
            logger.info("Pulled remote database (version ${data.version}, $size KB)")
            logger.info("Decrypting remote database and merging with local changes...")
            data.content.decryptToFile(encryptionKey, tmpFile)

            try {
                db.beginTransaction()
                dbImporter.importHabitsFromFile(tmpFile)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                logger.error("Failed to import database")
                logger.error(e)
            } finally {
                db.endTransaction()
            }

            dirty = true
            setCurrentVersion(data.version + 1)
        }
    }

    fun onResume() = sync()

    fun onPause() = sync()

    override fun onSyncEnabled() {
        logger.info("Sync enabled.")
        setCurrentVersion(1)
        dirty = true
        sync()
    }

    override fun onNetworkAvailable() {
        logger.info("Network available.")
        connected = true
        sync()
    }

    override fun onNetworkLost() {
        logger.info("Network unavailable.")
        connected = false
    }

    override fun onCommandFinished(command: Command) {
        if (!dirty) setCurrentVersion(currVersion + 1)
        dirty = true
    }

    private fun assertConnected() {
        if (!connected) throw ConnectionLostException()
    }

    private fun setCurrentVersion(v: Long) {
        currVersion = v
        logger.info("Setting local database version: $currVersion")
    }
}

class ConnectionLostException : RuntimeException()
