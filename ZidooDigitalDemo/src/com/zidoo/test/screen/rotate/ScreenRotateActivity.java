/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.screen.rotate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.example.zuishare_test.R.id;
import com.example.zuishare_test.R.layout;
import com.zidoo.share.ZidooShareListener;
import com.zidoo.share.ZidooShareService;
import com.zidoo.share.aidl.IZidooShareService;
import com.zidoo.share.tool.ZidooScreenRotateTool;

public class ScreenRotateActivity extends Activity {
	private TextView				mValuesView				= null;
	private ZidooScreenRotateTool	mZidooScreenRotateTool	= null;
	private ZidooShareService		mZidooShareService		= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenrotation_ac);
		mValuesView = (TextView) findViewById(R.id.text);
		initService();
	}

	private void initService() {
		mZidooShareService = new ZidooShareService(this);
		mZidooShareService.registerShareService(new ZidooShareListener() {

			@Override
			public void registerSuccess() {
				mZidooScreenRotateTool = mZidooShareService.getZidooScreenRotateTool();
				setDisHit();
			}

			@Override
			public void registerFaile(int arg0) {

			}
		});
	}

	private void setDisHit() {
		try {
			String dis[] = { "0", "90", "180", "270" };
			mValuesView.setText("Current: " + dis[mZidooScreenRotateTool.getScreenRotation()]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onMyClick(View view) {
		if (mZidooScreenRotateTool == null) {
			return;
		}
		try {
			switch (view.getId()) {
			case R.id.d_0:
				mZidooScreenRotateTool.setScreenRotation(0);
				break;
			case R.id.d_90:
				mZidooScreenRotateTool.setScreenRotation(1);
				break;
			case R.id.d_180:
				mZidooScreenRotateTool.setScreenRotation(2);
				break;
			case R.id.d_270:
				mZidooScreenRotateTool.setScreenRotation(3);
				break;

			default:
				break;
			}
			setDisHit();
		} catch (Exception e) {
			e.printStackTrace();
		}

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

}
