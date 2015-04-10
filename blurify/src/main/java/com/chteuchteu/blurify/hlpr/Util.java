package com.chteuchteu.blurify.hlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.blurify.R;

import java.io.File;
import java.io.FileOutputStream;

public class Util {
	public enum CustomFont {
		RobotoMedium("Roboto-Medium.ttf");

		private String fileName;
		CustomFont(String fileName) { this.fileName = fileName; }
		public String getFileName() { return this.fileName; }
	}

	public static void setFont(Context context, ViewGroup group, CustomFont customFont) {
		Typeface typeFace = Typeface.createFromAsset(context.getAssets(), customFont.getFileName());
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView) {
				((TextView) v).setTypeface(typeFace);
			} else if (v instanceof ViewGroup)
				setFont(context, (ViewGroup) v, customFont);
		}
	}

	public static int getStatusBarHeight(Context c) {
		int result = 0;
		int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			result = c.getResources().getDimensionPixelSize(resourceId);
		return result;
	}
	
	@SuppressLint("NewApi")
	public static int getSoftbuttonsbarHeight(Activity activity) {
		// getRealMetrics is only available with API 17 and +
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int usableHeight = metrics.heightPixels;
			activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			int realHeight = metrics.heightPixels;
			if (realHeight > usableHeight)
				return realHeight - usableHeight;
			else
				return 0;
		}
		return 0;
	}

	public static void saveBitmap(Context context, Bitmap bitmap) {
		String root = Environment.getExternalStorageDirectory().toString();
		File dir = new File(root + "/blurify/");
		if(!dir.exists() || !dir.isDirectory()) {
			if (!dir.mkdir())
				return;
		}

		String fileName1 = "Photo";
		String fileName2 = "01.png";
		File file = new File(dir, fileName1 + fileName2);
		int i = 1; 	String i_s;
		while (file.exists()) {
			if (i<99) {
				if (i<10)	i_s = "0" + i;
				else		i_s = "" + i;
				fileName2 = i_s + ".png";
				file = new File(dir, fileName1 + fileName2);
				i++;
			}
			else
				break;
		}
		if (file.exists()) {
			if (!file.delete())
				return;
		}

		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			String filePath = Environment.getExternalStorageDirectory() + "/blurify/" + fileName1 + fileName2;
			MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);
			Toast.makeText(context, context.getString(R.string.photo_saved_as) + " " + fileName1 + fileName2 + "!", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(context, context.getString(R.string.error_save_photo), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
		if (Build.VERSION.SDK_INT < 16)
			v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		else
			v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	}

	public static String getAppVersion(Context context) {
		String versionName;
		try {
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "";
		}
		return versionName;
	}

    public static final class Animations {
        public static void fadeIn(View view) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(200);
            view.startAnimation(fadeIn);
        }

        public static void fadeOut(View view) {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(200);
            view.startAnimation(fadeOut);
        }
    }

    public static class Pref {
        public enum PrefKeys {
            I18NDialogShown("i18n_dialog_shown");

            private String key;
            PrefKeys(String k) { this.key = k; }

            public String getKey() { return this.key; }
        }

        public static boolean getBool(Context context, PrefKeys key, boolean defaultVal) {
            return context.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getBoolean(key.getKey(), defaultVal);
        }

        public static void setBool(Context context, PrefKeys key, boolean value) {
            SharedPreferences prefs = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key.getKey(), value);
            editor.apply();
        }
    }
}
