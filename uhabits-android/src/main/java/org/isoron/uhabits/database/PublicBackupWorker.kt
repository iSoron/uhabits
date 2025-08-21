package org.isoron.uhabits.database

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.utils.DatabaseUtils
import org.isoron.uhabits.utils.UriUtils
import java.io.File
import java.io.IOException

class PublicBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as HabitsApplication
        val prefs = app.component.preferences
        val uriString = prefs.publicBackupUri ?: return Result.failure()
        val path = UriUtils.getPathFromTreeUri(applicationContext, Uri.parse(uriString))
            ?: return Result.failure()
        return try {
            val addDate = prefs.isPublicBackupAddDateEnabled
            DatabaseUtils.saveDatabaseCopy(applicationContext, File(path), addDate)
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        }
    }
}
