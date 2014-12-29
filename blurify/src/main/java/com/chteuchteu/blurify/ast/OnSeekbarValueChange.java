package com.chteuchteu.blurify.ast;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.SeekBar;

import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.hlpr.Util;
import com.chteuchteu.blurify.ui.Activity_Main;
import com.enrique.stackblur.StackBlurManager;

public class OnSeekbarValueChange extends AsyncTask<Void, Integer, Void> {
	private Activity_Main activity;
	private SeekBar seekBar;
	private Bitmap little_bitmap;
	private Bitmap little_bitmap_original;
	private int progress;
	private int previousProgress;

	private boolean success;

	private enum BlurMethod { STACK_BLUR, FAST_BLUR, FASTER_BLUR }


	public OnSeekbarValueChange(Activity_Main activity, SeekBar seekBar, Bitmap little_bitmap,
	                            Bitmap little_bitmap_original) {
		this.activity = activity;
		this.seekBar = seekBar;
		this.little_bitmap = little_bitmap;
		this.little_bitmap_original = little_bitmap_original;
	}

	@Override
	protected void onPreExecute() {
		progress = seekBar.getProgress();
		previousProgress = progress;
		seekBar.setEnabled(false);
		activity.findViewById(R.id.saveimg).setEnabled(false);
		activity.findViewById(R.id.setWallpaper).setEnabled(false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		success = false;

		try {
			blur(BlurMethod.STACK_BLUR);
			success = true;
		} catch (Exception ex) {
			try {
				blur(BlurMethod.FAST_BLUR);
				success = true;
			} catch (Exception ex2) {
				try {
					blur(BlurMethod.FASTER_BLUR);
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
			activity.findViewById(R.id.setWallpaper).setEnabled(true);
			activity.updateContainer();
		}
		else
			seekBar.setProgress(previousProgress);
	}

	private void blur(BlurMethod blurMethod) {
		if (little_bitmap != null)
			little_bitmap.recycle();
		little_bitmap = null;

		if (progress == 0)
			little_bitmap = little_bitmap_original.copy(little_bitmap_original.getConfig(), true);
		else {
			switch (blurMethod) {
				case STACK_BLUR:
					StackBlurManager _stackBlurManager = new StackBlurManager(little_bitmap_original);
					_stackBlurManager.process(progress);
					little_bitmap = _stackBlurManager.returnBlurredImage();
					break;
				case FAST_BLUR:
					little_bitmap = Util.fastblur(little_bitmap_original, progress);
					break;
				case FASTER_BLUR:
					break;
			}
		}
	}
}
