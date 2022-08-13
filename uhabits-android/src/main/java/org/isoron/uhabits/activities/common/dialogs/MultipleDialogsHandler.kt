package org.isoron.uhabits.activities.common.dialogs

import android.app.Dialog
import java.lang.ref.WeakReference

class MultipleDialogsHandler {
    companion object {
        var currentDialog: WeakReference<Dialog> = WeakReference(null)

        fun Dialog.dismissCurrent() {
            currentDialog.get()?.dismiss()
            currentDialog = WeakReference(this)
        }
    }
}
