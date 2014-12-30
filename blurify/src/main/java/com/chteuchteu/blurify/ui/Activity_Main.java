package com.chteuchteu.blurify.ui;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.chteuchteu.blurify.R;
import com.chteuchteu.blurify.ast.BlurBackgroundBitmap;
import com.chteuchteu.blurify.ast.OnSeekbarValueChange;
import com.chteuchteu.blurify.ast.WallpaperSetter;
import com.chteuchteu.blurify.hlpr.Util;
import com.edmodo.cropper.CropImageView;

public class Activity_Main extends ActionBarActivity {
	private Bitmap tmp_original_bitmap;
	public Bitmap little_bitmap_original;
	public Bitmap little_bitmap;

	private SeekBar seekBar;

	private int aspectRatioX = 0;
	private int aspectRatioY = 0;
	private int state = 0;

	private Activity_Main activity;
	private Context context;
	
	private static final int ST_UNKNWOWN = 0;
	private static final int ST_CROP = 1;
	private static final int ST_BLUR = 2;

	private WallpaperManager myWallpaperManager;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		activity = this;

		getSupportActionBar().hide();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, Util.getSoftbuttonsbarHeight(this));
				findViewById(R.id.ad_container).setPadding(0, Util.getStatusBarHeight(this), 0, 0);
			}
		}
		
		Util.setFont((ViewGroup) findViewById(R.id.main_container), Typeface.createFromAsset(getAssets(), "Roboto-Medium.ttf"));
		Button set_wallpaper = (Button) findViewById(R.id.setWallpaper);
		myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
		aspectRatioX = myWallpaperManager.getDesiredMinimumWidth();
		aspectRatioY = myWallpaperManager.getDesiredMinimumHeight();
		
		state = ST_UNKNWOWN;
		
		set_wallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View actualView) {
				if (little_bitmap != null)
					new WallpaperSetter(activity, myWallpaperManager, little_bitmap).execute();
			}
		});
		
		findViewById(R.id.saveimg).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.saveBitmap(context, little_bitmap);
			}
		});
		
		findViewById(R.id.getimg).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View actualView) {
				Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(pickPhoto , 1);
			}
		});

		seekBar = (SeekBar)findViewById(R.id.seekBar);
		seekBar.setMax(100);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (little_bitmap_original != null)
					new OnSeekbarValueChange(activity, seekBar).execute();
			}
		});
		AppLovinSdk.initializeSdk(context);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) // Translucent available
				findViewById(R.id.buttonsContainer).setPadding(0, 0, 0, Util.getSoftbuttonsbarHeight(this));
		}
		View cropImageView = findViewById(R.id.CropImageView);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			params.setMargins(0, 0, 0, Util.getSoftbuttonsbarHeight(this) + findViewById(R.id.buttonsContainer).getHeight());
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
			findViewById(R.id.container).setVisibility(View.GONE);
			findViewById(R.id.CropImageView).setVisibility(View.GONE);
			state = ST_UNKNWOWN;
		} else if (state == ST_BLUR) {
			seekBar.setProgress(0);
			findViewById(R.id.container).setVisibility(View.GONE);
			findViewById(R.id.mask).setVisibility(View.VISIBLE);
			findViewById(R.id.CropImageView).setVisibility(View.VISIBLE);
			findViewById(R.id.blurryBackground).setVisibility(View.VISIBLE);
			findViewById(R.id.blurryBackground_darkMask).setVisibility(View.VISIBLE);
			findViewById(R.id.actions1).setVisibility(View.VISIBLE);
			findViewById(R.id.actions2).setVisibility(View.GONE);
			state = ST_CROP;
			launchCrop(false);
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		
		if (requestCode == 1 && resultCode == RESULT_OK) {
			state = ST_CROP;
			
			seekBar.setProgress(0);
			
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
			int totalScreenPixels;
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
				try {
					tmp_original_bitmap = b.copy(b.getConfig(), true);
					b.recycle();
					
					findViewById(R.id.getimg).setVisibility(View.GONE);
					findViewById(R.id.actions1).setVisibility(View.VISIBLE);
					launchCrop(true);
					((ImageView)findViewById(R.id.container)).setImageBitmap(null);
				} catch (Exception ignored) {
					Toast.makeText(context, getString(R.string.err_import), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(context, getString(R.string.err_import), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	// No need to execute blur background when pressing back
	private void launchCrop(boolean executeBlurBackground) {
		findViewById(R.id.mask).setVisibility(View.VISIBLE);
		final CropImageView c = (CropImageView)findViewById(R.id.CropImageView);
		//c.setFixedAspectRatio(true);
		c.setAspectRatio(aspectRatioX, aspectRatioY);
		c.setVisibility(View.VISIBLE);
		c.setImageBitmap(tmp_original_bitmap);
		c.invalidate();
		if (executeBlurBackground)
			new BlurBackgroundBitmap(this, tmp_original_bitmap).execute();
		
		findViewById(R.id.crop).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				little_bitmap_original = c.getCroppedImage();
				AlphaAnimation a2 = new AlphaAnimation(1.0f, 0.0f);
				a2.setDuration(500);
				little_bitmap = little_bitmap_original.copy(little_bitmap_original.getConfig(), true);

				ImageView container = (ImageView) findViewById(R.id.container);
				container.setVisibility(View.VISIBLE);
				container.setImageBitmap(little_bitmap_original);

				findViewById(R.id.mask).startAnimation(a2);
				findViewById(R.id.mask).setVisibility(View.GONE);
				findViewById(R.id.CropImageView).startAnimation(a2);
				findViewById(R.id.CropImageView).setVisibility(View.GONE);

				findViewById(R.id.actions1).setVisibility(View.GONE);
				findViewById(R.id.actions2).setVisibility(View.VISIBLE);
				updateContainer();
				state = ST_BLUR;
			}
		});
		findViewById(R.id.rotate).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((CropImageView) findViewById(R.id.CropImageView)).rotateImage(90);
			}
		});
	}
	
	public void updateContainer() {
		try {
			if (little_bitmap != null && !little_bitmap.isRecycled())
				((ImageView)findViewById(R.id.container)).setImageBitmap(little_bitmap);
		} catch (Exception ex) { ex.printStackTrace(); }
	}
}
