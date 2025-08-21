package org.isoron.uhabits.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract

object UriUtils {
    fun getPathFromTreeUri(context: Context, uri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val parts = docId.split(":")
        val type = parts[0]
        val relPath = if (parts.size > 1) parts[1] else ""
        val base = if ("primary" == type) {
            Environment.getExternalStorageDirectory().path
        } else {
            "/storage/" + type
        }
        return if (relPath.isEmpty()) base else "$base/$relPath"
    }
}
