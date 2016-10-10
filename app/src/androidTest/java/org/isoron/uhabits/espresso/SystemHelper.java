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

package org.isoron.uhabits.espresso;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.test.runner.AndroidJUnitRunner;
import android.util.Log;

import java.lang.reflect.Method;

public final class SystemHelper extends AndroidJUnitRunner
{
    private static final String ANIMATION_PERMISSION = "android.permission.SET_ANIMATION_SCALE";
    private static final float DISABLED = 0.0f;
    private static final float DEFAULT = 1.0f;

    private final Context context;
    private PowerManager.WakeLock wakeLock;

    SystemHelper(Context context)
    {
        this.context = context;
    }

    void acquireWakeLock()
    {
        PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, getClass().getSimpleName());
        wakeLock.acquire();
    }

    void releaseWakeLock()
    {
        if(wakeLock != null)
            wakeLock.release();
    }

    void unlockScreen()
    {
        Log.i("SystemHelper", "Trying to unlock screen");
        try
        {
            KeyguardManager mKeyGuardManager =
                    (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock mLock = mKeyGuardManager.newKeyguardLock("lock");
            mLock.disableKeyguard();
            Log.e("SystemHelper", "Successfully unlocked screen");
        } catch (Exception e)
        {
            Log.e("SystemHelper", "Could not unlock screen");
            e.printStackTrace();
        }
    }

    void disableAllAnimations()
    {
        Log.i("SystemHelper", "Trying to disable animations");
        int permStatus = context.checkCallingOrSelfPermission(ANIMATION_PERMISSION);
        if (permStatus == PackageManager.PERMISSION_GRANTED) setSystemAnimationsScale(DISABLED);
        else Log.e("SystemHelper", "Permission denied");

    }

    void enableAllAnimations()
    {
        int permStatus = context.checkCallingOrSelfPermission(ANIMATION_PERMISSION);
        if (permStatus == PackageManager.PERMISSION_GRANTED)
        {
            setSystemAnimationsScale(DEFAULT);
        }
    }

    private void setSystemAnimationsScale(float animationScale)
    {
        try
        {
            Class<?> windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
            Method asInterface =
                    windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);
            Class<?> serviceManagerClazz = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);
            Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");
            Method setAnimationScales =
                    windowManagerClazz.getDeclaredMethod("setAnimationScales", float[].class);
            Method getAnimationScales = windowManagerClazz.getDeclaredMethod("getAnimationScales");

            IBinder windowManagerBinder = (IBinder) getService.invoke(null, "window");
            Object windowManagerObj = asInterface.invoke(null, windowManagerBinder);
            float[] currentScales = (float[]) getAnimationScales.invoke(windowManagerObj);
            for (int i = 0; i < currentScales.length; i++)
                currentScales[i] = animationScale;

            setAnimationScales.invoke(windowManagerObj, new Object[]{currentScales});
            Log.i("SystemHelper", "All animations successfully disabled");
        }
        catch (Exception e)
        {
            Log.e("SystemHelper", "Could not change animation scale to " + animationScale + " :'(");
        }
    }
}