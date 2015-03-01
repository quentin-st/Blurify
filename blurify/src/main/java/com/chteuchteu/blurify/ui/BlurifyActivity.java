package com.chteuchteu.blurify.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.chteuchteu.blurify.Foofy;
import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.Util;

public class BlurifyActivity extends ActionBarActivity {
	protected Context context;

	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);
		context = this;

	}

	protected void onContentViewSet() {
		// Load Foofy instance
		Foofy.getInstance();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, Util.getSoftbuttonsbarHeight(this));
			}
		}

		Util.setFont((ViewGroup) findViewById(R.id.main_container), Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf"));
	}

	@Override
	protected void onDestroy() {
		Foofy.getInstance().destroyRenderScriptContext();
	}
}
