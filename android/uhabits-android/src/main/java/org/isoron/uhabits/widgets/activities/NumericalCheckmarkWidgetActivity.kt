package org.isoron.uhabits.widgets.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import org.isoron.uhabits.R
import android.content.Context
import android.os.Bundle
import android.view.Window
import org.isoron.androidbase.AppContextModule
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.common.dialogs.NumberPickerFactory
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.core.ui.widgets.NumericalCheckmarkWidgetBehavior
import org.isoron.uhabits.intents.IntentFactory
import org.isoron.uhabits.intents.IntentParser
import org.isoron.uhabits.intents.PendingIntentFactory
import org.isoron.uhabits.widgets.WidgetUpdater

class NumericalCheckmarkWidgetActivity : Activity() {

    private lateinit var behavior : NumericalCheckmarkWidgetBehavior
    private var data: IntentParser.CheckmarkIntentData? = null
    private lateinit var widgetUpdater : WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.widget_checkmark_number_screen)
        val app = this.applicationContext as HabitsApplication
        val component = app.component
        val parser = app.component.intentParser
        data = parser.parseCheckmarkIntent(intent)
        behavior = NumericalCheckmarkWidgetBehavior(component.habitList,component.commandRunner)
        widgetUpdater = component.widgetUpdater
        showNumberSelector(this)
    }

    class CallBackReceiver : ListHabitsBehavior.NumberPickerCallback{

        private val activity : NumericalCheckmarkWidgetActivity

        constructor(activity: NumericalCheckmarkWidgetActivity){
            this.activity=activity
        }

        override fun onNumberPicked(newValue: Double) {
            activity.saveNewNumer(newValue)
        }
    }

    fun saveNewNumer(newValue: Double){
        behavior.setNumericValue(data!!.habit,data!!.timestamp,(newValue*1000).toInt())
        widgetUpdater.updateWidgets()
        finish()
    }


    fun showNumberSelector(context: Context) {
        var localData = data
        if(behavior!=null && localData!=null) {//if a blank screen shows up without a popup when pressing the widget, you should check if this check passes.
            val numberPickerFactory = NumberPickerFactory(context)
            numberPickerFactory.create( data!!.habit.checkmarks.today!!.value.toDouble()/1000, "This is a test", CallBackReceiver(this)).show()
        }
    }

    companion object{
        const val ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY = "org.isoron.uhabits.ACTION_SHOW_NUMERICAL_VALUE_ACTIVITY"
    }
}