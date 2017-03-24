/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.digitalsignage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoScrollTextView extends TextView {

	private int		textLength;
	private float	viewWidth	= 0f;
	private float	tx			= 0f;
	private float	ty			= 0f;
	private float	temp_tx1	= 0.0f;
	private float	temp_tx2	= 0x0f;
	private boolean	isStarting	= false;
	private Paint	paint		= null;
	private String	text		= "";
	private float	Speed		= 2.0f;
	private int		time		= 1;

	private int		count		= 0;

	private boolean	isCycle		= false;

	public void setTime(int time) {
		isCycle = false;
		this.time = time;
	}

	public boolean isCycle() {
		return isCycle;
	}

	public void setCycle(boolean isCycle) {
		this.isCycle = isCycle;
	}

	public AutoScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public AutoScrollTextView(Context context) {
		super(context);
	}
	
	

	public void initScrollTextView(float width, String text) {
		paint = this.getPaint();
		this.text = text;

		textLength = (int) paint.measureText(text);
		viewWidth = this.getWidth();
		if (viewWidth == 0) {
			viewWidth = width;
		}
		tx = textLength;
		temp_tx1 = viewWidth + textLength;
		temp_tx2 = viewWidth + textLength * 2;
		ty = this.getTextSize() + this.getPaddingTop();
	}

	public void starScroll() {
		isStarting = true;
		this.invalidate();
	}

	public void stopScroll() {
		isStarting = false;
		setText(text);
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isStarting) {
			canvas.drawText(text, temp_tx1 - tx, ty, paint);
			tx += Speed;
			if (tx >= temp_tx2) {
				count++;
				tx = temp_tx1 - viewWidth;
			}
			if (!isCycle) {
				if (count == time) {
					stopScroll();
				}
			}
			this.invalidate();
		}
		super.onDraw(canvas);
	}
}
