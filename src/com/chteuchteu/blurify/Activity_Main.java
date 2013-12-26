package com.chteuchteu.blurify;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;
import com.enrique.stackblur.StackBlurManager;

public class Activity_Main extends Activity {
	private static Bitmap tmp_original_bitmap;
	private static Bitmap little_bitmap_original;
	private static Bitmap little_bitmap;
	private static int previous_step = 0;
	public static boolean success;
	private static int progress = 0;
	private static int aspectRatioX = 0;
	private static int aspectRatioY = 0;
	private static int state = 0;
	
	private static int ST_UNKNWOWN = 0;
	private static int ST_CROP = 1;
	private static int ST_BLUR = 2;
	
	private static Button set_wallpaper;
	final Handler uiThreadCallback = new Handler();
	StackBlurManager _stackBlurManager;
	WallpaperManager myWallpaperManager;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().hide();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, getSoftbuttonsbarHeight());
			}
		}
		
		setFont((ViewGroup) findViewById(R.id.main_container), Typeface.createFromAsset(getAssets(), "RobotoCondensed-Regular.ttf"));
		Activity_Main.set_wallpaper = (Button)findViewById(R.id.setWallpaper);
		myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
		aspectRatioX = myWallpaperManager.getDesiredMinimumWidth();
		aspectRatioY = myWallpaperManager.getDesiredMinimumHeight();
		
		/*if (savedInstanceState != null) {
			if (savedInstanceState.getByteArray("little_bitmap_original") != null)
				little_bitmap_original = BitmapFactory.decodeByteArray(savedInstanceState.getByteArray("little_bitmap_original"), 0, savedInstanceState.getByteArray("little_bitmap_original").length);
			if (savedInstanceState.getByteArray("little_bitmap") != null)
				little_bitmap = BitmapFactory.decodeByteArray(savedInstanceState.getByteArray("little_bitmap"), 0, savedInstanceState.getByteArray("little_bitmap").length);
			state = savedInstanceState.getInt("state");
			previous_step = savedInstanceState.getInt("previous_step");
			progress = savedInstanceState.getInt("progress");
			aspectRatioX = savedInstanceState.getInt("aspectRatioX");
			aspectRatioY = savedInstanceState.getInt("aspectRatioY");
		}
		else*/
		state = ST_UNKNWOWN;
		
		set_wallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View actualView) {
				if (Activity_Main.little_bitmap != null) {
					((SeekBar)findViewById(R.id.seekBar)).setEnabled(false);
					((Button)findViewById(R.id.saveimg)).setEnabled(false);
					((Button)findViewById(R.id.setWallpaper)).setEnabled(false);
					
					final Runnable runInUIThread = new Runnable() {
						public void run() {
							if (success)
								Toast.makeText(Activity_Main.this, getString(R.string.wallpaper_set), Toast.LENGTH_SHORT).show();
							else
								Toast.makeText(Activity_Main.this, getString(R.string.wallpaper_error), Toast.LENGTH_SHORT).show();
							
							((SeekBar)findViewById(R.id.seekBar)).setEnabled(true);
							((Button)findViewById(R.id.saveimg)).setEnabled(true);
							((Button)findViewById(R.id.setWallpaper)).setEnabled(true);
						}
					};
					new Thread() {
						@Override public void run() {
							try {
								myWallpaperManager.setBitmap(little_bitmap);
								Activity_Main.success = true;
							} catch (Exception e) {
								Activity_Main.success = false;
							}
							uiThreadCallback.post(runInUIThread);
						}
					}.start();
				}
			}
		});
		
		((Button)findViewById(R.id.saveimg)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String root = Environment.getExternalStorageDirectory().toString();
				File dir = new File(root + "/blurify/");
				if(!dir.exists() || !dir.isDirectory())
					dir.mkdir();
				
				String fileName1 = "Photo";
				String fileName2 = "01.png";
				File file = new File(dir, fileName1 + fileName2);
				int i = 1; 	String i_s = "";
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
					little_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
					out.flush();
					out.close();
					String filePath = Environment.getExternalStorageDirectory() + "/blurify/" + fileName1 + fileName2;  
					MediaScannerConnection.scanFile(Activity_Main.this, new String[] { filePath }, null, null);
					Toast.makeText(Activity_Main.this, getString(R.string.photo_saved_as) + " " + fileName1 + fileName2 + "!", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Toast.makeText(Activity_Main.this, getString(R.string.error_save_photo), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
		
		((Button)findViewById(R.id.getimg)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View actualView) {
				Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(pickPhoto , 1);
			}
		});
		
		((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (Activity_Main.little_bitmap_original != null) {
					Activity_Main.progress = seekBar.getProgress();
					seekBar.setEnabled(false);
					((Button)findViewById(R.id.saveimg)).setEnabled(false);
					((Button)findViewById(R.id.setWallpaper)).setEnabled(false);
					
					final Runnable runInUIThread = new Runnable() {
						public void run() {
							((SeekBar)findViewById(R.id.seekBar)).setEnabled(true);
							((Button)findViewById(R.id.saveimg)).setEnabled(true);
							((Button)findViewById(R.id.setWallpaper)).setEnabled(true);
							updateBackground();
						}
					};
					new Thread() {
						@Override public void run() {
							try {
								if (Activity_Main.little_bitmap != null)
									Activity_Main.little_bitmap.recycle();
								Activity_Main.little_bitmap = null;
								
								if (progress == 0)
									Activity_Main.little_bitmap = Activity_Main.little_bitmap_original.copy(Activity_Main.little_bitmap_original.getConfig(), true);
								else {
									//Activity_Main.little_bitmap = fastblur(Activity_Main.little_bitmap_original, Activity_Main.progress);
									_stackBlurManager = new StackBlurManager(Activity_Main.little_bitmap_original);
									_stackBlurManager.process(progress);
									Activity_Main.little_bitmap = _stackBlurManager.returnBlurredImage();
								}
								Activity_Main.previous_step = Activity_Main.progress;
								uiThreadCallback.post(runInUIThread);
							} catch (Exception ex) {
								try {
									if (Activity_Main.little_bitmap != null)
										Activity_Main.little_bitmap.recycle();
									Activity_Main.little_bitmap = null;
									
									if (progress == 0)
										Activity_Main.little_bitmap = Activity_Main.little_bitmap_original.copy(Activity_Main.little_bitmap_original.getConfig(), true);
									else
										Activity_Main.little_bitmap = fastblur(Activity_Main.little_bitmap_original, Activity_Main.progress);
									Activity_Main.previous_step = Activity_Main.progress;
									uiThreadCallback.post(runInUIThread);
								} catch (Exception ex2) {
									((SeekBar)findViewById(R.id.seekBar)).setProgress(Activity_Main.previous_step);
								}
							}
						}
					}.start();
				}
			}
		});
		
		
		Bitmap b = null;
		
		// Premier redimentionnement de original_bitmap
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper, options);
		
		long totalImagePixels = options.outHeight * options.outWidth;
		
		// Get screen pixels
		int totalScreenPixels = 0;
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			totalScreenPixels = size.x * size.y;
		} else {
			Display display = getWindowManager().getDefaultDisplay();
			totalScreenPixels = display.getHeight() * display.getWidth();
		}
		
		if (totalScreenPixels > 2048*2048)
			totalScreenPixels = 2048*2048;
		
		if (totalImagePixels > totalScreenPixels) {    
			double factor=(float)totalImagePixels/(float)(totalScreenPixels);
			int sampleSize=(int) Math.pow(2, Math.floor(Math.sqrt(factor)));
			options.inJustDecodeBounds=false;
			options.inSampleSize=sampleSize;
			b = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper, options);
		} else {
			b = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);
		}
		
		if (b != null) {
			Activity_Main.little_bitmap = b.copy(b.getConfig(), true);
			b.recycle();
			b = null;
			updateBackground();
		}
	}
	
	/*protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (little_bitmap_original != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			little_bitmap_original.compress(Bitmap.CompressFormat.PNG, 100, stream);
			outState.putByteArray("little_bitmap_original", stream.toByteArray());
			//try {	little_bitmap_original.recycle(); } catch (Exception ignored) { }
			//try {	stream.close(); } catch (Exception ignored) { }
		}
		if (little_bitmap != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			little_bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			outState.putByteArray("little_bitmap", stream.toByteArray());
			//try {	little_bitmap.recycle(); } catch (Exception ignored) { }
			//try {	stream.close(); } catch (Exception ignored) { }
		}
		outState.putInt("state", state);
		outState.putInt("previous_step", previous_step);
		outState.putInt("progress", progress);
		outState.putInt("aspectRatioX", aspectRatioX);
		outState.putInt("aspectRatioY", aspectRatioY);
	}*/
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) // Translucent available
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, getSoftbuttonsbarHeight());
		}
		View cropImageView = findViewById(R.id.CropImageView);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			params.setMargins(0, 0, 0, getSoftbuttonsbarHeight() + findViewById(R.id.buttonsContainer).getHeight());
		else
			params.setMargins(0, 0, 0, findViewById(R.id.buttonsContainer).getHeight());
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		cropImageView.setLayoutParams(params);
	}
	
	@Override
	public void onBackPressed() {
		if (state == ST_UNKNWOWN) {
			super.onBackPressed();
		} else if (state == ST_CROP) {
			findViewById(R.id.getimg).setVisibility(View.VISIBLE);
			findViewById(R.id.actions1).setVisibility(View.GONE);
			((CropImageView)findViewById(R.id.CropImageView)).setVisibility(View.GONE);
			state = ST_UNKNWOWN;
		} else if (state == ST_BLUR) {
			((SeekBar)findViewById(R.id.seekBar)).setProgress(0);
			((ImageView)findViewById(R.id.container)).setVisibility(View.GONE);
			findViewById(R.id.mask).setVisibility(View.VISIBLE);
			findViewById(R.id.CropImageView).setVisibility(View.VISIBLE);
			((ImageView)findViewById(R.id.tmp_container)).setVisibility(View.VISIBLE);
			findViewById(R.id.actions1).setVisibility(View.VISIBLE);
			findViewById(R.id.actions2).setVisibility(View.GONE);
			state = ST_CROP;
			launchCrop();
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		
		if (requestCode == 1 && resultCode == RESULT_OK) {
			state = ST_CROP;
			
			((SeekBar)findViewById(R.id.seekBar)).setProgress(0);
			
			Uri selectedImage = imageReturnedIntent.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};
			
			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
			
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();
			
			Bitmap b = null;
			
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inJustDecodeBounds=true;
			
			BitmapFactory.decodeFile(filePath, options);
			
			long totalImagePixels=options.outHeight*options.outWidth;
			
			// Get screen pixels
			int totalScreenPixels = 0;
			if (android.os.Build.VERSION.SDK_INT >= 13) {
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				totalScreenPixels = size.x * size.y;
			} else {
				Display display = getWindowManager().getDefaultDisplay();
				totalScreenPixels = display.getHeight() * display.getWidth();
			}
			
			if (totalScreenPixels > 2048*2048)
				totalScreenPixels = 2048*2048;
			
			if (totalImagePixels > totalScreenPixels) {    
				double factor=(float)totalImagePixels/(float)(totalScreenPixels);
				int sampleSize=(int) Math.pow(2, Math.floor(Math.sqrt(factor)));
				options.inJustDecodeBounds=false;
				options.inSampleSize=sampleSize;
				b = BitmapFactory.decodeFile(filePath, options);
			}
			
			if (b == null) b = BitmapFactory.decodeFile(filePath);
			
			
			if (b != null && b.getConfig() != null) {
				/*//Activity_Main.little_bitmap = b.copy(b.getConfig(), true);
				Activity_Main.little_bitmap_original = b.copy(b.getConfig(), true);
				b.recycle();
				b = null;
				updateBackground();*/
				
				/*int x = 0, y = 0;
				if (android.os.Build.VERSION.SDK_INT >= 13) {
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					x = size.x;
					y = size.y;
				} else {
					Display display = getWindowManager().getDefaultDisplay();
					x = display.getHeight();
					y = display.getWidth();
				}
				//b = scaleCenterCrop(b, x, y);
				
				Activity_Main.little_bitmap = scaleCenterCrop(BitmapFactory.decodeFile(filePath), x, y);
				Activity_Main.little_bitmap_original = Activity_Main.little_bitmap.copy(Activity_Main.little_bitmap.getConfig(), true);*/
				try {
					tmp_original_bitmap = b.copy(b.getConfig(), true);
					b.recycle();
					b = null;
					
					findViewById(R.id.getimg).setVisibility(View.GONE);
					findViewById(R.id.actions1).setVisibility(View.VISIBLE);
					launchCrop();
					((ImageView)findViewById(R.id.container)).setImageBitmap(null);
				} catch (Exception ignored) {
					Toast.makeText(Activity_Main.this, getString(R.string.err_import), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(Activity_Main.this, getString(R.string.err_import), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void launchCrop() {
		findViewById(R.id.mask).setVisibility(View.VISIBLE);
		final CropImageView c = (CropImageView)findViewById(R.id.CropImageView);
		c.setFixedAspectRatio(true);
		c.setAspectRatio(aspectRatioX, aspectRatioY);
		c.setVisibility(View.VISIBLE);
		c.setImageBitmap(tmp_original_bitmap);
		c.invalidate();
		new BlurAndBackgroundBitmap().execute();
		
		((Button)findViewById(R.id.crop)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				little_bitmap_original = c.getCroppedImage();
				AlphaAnimation a2 = new AlphaAnimation(1.0f, 0.0f);
				a2.setDuration(500);
				Activity_Main.little_bitmap = Activity_Main.little_bitmap_original.copy(Activity_Main.little_bitmap_original.getConfig(), true);
				((ImageView)findViewById(R.id.container)).setVisibility(View.VISIBLE);
				findViewById(R.id.mask).startAnimation(a2);
				findViewById(R.id.mask).setVisibility(View.GONE);
				findViewById(R.id.CropImageView).startAnimation(a2);
				findViewById(R.id.CropImageView).setVisibility(View.GONE);
				((ImageView)findViewById(R.id.container)).setImageBitmap(little_bitmap_original);
				((ImageView)findViewById(R.id.tmp_container)).setImageBitmap(null);
				((ImageView)findViewById(R.id.tmp_container)).setVisibility(View.GONE);
				findViewById(R.id.actions1).setVisibility(View.GONE);
				findViewById(R.id.actions2).setVisibility(View.VISIBLE);
				updateBackground();
				state = ST_BLUR;
			}
		});
		((Button)findViewById(R.id.rotate)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((CropImageView)findViewById(R.id.CropImageView)).rotateImage(90);
			}
		});
	}
	
	public void updateBackground() {
		try {
			if (Activity_Main.little_bitmap != null && !Activity_Main.little_bitmap.isRecycled())
				((ImageView)findViewById(R.id.container)).setImageBitmap(Activity_Main.little_bitmap);
		} catch (Exception ex) { }
	}
	
	public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		
		// Compute the scaling factors to fit the new height and width, respectively.
		// To cover the final image, the final scaling will be the bigger 
		// of these two.
		float xScale = (float) newWidth / sourceWidth;
		float yScale = (float) newHeight / sourceHeight;
		float scale = Math.max(xScale, yScale);
		
		// Now get the size of the source bitmap when scaled
		float scaledWidth = scale * sourceWidth;
		float scaledHeight = scale * sourceHeight;
		
		// Let's find out the upper left coordinates if the scaled bitmap
		// should be centered in the new size give by the parameters
		float left = (newWidth - scaledWidth) / 2;
		float top = (newHeight - scaledHeight) / 2;
		
		// The target rectangle for the new, scaled version of the source bitmap will now
		// be
		RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
		
		// Finally, we create a new bitmap of the specified size and draw our new,
		// scaled bitmap onto it.
		Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
		Canvas canvas = new Canvas(dest);
		canvas.drawBitmap(source, null, targetRect, null);
		
		return dest;
	}
	
	public void setFont(ViewGroup group, Typeface font) {
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
	
	public class BlurAndBackgroundBitmap extends AsyncTask<Void, Integer, Void> {
		Bitmap b2 = null;
		@Override
		protected Void doInBackground(Void... params) {
			Bitmap b;
			try {
				_stackBlurManager = new StackBlurManager(Activity_Main.tmp_original_bitmap);
				_stackBlurManager.process(10);
				b = _stackBlurManager.returnBlurredImage();
			} catch (Exception ex) {
				b = fastblur(Activity_Main.tmp_original_bitmap, 10);
			}
			b2 = Bitmap.createScaledBitmap(b, b.getWidth()/2, b.getHeight()/2, false);
			b.recycle(); b = null;
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
			a.setDuration(1000);
			ImageView i = (ImageView)findViewById(R.id.tmp_container);
			i.setImageBitmap(b2);
			i.startAnimation(a);
			i.setVisibility(View.VISIBLE);
		}
	}
	
	public static Bitmap fastblur(Bitmap sentBitmap, int radius) {
		
		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012
		
		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
		
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		
		if (radius < 1) {
			return (null);
		}
		
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		int[] pix = new int[w * h];
		//Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}
		
		yw = yi = 0;
		
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
			sir[1] = (p & 0x00ff00) >> 8;
		sir[2] = (p & 0x0000ff);
		rbs = r1 - Math.abs(i);
		rsum += sir[0] * rbs;
		gsum += sir[1] * rbs;
		bsum += sir[2] * rbs;
		if (i > 0) {
			rinsum += sir[0];
			ginsum += sir[1];
			binsum += sir[2];
		} else {
			routsum += sir[0];
			goutsum += sir[1];
			boutsum += sir[2];
		}
			}
			stackpointer = radius;
			
			for (x = 0; x < w; x++) {
				
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];
				
				sir[0] = (p & 0xff0000) >> 16;
			sir[1] = (p & 0x00ff00) >> 8;
			sir[2] = (p & 0x0000ff);
			
			rinsum += sir[0];
			ginsum += sir[1];
			binsum += sir[2];
			
			rsum += rinsum;
			gsum += ginsum;
			bsum += binsum;
			
			stackpointer = (stackpointer + 1) % div;
			sir = stack[(stackpointer) % div];
			
			routsum += sir[0];
			goutsum += sir[1];
			boutsum += sir[2];
			
			rinsum -= sir[0];
			ginsum -= sir[1];
			binsum -= sir[2];
			
			yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				
				sir = stack[i + radius];
				
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				
				rbs = r1 - Math.abs(i);
				
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
				
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];
				
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];
				
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				
				yi += w;
			}
		}
		
		//Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		
		return (bitmap);
	}
	
	@SuppressLint("NewApi")
	private int getSoftbuttonsbarHeight() {
		// getRealMetrics is only available with API 17 and +
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int usableHeight = metrics.heightPixels;
			getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			int realHeight = metrics.heightPixels;
			if (realHeight > usableHeight)
				return realHeight - usableHeight;
			else
				return 0;
		}
		return 0;
	}
}
