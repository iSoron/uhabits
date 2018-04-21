package com.activeandroid.util;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public final class Log {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static String sTag = "ActiveAndroid";
	private static boolean sEnabled = false;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	private Log() {
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

    public static boolean isEnabled() {
        return sEnabled;
    }

	public static void setEnabled(boolean enabled) {
		sEnabled = enabled;
	}

	public static boolean isLoggingEnabled() {
		return sEnabled;
	}

	public static int v(String msg) {
		if (sEnabled) {
			return android.util.Log.v(sTag, msg);
		}
		return 0;
	}

	public static int v(String tag, String msg) {
		if (sEnabled) {
			return android.util.Log.v(tag, msg);
		}
		return 0;
	}

	public static int v(String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.v(sTag, msg, tr);
		}
		return 0;
	}

	public static int v(String tag, String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.v(tag, msg, tr);
		}
		return 0;
	}

	public static int d(String msg) {
		if (sEnabled) {
			return android.util.Log.d(sTag, msg);
		}
		return 0;
	}

	public static int d(String tag, String msg) {
		if (sEnabled) {
			return android.util.Log.d(tag, msg);
		}
		return 0;
	}

	public static int d(String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.d(sTag, msg, tr);
		}
		return 0;
	}

	public static int d(String tag, String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.d(tag, msg, tr);
		}
		return 0;
	}

	public static int i(String msg) {
		if (sEnabled) {
			return android.util.Log.i(sTag, msg);
		}
		return 0;
	}

	public static int i(String tag, String msg) {
		if (sEnabled) {
			return android.util.Log.i(tag, msg);
		}
		return 0;
	}

	public static int i(String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.i(sTag, msg, tr);
		}
		return 0;
	}

	public static int i(String tag, String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.i(tag, msg, tr);
		}
		return 0;
	}

	public static int w(String msg) {
		if (sEnabled) {
			return android.util.Log.w(sTag, msg);
		}
		return 0;
	}

	public static int w(String tag, String msg) {
		if (sEnabled) {
			return android.util.Log.w(tag, msg);
		}
		return 0;
	}

	public static int w(String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.w(sTag, msg, tr);
		}
		return 0;
	}

	public static int w(String tag, String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.w(tag, msg, tr);
		}
		return 0;
	}

	public static int e(String msg) {
		if (sEnabled) {
			return android.util.Log.e(sTag, msg);
		}
		return 0;
	}

	public static int e(String tag, String msg) {
		if (sEnabled) {
			return android.util.Log.e(tag, msg);
		}
		return 0;
	}

	public static int e(String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.e(sTag, msg, tr);
		}
		return 0;
	}

	public static int e(String tag, String msg, Throwable tr) {
		if (sEnabled) {
			return android.util.Log.e(tag, msg, tr);
		}
		return 0;
	}

	public static int t(String msg, Object... args) {
		if (sEnabled) {
			return android.util.Log.v("test", String.format(msg, args));
		}
		return 0;
	}
}