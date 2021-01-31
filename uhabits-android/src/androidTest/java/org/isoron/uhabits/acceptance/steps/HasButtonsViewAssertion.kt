package org.isoron.uhabits.acceptance.steps

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.isoron.uhabits.activities.habits.list.views.HabitCardView

class HasButtonsViewAssertion(private val buttons: List<Int>) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }

        if (view !is HabitCardView) {
            throw IllegalStateException("Not an HabitCardView.")
        }

        for (p in view.checkmarkPanel.buttons zip buttons) {
            assertThat("", p.first.value, equalTo(p.second))
        }
    }
}
