package com.chteuchteu.blurify.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.chteuchteu.blurify.Foofy;
import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.Util;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

public class BlurifyActivity extends AppCompatActivity {
	protected boolean isAboutShown;
	protected Context context;
	protected Toolbar toolbar;
	private List<MenuItem> menuItems;

	protected void onCreate(Bundle savedStateInstance) {
		super.onCreate(savedStateInstance);
		Crashlytics.start(this);
		context = this;
		isAboutShown = false;
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

		Util.setFont(this, (ViewGroup) findViewById(R.id.main_container), Util.CustomFont.RobotoMedium);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		menuItems = new ArrayList<>();
		menuItems.add(menu.findItem(R.id.menu_about));
		menuItems.add(menu.findItem(R.id.menu_contribute));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				closeAbout();
				return true;
			case R.id.menu_about:
				about();
				return true;
			case R.id.menu_contribute:
				new AlertDialogWrapper.Builder(this)
						.setTitle(R.string.contribute)
						.setMessage(R.string.contribute_text)
						.setPositiveButton(R.string.contribute_go, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chteuchteu/Blurify"));
								startActivity(browserIntent);
							}
						})
						.show();
				return true;
		}
		return true;
	}

	public void about() {
		isAboutShown = true;
		View container = findViewById(R.id.aboutContainer);
		container.setVisibility(View.VISIBLE);
        Util.Animations.fadeIn(container);
		Util.setFont(this, (ViewGroup) container, Util.CustomFont.RobotoMedium);

		TextView version = (TextView) findViewById(R.id.about_version);
		version.setText(Util.getAppVersion(this));

		toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeAbout();
			}
		});

		for (MenuItem item : menuItems)
			item.setVisible(false);
	}

	public void closeAbout() {
		View container = findViewById(R.id.aboutContainer);
		container.setVisibility(View.GONE);
        Util.Animations.fadeOut(container);
		isAboutShown = false;

		toolbar.setNavigationIcon(null);
		for (MenuItem item : menuItems)
			item.setVisible(true);
	}

	@Override
	protected void onDestroy() {
		Foofy.getInstance().destroyRenderScriptContext();
        super.onDestroy();
	}
}
