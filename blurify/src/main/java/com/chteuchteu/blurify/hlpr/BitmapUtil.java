package com.chteuchteu.blurify.hlpr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;

public class BitmapUtil {
	/**
	 * Applies mask to source bitmap. Returns a bitmap with the size
	 * of the mask (or lower)
	 * @param source Source bitmap
	 * @param mask Bitmap mask
	 * @param maskPosX X pos of the mask
	 * @param maskPosY Y pos of the mask
	 * @return Bitmap
	 */
	public static Bitmap applyMask(Bitmap source, Bitmap mask, int maskPosX, int maskPosY) {
		int maskWidth = mask.getWidth();
		int maskHeight = mask.getHeight();

		// Copy the original bitmap
		Bitmap bitmap;
		if (source.isMutable()) {
			bitmap = source;
		} else {
			bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
			source.recycle();
		}
		bitmap.setHasAlpha(true);

		if (maskPosX < 0)
			maskPosX = 0;
		if (maskPosY < 0)
			maskPosY = 0;

		// If the mask is larger than the source, resize the mask
		if (mask.getWidth() > source.getWidth() || mask.getHeight() > source.getHeight())
			mask = resizeBitmap(mask, source.getWidth(), source.getHeight());

		// Crop bitmap to fit mask
		bitmap = Bitmap.createBitmap(bitmap, maskPosX, maskPosY, maskWidth, maskHeight);

		// Apply mask
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(mask, 0, 0, paint);

		mask.recycle();
		return bitmap;
	}

	public static Bitmap resizeBitmap(Bitmap source, int maxWidth, int maxHeight) {
		int outWidth;
		int outHeight;
		int inWidth = source.getWidth();
		int inHeight = source.getHeight();
		if (inWidth > inHeight) {
			outWidth = maxWidth;
			outHeight = (inHeight * maxWidth) / inWidth;
		} else {
			outHeight = maxHeight;
			outWidth = (inWidth * maxHeight) / inHeight;
		}

		return Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
	}

	public static Bitmap getDrawableAsBitmap(Context context, int drawable) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			options.inMutable = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Resources res = context.getResources();
		return BitmapFactory.decodeResource(res, drawable, options);
	}

	public static int getDominantColor(Bitmap bitmap) {
		try {
			Bitmap onePixelBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
			return onePixelBitmap.getPixel(0,0);
		} catch (Exception ex) {
			return Color.BLACK;
		}
	}
}
