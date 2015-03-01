package com.chteuchteu.blurify.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.chteuchteu.blurify.Foofy;
import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.Util;

public class BlurifyActivity extends ActionBarActivity {
	protected Context context;
	protected Toolbar toolbar;

	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);
		context = this;
	}

	protected void onContentViewSet() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setBackgroundColor(Color.TRANSPARENT);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);

		// Load Foofy instance
		Foofy.getInstance(context);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				int softButtonsBarHeight = Util.getSoftbuttonsbarHeight(this);
				int statusBarHeight = Util.getStatusBarHeight(this);
				findViewById(R.id.statusBarBackground).setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, statusBarHeight));
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, softButtonsBarHeight);
			}
		}

		Util.setFont((ViewGroup) findViewById(R.id.main_container), Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf"));
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_about:

				return true;
			case R.id.menu_contribute:

				return true;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		Foofy.getInstance().destroyRenderScriptContext();
	}
}
