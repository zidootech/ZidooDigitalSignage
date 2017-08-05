/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.screen.scale;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.example.zuishare_test.R.id;
import com.example.zuishare_test.R.layout;
import com.zidoo.share.ZidooShareListener;
import com.zidoo.share.ZidooShareService;
import com.zidoo.share.tool.ZidooScreenScaleTool;

public class ScreenScaleActivity extends Activity {
	private ZidooShareService		mZidooShareService	= null;
	private TextView				mValuesView			= null;
	private ZidooScreenScaleTool	mScreenScaleTool	= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scale_ac);
		mValuesView = (TextView) findViewById(R.id.text);
		initService();
	}

	private void initService() {
		mZidooShareService = new ZidooShareService(this);
		mZidooShareService.registerShareService(new ZidooShareListener() {

			@Override
			public void registerSuccess() {
				mScreenScaleTool = mZidooShareService.getZidooScreenScaleTool();
				setDisHit();
			}

			@Override
			public void registerFaile(int arg0) {

			}
		});
	}

	private void setDisHit() {
		try {
			mValuesView.setText("Current: " + mScreenScaleTool.getScreenScale());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		super.onResume();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (mScreenScaleTool != null) {
					try {
						int curentScale = mScreenScaleTool.getScreenScale();
						curentScale--;
						if (curentScale <= 50) {
							return true;
						}
						mScreenScaleTool.setScreenScale(curentScale);
						setDisHit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (mScreenScaleTool != null) {
					try {
						int curentScale = mScreenScaleTool.getScreenScale();
						curentScale++;
						if (curentScale > 100) {
							return true;
						}
						mScreenScaleTool.setScreenScale(curentScale);
						setDisHit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			default:
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		try {
			mZidooShareService.unRegisterShareService();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

}
