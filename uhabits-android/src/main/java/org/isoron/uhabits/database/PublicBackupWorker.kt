package org.isoron.uhabits.database

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.utils.DatabaseUtils

class PublicBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as HabitsApplication
        val prefs = app.component.preferences
        val uriString = prefs.publicBackupUri ?: return Result.failure()
        val folderUri = Uri.parse(uriString)
        return try {
            val addDate = prefs.isPublicBackupAddDateEnabled
            DatabaseUtils.saveDatabaseCopy(applicationContext, folderUri, addDate)
            Result.success()
        } catch (e: Exception) {
            Log.e("PublicBackupWorker", "backup failed", e)
            Result.retry()
        }
    }
}
