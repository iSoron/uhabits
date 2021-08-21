/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.widgets

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.RemoteViews
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.preferences.WidgetPreferences
import org.isoron.uhabits.intents.PendingIntentFactory
import kotlin.math.max

abstract class BaseWidget(val context: Context, val id: Int, val stacked: Boolean) {
    private val widgetPrefs: WidgetPreferences
    protected val prefs: Preferences
    protected val pendingIntentFactory: PendingIntentFactory
    protected val commandRunner: CommandRunner
    private var dimensions: WidgetDimensions

    fun delete() {
        widgetPrefs.removeWidget(id)
    }

    val landscapeRemoteViews: RemoteViews
        get() = getRemoteViews(
            dimensions.landscapeWidth,
            dimensions.landscapeHeight
        )

    abstract fun getOnClickPendingIntent(context: Context): PendingIntent?
    val portraitRemoteViews: RemoteViews
        get() = getRemoteViews(
            dimensions.portraitWidth,
            dimensions.portraitHeight
        )

    abstract fun refreshData(widgetView: View)
    fun setDimensions(dimensions: WidgetDimensions) {
        this.dimensions = dimensions
    }

    protected abstract fun buildView(): View?
    protected abstract val defaultHeight: Int
    protected abstract val defaultWidth: Int
    private fun adjustRemoteViewsPadding(
        remoteViews: RemoteViews,
        view: View,
        width: Int,
        height: Int
    ) {
        val imageWidth = view.measuredWidth
        val imageHeight = view.measuredHeight
        val p = calculatePadding(width, height, imageWidth, imageHeight)
        remoteViews.setViewPadding(R.id.buttonOverlay, p[0], p[1], p[2], p[3])
    }

    private fun buildRemoteViews(
        view: View,
        remoteViews: RemoteViews,
        width: Int,
        height: Int
    ) {
        val bitmap = getBitmapFromView(view)
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap)
        adjustRemoteViewsPadding(remoteViews, view, width, height)
        val onClickIntent = getOnClickPendingIntent(context)
        if (onClickIntent != null) remoteViews.setOnClickPendingIntent(R.id.button, onClickIntent)
    }

    private fun calculatePadding(
        entireWidth: Int,
        entireHeight: Int,
        imageWidth: Int,
        imageHeight: Int
    ): IntArray {
        val w = ((entireWidth.toFloat() - imageWidth) / 2).toInt()
        val h = ((entireHeight.toFloat() - imageHeight) / 2).toInt()
        return intArrayOf(w, h, w, h)
    }

    private fun getBitmapFromView(view: View): Bitmap {
        view.invalidate()
        val width = max(1, view.measuredWidth)
        val height = max(1, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    protected open fun getRemoteViews(width: Int, height: Int): RemoteViews {
        val view = buildView()!!
        measureView(view, width, height)
        refreshData(view)
        if (view.isLayoutRequested) measureView(view, width, height)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_wrapper)
        buildRemoteViews(view, remoteViews, width, height)
        return remoteViews
    }

    private fun measureView(view: View, w: Int, h: Int) {
        var width = w
        var height = h
        val inflater = LayoutInflater.from(context)
        val entireView = inflater.inflate(R.layout.widget_wrapper, null)
        var specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        var specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        entireView.measure(specWidth, specHeight)
        entireView.layout(
            0,
            0,
            entireView.measuredWidth,
            entireView.measuredHeight
        )
        val imageView = entireView.findViewById<View>(R.id.imageView)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        view.measure(specWidth, specHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    protected val preferedBackgroundAlpha: Int
        get() {
            return if (stacked)
                255
            else
                prefs.widgetOpacity
        }

    init {
        val app = context.applicationContext as HabitsApplication
        widgetPrefs = app.component.widgetPreferences
        prefs = app.component.preferences
        commandRunner = app.component.commandRunner
        pendingIntentFactory = app.component.pendingIntentFactory
        dimensions = WidgetDimensions(
            defaultWidth,
            defaultHeight,
            defaultWidth,
            defaultHeight
        )
    }
}
