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
import android.view.View
import org.isoron.uhabits.widgets.views.EmptyWidgetView

class EmptyWidget(
    context: Context,
    widgetId: Int,
    stacked: Boolean = false,
) : BaseWidget(context, widgetId, stacked) {
    override val defaultHeight: Int = 200
    override val defaultWidth: Int = 200

    override fun getOnClickPendingIntent(context: Context): PendingIntent? = null
    override fun refreshData(v: View) {}
    override fun buildView() = EmptyWidgetView(context)
}
