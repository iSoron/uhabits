/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
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

package org.isoron.uhabits.notifications;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.app.NotificationCompat.*;

import org.isoron.androidbase.*;
import org.isoron.uhabits.R;
import org.isoron.uhabits.core.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.preferences.*;
import org.isoron.uhabits.core.ui.*;
import org.isoron.uhabits.intents.*;

import javax.inject.*;

import static android.graphics.BitmapFactory.decodeResource;
import static org.isoron.uhabits.notifications.RingtoneManager.getRingtoneUri;

@AppScope
public class AndroidNotificationTray implements NotificationTray.SystemTray
{
    @NonNull
    private final Context context;

    @NonNull
    private final PendingIntentFactory pendingIntents;

    @NonNull
    private final Preferences preferences;

    @Inject
    public AndroidNotificationTray(@AppContext @NonNull Context context,
                                   @NonNull PendingIntentFactory pendingIntents,
                                   @NonNull Preferences preferences)
    {
        this.context = context;
        this.pendingIntents = pendingIntents;
        this.preferences = preferences;
    }

    @Override
    public void removeNotification(int id)
    {
        NotificationManagerCompat.from(context).cancel(id);
    }

    public void showNotification(@NonNull Habit habit,
                                 int notificationId,
                                 long timestamp,
                                 long reminderTime)
    {
        Action checkAction = new Action(R.drawable.ic_action_check,
            context.getString(R.string.check),
            pendingIntents.addCheckmark(habit, timestamp));

        Action snoozeAction = new Action(R.drawable.ic_action_snooze,
            context.getString(R.string.snooze),
            pendingIntents.snoozeNotification(habit));

        Bitmap wearableBg =
            decodeResource(context.getResources(), R.drawable.stripe);

        // Even though the set of actions is the same on the phone and
        // on the watch, Pebble requires us to add them to the
        // WearableExtender.
        WearableExtender wearableExtender = new WearableExtender()
            .setBackground(wearableBg)
            .addAction(checkAction)
            .addAction(snoozeAction);

        Notification notification = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habit.getName())
            .setContentText(habit.getDescription())
            .setContentIntent(pendingIntents.showHabit(habit))
            .setDeleteIntent(pendingIntents.dismissNotification(habit))
            .addAction(checkAction)
            .addAction(snoozeAction)
            .setSound(getRingtoneUri(context))
            .extend(wearableExtender)
            .setWhen(reminderTime)
            .setShowWhen(true)
            .setOngoing(preferences.shouldMakeNotificationsSticky())
            .build();

        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(
                Activity.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notification);
    }
}
