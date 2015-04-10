package com.chteuchteu.blurify.ast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.chteuchteu.blurify.R;
import com.tjeannin.apprate.AppRate;

public class WallpaperSetter extends AsyncTask<Void, Integer, Void> {
	private Activity activity;
	private WallpaperManager wallpaperManager;
	private Bitmap bitmap;
	private boolean success;


	public WallpaperSetter(Activity activity, WallpaperManager wallpaperManager, Bitmap bitmap) {
		this.activity = activity;
		this.wallpaperManager = wallpaperManager;
		this.bitmap = bitmap;
	}

	@Override
	protected void onPreExecute() {
		activity.findViewById(R.id.seekBar).setEnabled(false);
		activity.findViewById(R.id.saveimg).setEnabled(false);
		activity.findViewById(R.id.setWallpaper).setEnabled(false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		success = false;

		try {
			wallpaperManager.setBitmap(bitmap);
			success = true;
		} catch (Exception e) {
			success = false;
		}


		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (success)
			Toast.makeText(activity, activity.getString(R.string.wallpaper_set), Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(activity, activity.getString(R.string.wallpaper_error), Toast.LENGTH_SHORT).show();

		activity.findViewById(R.id.seekBar).setEnabled(true);
		activity.findViewById(R.id.saveimg).setEnabled(true);
		activity.findViewById(R.id.setWallpaper).setEnabled(true);

		// Ask to note the app
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
                if (activity.isFinishing())
                    return;
                
				AlertDialog.Builder builder = new AlertDialog.Builder(activity)
						.setTitle(activity.getText(R.string.note))
						.setIcon(R.drawable.launcher_icon)
						.setMessage(activity.getText(R.string.note_txt))
						.setPositiveButton(activity.getText(R.string.yes), null)
						.setNegativeButton(activity.getText(R.string.no), null)
						.setNeutralButton(activity.getText(R.string.notnow), null);
				new AppRate(activity)
						.setCustomDialog(builder)
						.setMinDaysUntilPrompt(3)
						.setMinLaunchesUntilPrompt(4)
						.init();
			}
		}, 2500);
	}
}
