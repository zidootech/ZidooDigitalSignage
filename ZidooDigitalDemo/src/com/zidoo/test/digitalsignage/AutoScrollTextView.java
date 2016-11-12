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

	/** 文字长度 */
	private int		textLength;
	/** 滚动条长度 */
	private float	viewWidth	= 0f;
	/** 文本x轴 的坐标 */
	private float	tx			= 0f;
	/** 文本Y轴的坐标 */
	private float	ty			= 0f;
	/** 文本当前长度 */
	private float	temp_tx1	= 0.0f;
	/** 文本当前变换的长度 */
	private float	temp_tx2	= 0x0f;
	/** 文本滚动开关 */
	private boolean	isStarting	= false;
	/** 画笔对象 */
	private Paint	paint		= null;
	/** 显示的文字 */
	private String	text		= "";
	/** 设置移动速度 */
	private float	Speed		= 2.0f;
	/** 设置滚动次数 */
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

	/**
	 * 初始化自动滚动条,每次改变文字内容时，都需要重新初始化一次
	 * 
	 * @param windowManager
	 *            获取屏幕
	 * @param text
	 *            显示的内容
	 */
	public void initScrollTextView(float width, String text) {
		// 得到画笔,获取父类的textPaint
		paint = this.getPaint();
		// 得到文字
		this.text = text;

		textLength = (int) paint.measureText(text);// 获得当前文本字符串长度
		viewWidth = this.getWidth();// 获取宽度return mRight - mLeft;
		if (viewWidth == 0) {
			// if (windowManager != null) {
			// 获取当前屏幕的属性
			// Display display = windowManager.getDefaultDisplay();
			// viewWidth = display.getWidth();// 获取屏幕宽度
			viewWidth = width;
		}
		// }
		tx = textLength;
		temp_tx1 = viewWidth + textLength;
		temp_tx2 = viewWidth + textLength * 2;// 自己定义，文字变化多少
		// 文字的大小+距顶部的距离
		ty = this.getTextSize() + this.getPaddingTop();
	}

	/**
	 * 开始滚动
	 */
	public void starScroll() {
		// 开始滚动
		isStarting = true;
		this.invalidate();// 刷新屏幕
	}

	/**
	 * 停止方法,停止滚动
	 */
	public void stopScroll() {
		// 停止滚动
		isStarting = false;
		setText(text);
		this.invalidate();// 刷新屏幕
	}

	/** 重写onDraw方法 */
	@Override
	protected void onDraw(Canvas canvas) {
		if (isStarting) {
			// A-Alpha透明度/R-Read红色/g-Green绿色/b-Blue蓝色
			// paint.setColor(Color.WHITE);
			// paint.setARGB(255, 200, 200, 200);
			// paint.setTextSize(40);
			canvas.drawText(text, temp_tx1 - tx, ty, paint);
			// 设置当前文字移动的距离（设置移动速度）
			tx += Speed;
			// 当文字滚动到屏幕的最左边
			if (tx >= temp_tx2) {
				count++;
				// 把文字设置到最右边开始
				tx = temp_tx1 - viewWidth;
			}
			if (!isCycle) {
				if (count == time) {
					stopScroll();
				}
			}
			this.invalidate();// 刷新屏幕
		}
		super.onDraw(canvas);
	}
}