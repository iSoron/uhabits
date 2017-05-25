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

package org.isoron.uhabits.pebble;

import android.content.*;
import android.support.annotation.*;
import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;

import com.getpebble.android.kit.*;
import com.getpebble.android.kit.util.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.receivers.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.*;

import static com.getpebble.android.kit.Constants.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.IsEqual.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class PebbleReceiverTest extends BaseAndroidTest
{

    private Habit habit1;

    private Habit habit2;

    @Override
    public void setUp()
    {
        super.setUp();

        fixtures.purgeHabits(habitList);

        habit1 = fixtures.createEmptyHabit();
        habit1.setName("Exercise");

        habit2 = fixtures.createEmptyHabit();
        habit2.setName("Meditate");
    }

    @Test
    public void testCount() throws Exception
    {
        onPebbleReceived((dict) -> {
            assertThat(dict.getString(0), equalTo("COUNT"));
            assertThat(dict.getInteger(1), equalTo(2L));
        });

        PebbleDictionary dict = buildCountRequest();
        sendFromPebbleToAndroid(dict);
        awaitLatch();
    }

    @Test
    public void testFetch() throws Exception
    {
        onPebbleReceived((dict) -> {
            assertThat(dict.getString(0), equalTo("HABIT"));
            assertThat(dict.getInteger(1), equalTo(habit2.getId()));
            assertThat(dict.getString(2), equalTo(habit2.getName()));
            assertThat(dict.getInteger(3), equalTo(0L));
        });

        PebbleDictionary dict = buildFetchRequest(1);
        sendFromPebbleToAndroid(dict);
        awaitLatch();
    }

//    @Test
//    public void testToggle() throws Exception
//    {
//        int v = habit1.getCheckmarks().getTodayValue();
//        assertThat(v, equalTo(Checkmark.UNCHECKED));
//
//        onPebbleReceived((dict) -> {
//            assertThat(dict.getString(0), equalTo("OK"));
//            int value = habit1.getCheckmarks().getTodayValue();
//            assertThat(value, equalTo(200)); //Checkmark.CHECKED_EXPLICITLY));
//        });
//
//        PebbleDictionary dict = buildToggleRequest(habit1.getId());
//        sendFromPebbleToAndroid(dict);
//        awaitLatch();
//    }

    @NonNull
    protected PebbleDictionary buildCountRequest()
    {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "COUNT");
        return dict;
    }

    @NonNull
    protected PebbleDictionary buildFetchRequest(int position)
    {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "FETCH");
        dict.addInt32(1, position);
        return dict;
    }

    protected void onPebbleReceived(PebbleProcessor processor)
    {
        BroadcastReceiver pebbleReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                try
                {
                    String jsonData = intent.getStringExtra(MSG_DATA);
                    PebbleDictionary dict = PebbleDictionary.fromJson(jsonData);
                    processor.process(dict);
                    latch.countDown();
                    targetContext.unregisterReceiver(this);
                }
                catch (JSONException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Constants.INTENT_APP_SEND);
        targetContext.registerReceiver(pebbleReceiver, filter);
    }

    protected void sendFromPebbleToAndroid(PebbleDictionary dict)
    {
        Intent intent = new Intent(Constants.INTENT_APP_RECEIVE);
        intent.putExtra(Constants.APP_UUID, PebbleReceiver.WATCHAPP_UUID);
        intent.putExtra(Constants.TRANSACTION_ID, 0);
        intent.putExtra(Constants.MSG_DATA, dict.toJsonString());
        targetContext.sendBroadcast(intent);
    }

    private PebbleDictionary buildToggleRequest(long habitId)
    {
        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "TOGGLE");
        dict.addInt32(1, (int) habitId);
        return dict;
    }

    interface PebbleProcessor
    {
        void process(PebbleDictionary dict);
    }
}
