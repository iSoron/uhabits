/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.isoron.uhabits.R;

public abstract class DialogHelper
{

    private static Typeface fontawesome;

    public interface OnSavedListener
    {
        void onSaved(Command command, Object savedObject);
    }

    public static void showSoftKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void vibrate(Context context, int duration)
    {
        Vibrator vb = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(duration);
    }


    public static void incrementLaunchCount(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int count = prefs.getInt("launch_count", 0);
        prefs.edit().putInt("launch_count", count + 1).apply();
    }

    public static int getLaunchCount(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("launch_count", 0);
    }
}
