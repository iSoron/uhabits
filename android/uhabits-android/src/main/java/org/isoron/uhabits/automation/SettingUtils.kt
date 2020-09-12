package org.isoron.uhabits.automation

import android.content.Intent
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList

object SettingUtils {
    @JvmStatic
    fun parseIntent(intent: Intent, allHabits: HabitList): Arguments? {
        val bundle = intent.getBundleExtra(EXTRA_BUNDLE) ?: return null
        val action = bundle.getInt("action")
        if (action < 0 || action > 4) return null
        val habit = allHabits.getById(bundle.getLong("habit")) ?: return null
        return Arguments(action, habit)
    }

    class Arguments(var action: Int, var habit: Habit)
}