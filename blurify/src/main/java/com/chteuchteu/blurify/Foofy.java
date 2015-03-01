package com.chteuchteu.blurify;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

public class Foofy {
	private RenderScript renderScriptContext;

	private static Foofy instance;

	public static synchronized Foofy getInstance() {
		if (instance == null)
			instance = new Foofy();

		return instance;
	}
	private Foofy() {
		this.renderScriptContext = null;
	}


	public synchronized RenderScript getRenderScriptContext(Context context) {
		if (renderScriptContext == null) {
			Foofy.log("Creating RenderScript context");
			renderScriptContext = RenderScript.create(context);
		}

		return renderScriptContext;
	}


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
