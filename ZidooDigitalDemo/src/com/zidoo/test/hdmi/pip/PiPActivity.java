/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi.pip;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import com.example.zuishare_test.R;
import com.zidoo.test.hdmi.pip.PiPService.LocalBinder;
import com.zidoo.test.zidooutil.MyLog;

public class PiPActivity extends Activity {
	private ServiceConnection	mServiceConnection	= null;
	private PiPService			mPiPService			= null;
	private Button				mPipButton			= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pip_ac);
		mPipButton = (Button) findViewById(R.id.open_pip);
		startService();
		initService();
	}

	private void initService() {
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				MyLog.v("bindService RecorderService onServiceConnected");
				LocalBinder binder = (LocalBinder) service; // 通过IBinder获取Service
				mPiPService = binder.getService();
				setButton(mPiPService.isPip());
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				MyLog.e("bindService RecorderService onServiceDisconnected");
				mPiPService = null;
			}
		};

		/* 绑定service */
		Intent bindIntent = new Intent(this, PiPService.class);
		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void startService() {
		Intent bindIntent = new Intent(this, PiPService.class);
		startService(bindIntent);
	}

	private void setButton(boolean isOpenPip) {
		mPipButton.setText(isOpenPip ? "Close Pip" : "Open Pip");
	}

	@Override
	protected void onDestroy() {
		try {
			unbindService(mServiceConnection);
			if (mPiPService != null && !mPiPService.isPip()) {
				stopService(new Intent(this, PiPService.class));
			}
		} catch (Exception e) {
			// TODO: handle exception
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

	public void onMyClick(View view) {
		switch (view.getId()) {
		case R.id.open_pip:
			if (mPiPService != null) {
				if (mPiPService.isPip()) {
					mPiPService.stopPip();
				} else {
					mPiPService.startPip();
				}
				setButton(mPiPService.isPip());
			}
			break;
		default:
			break;
		}
	}

}
