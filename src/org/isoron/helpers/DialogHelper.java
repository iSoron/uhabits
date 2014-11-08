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
		public void onSaved(Command command);
	}

	public static void showSoftKeyboard(View view)
	{
		InputMethodManager imm = (InputMethodManager)
				view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}
}
