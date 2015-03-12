package com.android.datetimepicker;

import android.app.Service;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;

/**
 * A simple utility class to handle haptic feedback.
 */
public class HapticFeedbackController {
    private static final int VIBRATE_DELAY_MS = 125;
    private static final int VIBRATE_LENGTH_MS = 5;

    private static boolean checkGlobalSetting(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1;
    }

    private final Context mContext;
    private final ContentObserver mContentObserver;

    private Vibrator mVibrator;
    private boolean mIsGloballyEnabled;
    private long mLastVibrate;

    public HapticFeedbackController(Context context) {
        mContext = context;
        mContentObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                mIsGloballyEnabled = checkGlobalSetting(mContext);
            }
        };
    }

    /**
     * Call to setup the controller.
     */
    public void start() {
        mVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);

        // Setup a listener for changes in haptic feedback settings
        mIsGloballyEnabled = checkGlobalSetting(mContext);
        Uri uri = Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED);
        mContext.getContentResolver().registerContentObserver(uri, false, mContentObserver);
    }

    /**
     * Call this when you don't need the controller anymore.
     */
    public void stop() {
        mVibrator = null;
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
    }

    /**
     * Try to vibrate. To prevent this becoming a single continuous vibration, nothing will
     * happen if we have vibrated very recently.
     */
    public void tryVibrate() {
        if (mVibrator != null && mIsGloballyEnabled) {
            long now = SystemClock.uptimeMillis();
            // We want to try to vibrate each individual tick discretely.
            if (now - mLastVibrate >= VIBRATE_DELAY_MS) {
                mVibrator.vibrate(VIBRATE_LENGTH_MS);
                mLastVibrate = now;
            }
        }
    }
}
