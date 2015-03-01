package com.chteuchteu.blurify;

import android.util.Log;

public class Foofy {
	public static void log(String txt) {
		log(txt, Log.DEBUG);
	}
	public static void log(String txt, int logLevel) {
		if (BuildConfig.DEBUG) {
			switch (logLevel) {
				case Log.DEBUG: Log.d("Blurify", txt); break;
				case Log.ERROR: Log.e("Blufiry", txt); break;
				case Log.INFO: Log.i("Blurify", txt); break;
				case Log.VERBOSE: Log.v("Blurify", txt); break;
				case Log.WARN: Log.w("Blurify", txt); break;
				default: log(txt, Log.INFO); break;
			}
		}
	}
}
