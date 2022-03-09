package org.isoron.uhabits.activities.common.dialogs

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.databinding.CheckmarkDialogBinding
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.InterfaceUtils
import org.isoron.uhabits.utils.StyledResources
import java.util.Locale
import javax.inject.Inject

class CheckmarkDialog
@Inject constructor(
    @ActivityContext private val context: Context,
    private val preferences: Preferences,
) : View.OnClickListener {

    private lateinit var binding: CheckmarkDialogBinding
    private lateinit var fontAwesome: Typeface
    private val allButtons = mutableListOf<Button>()
    private var selectedButton: Button? = null

    fun create(
        selectedValue: Int,
        notes: String,
        date: LocalDate,
        paletteColor: PaletteColor,
        callback: ListHabitsBehavior.CheckMarkDialogCallback,
        theme: Theme,
    ): AlertDialog {
        binding = CheckmarkDialogBinding.inflate(LayoutInflater.from(context))
        fontAwesome = InterfaceUtils.getFontAwesome(context)!!
        binding.etNotes.append(notes)
        setUpButtons(selectedValue, theme.color(paletteColor).toInt())

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle(JavaLocalDateFormatter(Locale.getDefault()).longFormat(date))
            .setPositiveButton(R.string.save) { _, _ ->
                val newValue = when (selectedButton?.id) {
                    R.id.yesBtn -> YES_MANUAL
                    R.id.noBtn -> NO
                    R.id.skippedBtn -> SKIP
                    else -> UNKNOWN
                }
                callback.onNotesSaved(newValue, binding.etNotes.text.toString())
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                callback.onNotesDismissed()
            }
            .setOnDismissListener {
                callback.onNotesDismissed()
            }
            .create()

        dialog.setOnShowListener {
            binding.etNotes.requestFocus()
            dialog.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return dialog
    }

    private fun setUpButtons(value: Int, color: Int) {
        val sres = StyledResources(context)
        val mediumContrastColor = sres.getColor(R.attr.contrast60)
        setButtonAttrs(binding.yesBtn, color)
        setButtonAttrs(binding.noBtn, mediumContrastColor)
        setButtonAttrs(binding.skippedBtn, color, visible = preferences.isSkipEnabled)
        setButtonAttrs(binding.questionBtn, mediumContrastColor, visible = preferences.areQuestionMarksEnabled)
        when (value) {
            UNKNOWN -> if (preferences.areQuestionMarksEnabled) {
                binding.questionBtn.performClick()
            } else {
                binding.noBtn.performClick()
            }
            SKIP -> binding.skippedBtn.performClick()
            YES_MANUAL -> binding.yesBtn.performClick()
            YES_AUTO, NO -> binding.noBtn.performClick()
        }
    }

    private fun setButtonAttrs(button: Button, color: Int, visible: Boolean = true) {
        button.apply {
            visibility = if (visible) View.VISIBLE else View.GONE
            typeface = fontAwesome
            setTextColor(color)
            setOnClickListener(this@CheckmarkDialog)
        }
        allButtons.add(button)
    }

    override fun onClick(v: View?) {
        allButtons.forEach {
            if (v?.id == it.id) {
                it.isSelected = true
                selectedButton = it
            } else it.isSelected = false
        }
    }
}
