package org.isoron.uhabits.activities.habits.show.views

import android.content.*
import android.util.*
import android.view.*
import android.widget.*
import org.isoron.uhabits.activities.habits.show.*
import org.isoron.uhabits.databinding.*

class NotesCard(
        context: Context,
        attrs: AttributeSet
) : LinearLayout(context, attrs), ShowHabitPresenter.Listener {

    private val binding = ShowHabitNotesBinding.inflate(LayoutInflater.from(context), this)
    lateinit var presenter: ShowHabitPresenter

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        presenter.addListener(this)
        presenter.requestData(this)
    }

    override fun onDetachedFromWindow() {
        presenter.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onData(data: ShowHabitViewModel) {
        if (data.description.isEmpty()) {
            visibility = GONE
        } else {
            visibility = VISIBLE
            binding.habitNotes.text = data.description
        }
    }
}