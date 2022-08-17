package org.isoron.uhabits.utils

import android.app.Dialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

var currentDialog: WeakReference<Dialog> = WeakReference(null)

fun Dialog.dismissCurrentAndShow() {
    currentDialog.get()?.dismiss()
    currentDialog = WeakReference(this)
    show()
}

fun DialogFragment.dismissCurrentAndShow(fragmentManager: FragmentManager, tag: String) {
    currentDialog.get()?.dismiss()
    show(fragmentManager, tag)
    fragmentManager.executePendingTransactions()
    currentDialog = WeakReference(this.dialog)
}
