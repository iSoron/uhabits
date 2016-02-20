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
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public abstract class DialogHelper
{

    //	public static AlertDialog alert(Activity context, String title, String message, OnClickListener positiveClickListener) {
    //		return new AlertDialog.Builder(context)
    //			.setTitle(title)
    //			.setMessage(message)
    //			.setPositiveButton(android.R.string.yes, positiveClickListener)
    //			.setNegativeButton(android.R.string.no, null).show();
    //	}

    public static abstract class SimpleClickListener implements OnClickListener
    {
        public abstract void onClick();

        public void onClick(DialogInterface dialog, int whichButton)
        {
            onClick();
        }
    }

    public static interface OnSavedListener
    {
        public void onSaved(Command command, Object savedObject);
    }

    public static void showSoftKeyboard(View view)
    {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
