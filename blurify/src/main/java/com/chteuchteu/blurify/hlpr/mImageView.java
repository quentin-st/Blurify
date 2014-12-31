package com.chteuchteu.blurify.hlpr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class mImageView extends ImageView {
	public mImageView(Context context, AttributeSet attributeSet) { super(context, attributeSet); }

	public mImageView(Context context) {
		super(context);
	}

	OnImageChangeListener onImageChangeListener;

	public void setImageChangeListener(
			OnImageChangeListener onImageChangeListener) {
		this.onImageChangeListener = onImageChangeListener;
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
		if (onImageChangeListener != null)
			onImageChangeListener.imageChangedinView();
	}


	@Override
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		if (onImageChangeListener != null)
			onImageChangeListener.imageChangedinView();
	}


	public static interface OnImageChangeListener {
		public void imageChangedinView();
	}
}
