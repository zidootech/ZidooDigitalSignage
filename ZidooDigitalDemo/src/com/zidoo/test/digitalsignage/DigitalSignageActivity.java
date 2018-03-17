/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.digitalsignage;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.zuishare_test.R;
import com.zidoo.test.hdmi.ZidooHdmiDisPlay;
import com.zidoo.test.hdmi.ZidooHdmiDisPlay.HdmiInFristDisplayListener;

public class DigitalSignageActivity extends Activity implements OnClickListener {

	private ZidooHdmiDisPlay	mRealtekeHdmi	= null;
	private boolean				isHdmiAudio		= true;
	private ViewGroup			mHdmiRootView	= null;
	private ImageView			mGView			= null;
	private int					mCurrentG		= -1;
	private int					mG[]			= { R.drawable.digital_g_0, R.drawable.digital_g_1, R.drawable.digital_g_2, R.drawable.digital_g_3 };
	private Handler				mHandler		= null;
	private boolean				isDisplay		= false;
	private final static int	GHAND			= 0;
	private final static int	GDISPLAY		= 1;
	private final static int	GTIME			= 10 * 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.digitalsigna_ac);
		initView();
	}

	private void initView() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case GHAND:
					startGView();
					break;
				case GDISPLAY:
					if (!isDisplay) {
						initGView();
					}
					break;

				default:
					break;
				}
			}
		};
		findViewById(R.id.sound_hdmi).setOnClickListener(this);
		mGView = (ImageView) findViewById(R.id.g_view);
		mHdmiRootView = (ViewGroup) findViewById(R.id.home_ac_hdmi);
		mRealtekeHdmi = new ZidooHdmiDisPlay(DigitalSignageActivity.this, mHdmiRootView, new HdmiInFristDisplayListener() {

			@Override
			public void fristDisplay() {
				// TODO Auto-generated method stub
				animation();
			}
		},ZidooHdmiDisPlay.TYPE_SURFACEVIEW);
		mRealtekeHdmi.startDisPlay();
		mHandler.sendEmptyMessageDelayed(GDISPLAY, 5*1000);
	}

	private void animation() {
		// hdmi
		{
			PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1, 0.87f);
			PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1, 0.87f);
			PropertyValuesHolder[] mPropertyValuesHolders = new PropertyValuesHolder[2];
			mPropertyValuesHolders[0] = pvhX;
			mPropertyValuesHolders[1] = pvhY;
			ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(mHdmiRootView, mPropertyValuesHolders);
			mHdmiRootView.setPivotX(0);
			mHdmiRootView.setPivotY(0);
			oa.setDuration(1000);
			oa.start();
		}
		if (!isDisplay) {
			initGView();
		}
	}

	private void initGView() {
		isDisplay = true;
		mHandler.removeMessages(GDISPLAY);
		// mGView
		mHandler.sendEmptyMessage(GHAND);
		startGView();
		// TextView
		AutoScrollTextView autoScrollTextView = (AutoScrollTextView) findViewById(R.id.textView);
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		float screenWidth = dm.widthPixels;
		autoScrollTextView.initScrollTextView(screenWidth, this.getString(R.string.showtext));
		autoScrollTextView.setCycle(true);
		autoScrollTextView.starScroll();
	}

	private void startGView() {
		mCurrentG++;
		if (mCurrentG >= mG.length) {
			mCurrentG = 0;
		}
		mGView.setImageResource(mG[mCurrentG]);
		mGView.setTranslationX(mGView.getWidth());
		mGView.setVisibility(View.VISIBLE);
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("translationX", mGView.getWidth(), 0);
		PropertyValuesHolder[] mPropertyValuesHolders = new PropertyValuesHolder[1];
		mPropertyValuesHolders[0] = pvhX;
		ObjectAnimator oa = ObjectAnimator.ofPropertyValuesHolder(mGView, mPropertyValuesHolders);
		oa.setDuration(1000);
		oa.start();
		mHandler.removeMessages(GHAND);
		mHandler.sendEmptyMessageDelayed(GHAND, GTIME);
	}

	@Override
	protected void onDestroy() {
		if (mRealtekeHdmi != null) {
			mRealtekeHdmi.exit();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mRealtekeHdmi.stopDisPlay();
		super.onPause();
	}

	@Override
	protected void onResume() {
		try {
			View thisView = getWindow().getDecorView();
			thisView.setSystemUiVisibility((View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mRealtekeHdmi.startDisPlay();
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sound_hdmi:
			isHdmiAudio = !isHdmiAudio;
			mRealtekeHdmi.setAudio(isHdmiAudio);
			((Button) v).setText(isHdmiAudio ? "Close audio" : "Open audio");
			break;

		default:
			break;
		}
	}

}
