package org.isoron.uhabits.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import org.isoron.uhabits.R
import org.isoron.uhabits.core.tasks.Task

internal class NotesCard(context: Context?, attrs: AttributeSet?) : HabitCard(context, attrs) {

    private val notesTextView: TextView

    init {
        View.inflate(getContext(), R.layout.show_habit_notes, this)
        notesTextView = findViewById(R.id.habitNotes)
    }

    override fun refreshData() {
        notesTextView.text = habit.description
        visibility = if(habit.description.isEmpty()) View.GONE else View.VISIBLE
        notesTextView.visibility = visibility
    }

    override fun createRefreshTask(): Task = error("refresh task should never be called.")
}