package org.isoron.uhabits.utils

import android.app.Dialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

var currentDialog: WeakReference<Dialog> = WeakReference(null)
var currentDialogFragment: WeakReference<DialogFragment> = WeakReference(null)

fun dismissCurrentDialog() {
    currentDialog.get()?.dismiss()
    currentDialog = WeakReference(null)
    currentDialogFragment.get()?.dismiss()
    currentDialogFragment = WeakReference(null)
}

fun Dialog.dismissCurrentAndShow() {
    dismissCurrentDialog()
    currentDialog = WeakReference(this)
    show()
}

fun DialogFragment.dismissCurrentAndShow(fragmentManager: FragmentManager, tag: String) {
    dismissCurrentDialog()
    currentDialogFragment = WeakReference(this)
    show(fragmentManager, tag)
    fragmentManager.executePendingTransactions()
}
