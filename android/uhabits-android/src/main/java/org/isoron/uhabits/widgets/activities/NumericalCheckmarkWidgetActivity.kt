package org.isoron.uhabits.widgets.activities

import android.app.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.FrameLayout
import org.isoron.uhabits.*
import org.isoron.uhabits.activities.common.dialogs.*
import org.isoron.uhabits.core.ui.screens.habits.list.*
import org.isoron.uhabits.core.ui.widgets.*
import org.isoron.uhabits.intents.*
import org.isoron.uhabits.widgets.*

class NumericalCheckmarkWidgetActivity : Activity(), ListHabitsBehavior.NumberPickerCallback {

    private lateinit var behavior: WidgetBehavior
    private lateinit var data: IntentParser.CheckmarkIntentData
    private lateinit var widgetUpdater: WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(FrameLayout(this))
        val app = this.applicationContext as HabitsApplication
        val component = app.component
        val parser = app.component.intentParser
        data = parser.parseCheckmarkIntent(intent)
        behavior = WidgetBehavior(component.habitList, component.commandRunner, component.notificationTray)
        widgetUpdater = component.widgetUpdater
        showNumberSelector(this)
    }


    override fun onNumberPicked(newValue: Double) {
        behavior.setNumericValue(data!!.habit, data!!.timestamp, (newValue * 1000).toInt())
        widgetUpdater.updateWidgets()
        finish()
    }

    fun showNumberSelector(context: Context) {
        var localData = data
        if (behavior != null && localData != null) {//if a blank screen shows up without a popup when pressing the widget, you should check if this check passes.
            val numberPickerFactory = NumberPickerFactory(context)
            numberPickerFactory.create(data!!.habit.checkmarks.today!!.value.toDouble() / 1000, "This is a test", this).show()
        }
    }

    companion object {
        const val ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY = "org.isoron.uhabits.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY"
    }
}