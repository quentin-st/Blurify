package com.chteuchteu.blurify.ast;

import android.os.AsyncTask;
import android.widget.SeekBar;

import com.chteuchteu.blurify.Foofy;
import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.BlurUtil;
import com.chteuchteu.blurify.ui.Activity_Main;
import com.enrique.stackblur.StackBlurManager;

public class OnSeekbarValueChange extends AsyncTask<Void, Integer, Void> {
	private Activity_Main activity;
	private SeekBar seekBar;
	private int progress;
	private int previousProgress;

	private boolean success;

	private enum BlurMethod { RENDERSCRIPT, STACK_BLUR, FAST_BLUR }


	public OnSeekbarValueChange(Activity_Main activity, SeekBar seekBar) {
		this.activity = activity;
		this.seekBar = seekBar;
	}

	@Override
	protected void onPreExecute() {
		progress = seekBar.getProgress();
		previousProgress = progress;
		seekBar.setEnabled(false);
		activity.findViewById(R.id.saveimg).setEnabled(false);
		activity.findViewById(R.id.setWallpaper).setEnabled(false);
		activity.findViewById(R.id.saveimg).setAlpha(0.8f);
		activity.findViewById(R.id.setWallpaper).setEnabled(false);
		activity.findViewById(R.id.setWallpaper).setAlpha(0.8f);
	}

	@Override
	protected Void doInBackground(Void... params) {
		success = false;

		try {
			blur(BlurMethod.RENDERSCRIPT);
			success = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				blur(BlurMethod.STACK_BLUR);
				success = true;
			} catch (Exception ex2) {
				ex.printStackTrace();
				try {
					blur(BlurMethod.FAST_BLUR);
					success = true;
				} catch (Exception ex3) { ex3.printStackTrace(); }
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (success) {
			activity.findViewById(R.id.seekBar).setEnabled(true);
			activity.findViewById(R.id.saveimg).setEnabled(true);
			activity.findViewById(R.id.saveimg).setAlpha(1f);
			activity.findViewById(R.id.setWallpaper).setEnabled(true);
			activity.findViewById(R.id.setWallpaper).setAlpha(1f);
			activity.updateContainer();
		}
		else
			seekBar.setProgress(previousProgress);
	}

	private void blur(BlurMethod blurMethod) {
		Foofy.log("Bluring image with method " + blurMethod.name());

		if (activity.little_bitmap != null)
			activity.little_bitmap.recycle();
		activity.little_bitmap = null;

		if (progress == 0)
			activity.little_bitmap = activity.little_bitmap_original.copy(activity.little_bitmap_original.getConfig(), true);
		else {
			switch (blurMethod) {
				case RENDERSCRIPT:
					// RenderScript blur goes from 0 to 25
					float renderScriptProgress = progress * 25 / 100;
					activity.little_bitmap = BlurUtil.renderScriptBlur(activity.little_bitmap_original, activity, renderScriptProgress);
					break;
				case STACK_BLUR:
					StackBlurManager _stackBlurManager = new StackBlurManager(activity.little_bitmap_original);
					_stackBlurManager.process(progress);
					activity.little_bitmap = _stackBlurManager.returnBlurredImage();
					break;
				case FAST_BLUR:
					activity.little_bitmap = BlurUtil.fastBlur(activity.little_bitmap_original, progress);
					break;
			}
		}
	}
}
