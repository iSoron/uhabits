package org.isoron.uhabits.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Dummy receiver, relevant code is executed through HabitsApplication.
        Log.d("UpdateReceiver", "Update receiver called.")
    }
}
