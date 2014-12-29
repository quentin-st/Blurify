package com.chteuchteu.blurify;

import android.util.Log;

public class Foofy {
	public static void log(String txt) {
		if (BuildConfig.DEBUG)
			Log.i("Foofy.log", txt);
	}
}
