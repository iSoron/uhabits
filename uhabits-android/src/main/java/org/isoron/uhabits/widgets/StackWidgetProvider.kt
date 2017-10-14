package org.isoron.uhabits.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import org.isoron.uhabits.R

/**
 * Created by victoryu on 9/30/17.
 */

class StackWidgetProvider : BaseWidgetProvider() {
    override fun getWidgetFromId(context: Context, id: Int): StackGroupWidget {
        return StackGroupWidget(context, id)
    }
}
