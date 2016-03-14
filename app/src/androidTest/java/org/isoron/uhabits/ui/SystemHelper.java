package org.isoron.uhabits.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.test.runner.AndroidJUnitRunner;
import android.util.Log;

import java.lang.reflect.Method;

public final class SystemHelper extends AndroidJUnitRunner
{
    private static final String ANIMATION_PERMISSION = "android.permission.SET_ANIMATION_SCALE";
    private static final float DISABLED = 0.0f;
    private static final float DEFAULT = 1.0f;

    private final Context context;

    SystemHelper(Context context)
    {
        this.context = context;
    }

    void unlockScreen()
    {
        try
        {
            KeyguardManager mKeyGuardManager = (KeyguardManager) context
                    .getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock mLock = mKeyGuardManager.newKeyguardLock("lock");
            mLock.disableKeyguard();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void disableAllAnimations()
    {
        Log.i("SystemAnimations", "Trying to disable animations");
        int permStatus = context.checkCallingOrSelfPermission(ANIMATION_PERMISSION);
        if (permStatus == PackageManager.PERMISSION_GRANTED)
            setSystemAnimationsScale(DISABLED);
        else
            Log.e("SystemAnimations", "Permission denied");

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
            Log.i("SystemAnimations", "All animations successfully disabled");
        }
        catch (Exception e)
        {
            Log.e("SystemAnimations",
                    "Could not change animation scale to " + animationScale + " :'(");
        }
    }
}