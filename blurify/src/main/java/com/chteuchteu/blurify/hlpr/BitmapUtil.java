package com.chteuchteu.blurify.hlpr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.chteuchteu.blurify.Foofy;

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
		if (source.isMutable())
			bitmap = source;
		else {
			bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
			if (!source.isRecycled())
				source.recycle();
		}
		bitmap.setHasAlpha(true);

		/*if (maskPosX < 0)
			maskPosX = 0;
		if (maskPosY < 0)
			maskPosY = 0;*/

		// If the mask is larger than the source, resize the mask
		if (mask.getWidth() > bitmap.getWidth() || mask.getHeight() > bitmap.getHeight())
			mask = resizeBitmap(mask, bitmap.getWidth(), bitmap.getHeight());

		/* If the mask goes outside the picture, we have to create a new bitmap
		 * to avoid bitmap creation failure
		 * (maskPosX >= 0 && maskPosX + maskWidth <= bitmapWidth constraint)
		 */
		// Crop bitmap to fit mask
		if (maskPosX < 0 || maskPosX + maskWidth > bitmap.getWidth()
				|| maskPosY < 0 || maskPosY + maskHeight > bitmap.getHeight()) {
			Foofy.log("(bitmap is overlapping)");

			// Create a blank bitmap which will contain the piece of mask
			Bitmap blankBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888);

			// Compute dimensions of the piece of bitmap inside the mask
			// Width
			int w;
			if (maskPosX >= 0 && maskPosX + maskWidth <= bitmap.getWidth())
				w = maskWidth;
			else if (maskPosX < 0)
				w = maskWidth + maskPosX;
			else // maskPosX + maskWidth > bitmap.getWidth()
				w = bitmap.getWidth() - maskPosX;

			// Height
			int h;
			if (maskPosY >= 0 && maskPosY + maskHeight <= bitmap.getHeight())
				h = maskHeight;
			else if (maskPosY < 0)
				h = maskHeight + maskPosY;
			else // maskPosY + maskHeight > bitmap.getHeight()
				h = bitmap.getHeight() - maskPosY;


			int pieceDelX = maskPosX < 0 ? 0 : maskPosX;
			int pieceDelY = maskPosY < 0 ? 0 : maskPosY;
			Bitmap pieceOfMask = Bitmap.createBitmap(bitmap, pieceDelX, pieceDelY, w, h);

			// Put the piece on the blankBitmap
			// Get position of the piece inside the canvas
			int cx = maskPosX < 0 ? -maskPosX : 0;
			int cy = maskPosY < 0 ? -maskPosY : 0;

			Canvas canvas = new Canvas(blankBitmap);
			canvas.drawBitmap(pieceOfMask, cx, cy, new Paint());

			bitmap = blankBitmap;
		} else
			bitmap = Bitmap.createBitmap(bitmap, maskPosX, maskPosY, maskWidth, maskHeight);
		// Finished cropping bitmap

		// Apply mask
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(mask, 0, 0, paint);

		if (!mask.isRecycled())
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

	public static int[] mapBitmapCoordinatesFromImageView(int posX, int posY, ImageView imageView) {
		Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

		int ivW = imageView.getWidth();
		int ivH = imageView.getHeight();
		int bW = bitmap.getWidth();
		int bH = bitmap.getHeight();

		int newX = posX * bW / ivW;
		int newH = posY * bH / ivH;

		return new int[] { newX, newH };
	}

	public static void recycle(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled())
			bitmap.recycle();
	}

	public static class Click {
		/**
		 * Returns the coordinates of the click on a bitmap inside an ImageView
		 * @param posX X pos of the click
		 * @param posY Y pos of the click
		 * @param imageView ImageView
		 * @return [0] = X, [1] = y
		 */
		public static int[] mapCoordinates(int posX, int posY, ImageView imageView) {
			Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
			int[] values = new int[2];

			int ivW = imageView.getWidth();
			int ivH = imageView.getHeight();
			int bW = bitmap.getWidth();
			int bH = bitmap.getHeight();

			float imageViewRatio = ((float) imageView.getWidth()) / imageView.getHeight();
			float bitmapRatio = ((float) bitmap.getWidth()) / bitmap.getHeight();

			int[] bitmapPosition = getBitmapPositionInsideImageView(imageView);
			int left = bitmapPosition[0];
			int top = bitmapPosition[1];
			int width = bitmapPosition[2];
			int height = bitmapPosition[3];

			// Check if click has been made outside the bitmap
			if (posX < left)
				return null;
			else if (posX > left + width)
				return null;
			else if (posY < top)
				return null;
			else if (posY > top + height)
				return null;

			// Different possible cases for bitmap inside ImageView
			if (bitmapRatio < imageViewRatio) {
				// There's space on left and right
				values[0] = posX * width / ivW;
				values[1] = posY * bH / ivH;
			} else if (bitmapRatio > imageViewRatio) {
				// There's space on top and bottom
				values[0] = posX * bW / ivW;
				values[1] = (posY * height - top) / ivH;
			} else if (bitmapRatio == imageViewRatio) {
				// There's no space left
				// Simple coordinates proportion
				values[0] = posX * bW / ivW;
				values[1] = posY * bH / ivH;
			}

			return values;
		}

		/**
		 * Returns the bitmap position inside an imageView.
		 * @param imageView source ImageView
		 * @return 0: left, 1: top, 2: width, 3: height
		 */
		public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
			int[] ret = new int[4];

			if (imageView == null || imageView.getDrawable() == null)
				return ret;

			// Get image dimensions
			// Get image matrix values and place them in an array
			float[] f = new float[9];
			imageView.getImageMatrix().getValues(f);

			// Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
			final float scaleX = f[Matrix.MSCALE_X];
			final float scaleY = f[Matrix.MSCALE_Y];

			// Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
			final Drawable d = imageView.getDrawable();
			final int origW = d.getIntrinsicWidth();
			final int origH = d.getIntrinsicHeight();

			// Calculate the actual dimensions
			final int actW = Math.round(origW * scaleX);
			final int actH = Math.round(origH * scaleY);

			ret[2] = actW;
			ret[3] = actH;

			// Get image position
			// We assume that the image is centered into ImageView
			int imgViewW = imageView.getWidth();
			int imgViewH = imageView.getHeight();

			int top = (int) (imgViewH - actH)/2;
			int left = (int) (imgViewW - actW)/2;

			ret[0] = left;
			ret[1] = top;

			return ret;
		}
	}
}
