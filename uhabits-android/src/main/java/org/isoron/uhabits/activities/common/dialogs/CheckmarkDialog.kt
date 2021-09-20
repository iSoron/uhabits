package org.isoron.uhabits.activities.common.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.inject.ActivityContext
import javax.inject.Inject

class CheckmarkDialog
@Inject constructor(
    @ActivityContext private val context: Context
) {

    fun create(
        notes: String,
        callback: ListHabitsBehavior.CheckMarkDialogCallback
    ): AlertDialog {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.checkmark_dialog, null)

        val etNotes = view.findViewById<EditText>(R.id.etNotes)

        etNotes.setText(notes)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.edit_notes)
            .setPositiveButton(R.string.save) { _, _ ->
                val note = etNotes.text.toString()
                callback.onNotesSaved(note)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                callback.onNotesDismissed()
            }
            .setOnDismissListener {
                callback.onNotesDismissed()
            }
            .create()

        dialog.setOnShowListener {
            etNotes.requestFocus()
            dialog.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return dialog
    }
}
