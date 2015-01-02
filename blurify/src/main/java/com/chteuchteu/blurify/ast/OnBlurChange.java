package com.chteuchteu.blurify.ast;

import android.os.AsyncTask;
import android.widget.SeekBar;

import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.BlurUtil;
import com.chteuchteu.blurify.hlpr.CustomImageView;
import com.chteuchteu.blurify.ui.Activity_Main;

public class OnBlurChange extends AsyncTask<Void, Integer, Void> {
	private Activity_Main activity;
	private SeekBar seekBar;
	private SeekBar selectiveFocusSize;
	private int progress;
	private boolean selectiveFocus;

	private boolean success;


	public OnBlurChange(Activity_Main activity, SeekBar seekBar, SeekBar selectiveFocusSize,
	                    boolean selectiveFocus) {
		this.activity = activity;
		this.seekBar = seekBar;
		this.selectiveFocus = selectiveFocus;
		this.selectiveFocusSize = selectiveFocusSize;
	}

	@Override
	protected void onPreExecute() {
		progress = seekBar.getProgress();
		seekBar.setEnabled(false);
		activity.findViewById(R.id.seekBar).setEnabled(false);
		activity.findViewById(R.id.saveimg).setEnabled(false);
		activity.findViewById(R.id.saveimg).setAlpha(0.8f);
		activity.findViewById(R.id.setWallpaper).setEnabled(false);
		activity.findViewById(R.id.setWallpaper).setAlpha(0.8f);
		activity.findViewById(R.id.selectiveFocusSwitch).setEnabled(false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		success = false;

		try {
			blur();
			success = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO Crashlytics
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		activity.findViewById(R.id.seekBar).setEnabled(true);
		activity.findViewById(R.id.saveimg).setEnabled(true);
		activity.findViewById(R.id.saveimg).setAlpha(1f);
		activity.findViewById(R.id.setWallpaper).setEnabled(true);
		activity.findViewById(R.id.setWallpaper).setAlpha(1f);
		activity.findViewById(R.id.selectiveFocusSwitch).setEnabled(true);
		activity.findViewById(R.id.selectiveFocusSize).setEnabled(true);

		if (success)
			activity.updateContainer(new CustomImageView.AfterNextDrawListener() {
				@Override
				public void onDrawCalled() {
					activity.computing = false;
				}
			});
	}

	private void blur() {
		if (progress == 0)
			activity.little_bitmap = activity.little_bitmap_original.copy(activity.little_bitmap_original.getConfig(), true);
		else {
			// RenderScript blur goes from 0 to 25
			float renderScriptProgress = progress / 4;

			if (selectiveFocus) {
				float maskSize = ((float) selectiveFocusSize.getProgress()) / 100;
				activity.little_bitmap = BlurUtil.maskBlur(activity, activity.little_bitmap_original, renderScriptProgress, maskSize);
			}
			else
				activity.little_bitmap = BlurUtil.renderScriptBlur(activity, activity.little_bitmap_original, renderScriptProgress);
		}
	}
}
