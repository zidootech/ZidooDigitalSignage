/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi.pip;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.zidoo.test.zidooutil.MyLog;
import com.zidoo.test.zidooutil.ZidooSharedPrefsUtil;

public class FloatingWindowView extends FrameLayout {
	// jiangbo
	private final static String	PIP_SOUND_SHARE		= "pip_sound";
	public TextView				mNoHdmiSignalView	= null;
	public View					mOperateView		= null;
	private ImageView			mBigView			= null;
	private ImageView			mAudioView			= null;
	// jiangbo
	private int					xBeforeHide			= 1920 - 480 - 30;
	private int					yBeforeHide			= 50;
	private int					wBeforeHide			= 480;
	private int					hBeforeHide			= 360;
	private boolean				isMove				= true;

	// /////////////////////////
	final int					MIN_RATIO			= 9;
	private PointF				mLastDownPoint		= null;
	int							mBorder, mMinWidth, mMinHeight, mScreenWidth, mScreenHeight;
	int							mType				= 0;
	boolean						mStretchingDerailly	= false;
	float						mRatio				= 0.5625f;
	private Handler				mHandler			= null;
	private final static int	GONE				= 0;
	private final static int	GONETIME			= 3 * 1000;

	public static boolean isPipSound(Context mContext) {
		return ZidooSharedPrefsUtil.getValue(mContext, PIP_SOUND_SHARE, true);
	}

	public static void setPipSound(Context mContext, boolean isPipSound) {
		ZidooSharedPrefsUtil.putValue(mContext, PIP_SOUND_SHARE, isPipSound);
	}

	public boolean isMove() {
		return isMove;
	}

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	public void initDis() {
		mHandler.removeMessages(GONE);
		mOperateView.setVisibility(View.GONE);
	}

	public void setOperateView(View view, final PiPService piPService) {
		mOperateView = view;
		mOperateView.setVisibility(View.GONE);
		mBigView = (ImageView) view.findViewById(R.id.floating_recording_big);
		mBigView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
				if (lp.width == 1920) {
					mBigView.setImageResource(R.drawable.big);
					lp.x = xBeforeHide;
					lp.y = yBeforeHide;
					lp.width = wBeforeHide;
					lp.height = hBeforeHide;
				} else {
					xBeforeHide = lp.x;
					yBeforeHide = lp.y;
					wBeforeHide = lp.width;
					hBeforeHide = lp.height;
					mBigView.setImageResource(R.drawable.smal);
					lp.x = 0;
					lp.y = 0;
					lp.width = 1920;
					lp.height = 1080;
				}
				WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
				wm.updateViewLayout(FloatingWindowView.this, lp);
				setSize(lp.width);
			}
		});
		mAudioView = (ImageView) view.findViewById(R.id.floating_audio_big);
		mAudioView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isAuto = isPipSound(getContext());
				isAuto = !isAuto;
				setPipSound(getContext(),isAuto);
				mAudioView.setImageResource(isAuto ? R.drawable.pip_audio_open : R.drawable.pip_audio_close);
				piPService.togglePreview(true, isAuto);
			}
		});
		View deleteView = view.findViewById(R.id.floating_recording_delete);
		deleteView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				piPService.stopPip();
			}
		});
	}

	private void versionOper() {
		WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
		mBigView.setImageResource(lp.width == 1920 ? R.drawable.smal : R.drawable.big);
		boolean isAuto = isPipSound(getContext());
		mAudioView.setImageResource(isAuto ? R.drawable.pip_audio_open : R.drawable.pip_audio_close);
		mOperateView.setVisibility(View.VISIBLE);
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case GONE:
					gone();
					break;

				default:
					break;
				}
			}
		};
	}

	private void gone() {
		if (mOperateView != null) {
			mOperateView.setVisibility(View.GONE);
		}
	}

	public void setRatio(float ratio) {
		mRatio = ratio;
	}

	public FloatingWindowView(Context c) {
		super(c);
		init();
	}

	public FloatingWindowView(Context c, AttributeSet attrs) {
		super(c, attrs);
		init();
	}

	public FloatingWindowView(Context c, AttributeSet attrs, int defStyleAttr) {
		super(c, attrs, defStyleAttr);
		init();
	}

	private void init() {
		initHandler();
		DisplayMetrics mDMs = new DisplayMetrics();
		((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(mDMs);
		mScreenWidth = mDMs.widthPixels;
		mScreenHeight = mDMs.heightPixels;

		mMinWidth = mScreenWidth / 10;
		mMinHeight = mScreenHeight / 10;
		mBorder = (int) (8 * getResources().getDisplayMetrics().density);
	}

	public void setHdmiGone() {

		WindowManager.LayoutParams lParams = (WindowManager.LayoutParams) getLayoutParams();
		int w = lParams.width;
		int h = lParams.height;

		int flag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		xBeforeHide = lParams.x;
		yBeforeHide = lParams.y;

		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_TOAST, flag, PixelFormat.TRANSLUCENT);

		wmParams.x = 5000;
		wmParams.y = 5000;

		// wmParams.x = x;
		// wmParams.y = y;

		wmParams.gravity = Gravity.TOP | Gravity.LEFT;
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.updateViewLayout(this, wmParams);

	}

	public void setHdmiDisPlay(boolean isDisplay) {

		int flag;

		WindowManager.LayoutParams lParams = (WindowManager.LayoutParams) getLayoutParams();

		int w = lParams.width;
		int h = lParams.height;

		if (isDisplay) {
			flag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
			// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		} else {
			flag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		}

		if (!isDisplay) {
			xBeforeHide = lParams.x;
			yBeforeHide = lParams.y;
		}

		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams(w, h, WindowManager.LayoutParams.TYPE_TOAST, flag, PixelFormat.TRANSLUCENT);

		if (isDisplay) {
			// this.setAlpha(1);
			wmParams.x = xBeforeHide;
			wmParams.y = yBeforeHide;
		} else {
			// this.setAlpha(0);
			wmParams.x = 5000;
			wmParams.y = 5000;
		}

		// wmParams.x = x;
		// wmParams.y = y;

		wmParams.gravity = Gravity.TOP | Gravity.LEFT;
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		wm.updateViewLayout(this, wmParams);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (!isMove) {
			return super.onTouchEvent(event);
		}

		final float x = event.getRawX();
		final float y = event.getRawY();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			float vx = event.getX();
			float vy = event.getY();
			mType = (vx < mBorder ? 1 : 0) | (vy < mBorder ? 1 << 1 : 0) | (getWidth() - vx < mBorder ? 1 << 2 : 0) | (getHeight() - vy < mBorder ? 1 << 3 : 0);

			mLastDownPoint = new PointF(x, y);
		}
			return true;
		case MotionEvent.ACTION_MOVE: {
			if (mStretchingDerailly) {
				float vx = event.getX();
				float vy = event.getY();
				int type = (vx < mBorder ? 1 : 0) | (vy < mBorder ? 1 << 1 : 0) | (getWidth() - vx < mBorder ? 1 << 2 : 0) | (getHeight() - vy < mBorder ? 1 << 3 : 0);

				mStretchingDerailly = type != mType;
			} else {
				float dx = x - mLastDownPoint.x;
				float dy = y - mLastDownPoint.y;

				WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
				switch (mType) {
				case 0:// middle
					lp.x = offset(lp.x, dx, lp.width, mScreenWidth);
					lp.y = offset(lp.y, dy, lp.height, mScreenHeight);
					break;
				case 1:// left
					lp.x = offset(lp.x, dx, lp.width, mScreenWidth);
					lp.width -= dx;
					break;
				case 2:// top
					lp.y = offset(lp.y, dy, lp.height, mScreenHeight);
					lp.height -= dy;
					break;
				case 4:// right
					lp.width += dx;
					break;
				case 8:// bottom
					lp.height += dy;
					break;
				case 3:// top-left
					lp.x = offset(lp.x, dx, lp.width, mScreenWidth);
					lp.y = offset(lp.y, dy, lp.height, mScreenHeight);
					lp.width -= dx;
					lp.height -= dy;
					break;
				case 6:// top-right
					lp.y = offset(lp.y, dy, lp.height, mScreenHeight);
					lp.height -= dy;
					lp.width += dx;
					break;
				case 12:// bottom-right
					lp.width += dx;
					lp.height += dy;
					break;
				case 9:// bottom-left
					lp.x = offset(lp.x, dx, lp.width, mScreenWidth);
					lp.width -= dx;
					lp.height += dy;
					break;

				default:
					break;
				}

				if (lp.width < mMinWidth) {
					lp.width = mMinWidth;
					mStretchingDerailly = true;
				}

				if (lp.height < mMinHeight) {
					lp.height = mMinHeight;
					mStretchingDerailly = true;
				}

				WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
				wm.updateViewLayout(this, lp);
			}

			mLastDownPoint = new PointF(x, y);
		}
			return true;
		case MotionEvent.ACTION_UP: {
			// save position
			WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
			mStretchingDerailly = false;
			if (lp.width == 1920) {
				mBigView.setImageResource(R.drawable.big);
				lp.x = xBeforeHide;
				lp.y = yBeforeHide;
				lp.width = wBeforeHide;
				lp.height = hBeforeHide;
				WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
				wm.updateViewLayout(FloatingWindowView.this, lp);
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pipx", lp.x).putInt("pipy", lp.y).putInt("pipw", lp.width).putInt("piph", lp.height)
						.commit();
				setSize(lp.width);
				return true;
			} else {
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pipx", lp.x).putInt("pipy", lp.y).putInt("pipw", lp.width).putInt("piph", lp.height)
						.commit();
				setSize(lp.width);
			}

		}
			return true;

		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (!isMove) {
			return super.onGenericMotionEvent(event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_SCROLL:
			float value = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
			if (value > 0) {
				WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
				float cr = (float) lp.height / lp.width;
				if (cr == mRatio) {
					lp.width = Math.min(lp.width + (int) (MIN_RATIO / mRatio), mScreenWidth);
					lp.height = Math.min(lp.height + MIN_RATIO, mScreenHeight);
				} else if (cr > mRatio) {
					lp.width = Math.min(lp.width + (int) (MIN_RATIO / mRatio), mScreenWidth);
					if (lp.height < lp.width * mRatio) {
						lp.height = Math.min((int) (lp.width * mRatio), mScreenHeight);
					}
				} else {
					lp.height = Math.min(lp.height + MIN_RATIO, mScreenHeight);
					if (lp.width < lp.height / mRatio) {
						lp.width = Math.min((int) (lp.height / mRatio), mScreenWidth);
					}
				}

				if (lp.x + lp.width > mScreenWidth)
					lp.x = mScreenWidth - lp.width;
				if (lp.y + lp.height > mScreenHeight)
					lp.y = mScreenHeight - lp.height;

				WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
				wm.updateViewLayout(this, lp);
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pipx", lp.x).putInt("pipy", lp.y).putInt("pipw", lp.width).putInt("piph", lp.height)
						.commit();
				setSize(lp.width);
				return true;
			} else if (value < 0) {
				WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
				float cr = (float) lp.height / lp.width;
				if (cr == mRatio) {
					lp.width = Math.max(lp.width - (int) (MIN_RATIO / mRatio), mMinWidth);
					lp.height = Math.max(lp.height - MIN_RATIO, mMinHeight);
				} else if (cr > mRatio) {
					lp.height = Math.max(lp.height - MIN_RATIO, mMinHeight);
					if (lp.width > lp.height / mRatio) {
						lp.width = Math.max((int) (lp.height / mRatio), mMinWidth);
					}
				} else {
					lp.width = Math.max(lp.width - (int) (MIN_RATIO / mRatio), mMinWidth);
					if (lp.height > lp.width * mRatio) {
						lp.height = Math.max((int) (lp.width * mRatio), mMinHeight);
					}
				}

				WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
				wm.updateViewLayout(this, lp);
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("pipx", lp.x).putInt("pipy", lp.y).putInt("pipw", lp.width).putInt("piph", lp.height)
						.commit();
				setSize(lp.width);
				return true;
			}
			break;
		case MotionEvent.ACTION_HOVER_MOVE:
			if (isMove) {
				mHandler.removeMessages(GONE);
				mHandler.sendEmptyMessageDelayed(GONE, GONETIME);
				versionOper();
			}
			break;
		case MotionEvent.ACTION_HOVER_ENTER:
			if (isMove) {
				mHandler.removeMessages(GONE);
				mHandler.sendEmptyMessageDelayed(GONE, GONETIME);
				versionOper();
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			mHandler.removeMessages(GONE);
			mOperateView.setVisibility(View.GONE);
			break;
		}

		return super.onGenericMotionEvent(event);
	}

	private int offset(float x, float dx, int w, int max) {
		return dx > 0 ? (int) Math.min(max - w, x + dx) : (int) Math.max(0, x + dx);
	}

	public static int getPIPX(Context context) {
		return ZidooSharedPrefsUtil.getValue(context, "pipx", 1920 - 480 - 30);
	}

	public static int getPIPY(Context context) {
		return ZidooSharedPrefsUtil.getValue(context, "pipy", 50);
	}

	public static int getPIPW(Context context) {
		return ZidooSharedPrefsUtil.getValue(context, "pipw", 480);
	}

	public static int getPIPH(Context context) {
		return ZidooSharedPrefsUtil.getValue(context, "piph", 360);
	}

	public void setSize(int w) {

		MyLog.v("setsize = w = " + w);
		if (w <= 250) {
			mNoHdmiSignalView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		} else if (w <= 500) {
			mNoHdmiSignalView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		} else if (w <= 900) {
			mNoHdmiSignalView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
		} else if (w <= 1600) {
			mNoHdmiSignalView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
		} else if (w <= 1920) {
			mNoHdmiSignalView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
		}

	}
}
