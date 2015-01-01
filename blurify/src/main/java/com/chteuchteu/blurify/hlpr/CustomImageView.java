package com.chteuchteu.blurify.hlpr;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CustomImageView extends ImageView {
	public CustomImageView(Context context, AttributeSet attributeSet) { super(context, attributeSet); }

	public CustomImageView(Context context) {
		super(context);
	}

	private AfterNextDrawListener afterNextDrawListener;

	public void setAfterNextDrawListener(
			AfterNextDrawListener afterNextDrawListener) {
		this.afterNextDrawListener = afterNextDrawListener;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (this.afterNextDrawListener != null) {
			this.afterNextDrawListener.onDrawCalled();
			this.afterNextDrawListener = null;
		}
	}


	public static interface AfterNextDrawListener {
		public void onDrawCalled();
	}
}
