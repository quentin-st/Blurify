package com.chteuchteu.blurify.hlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.blurify.R;

import java.io.File;
import java.io.FileOutputStream;

public class Util {
	public static void setFont(ViewGroup group, Typeface font) {
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
				((TextView) v).setTypeface(font);
			} else if (v instanceof ViewGroup)
				setFont((ViewGroup) v, font);
		}
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
	
	public static int getStatusBarHeight(Activity activity) {
		int result = 0;
		int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			result = activity.getResources().getDimensionPixelSize(resourceId);
		return result;
	}

	public static void saveBitmap(Context context, Bitmap bitmap) {
		String root = Environment.getExternalStorageDirectory().toString();
		File dir = new File(root + "/blurify/");
		if(!dir.exists() || !dir.isDirectory())
			dir.mkdir();

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
		if (file.exists())
			file.delete();

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
}
