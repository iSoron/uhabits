package org.isoron.uhabits.utils;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;


public class SystemUtils
{
    public static boolean isAndroidOOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static void unlockScreen(Activity activity)
    {
        if (isAndroidOOrLater()) {
            KeyguardManager km =
                    (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(activity, null);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }
}
