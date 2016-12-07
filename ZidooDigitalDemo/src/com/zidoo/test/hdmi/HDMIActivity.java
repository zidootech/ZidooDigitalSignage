/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.zuishare_test.R;

public class HDMIActivity extends Activity {
	private ZidooHdmiDisPlay	mRealtekeHdmi	= null;
	private boolean				isAudio			= true;
	private boolean				isFull			= false;
	private Button				mAudioView		= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hdmi_ac);
		mAudioView = (Button) findViewById(R.id.home_ac_video_hdmi_change_sound);
		initService();
	}

	private void initService() {
		ViewGroup hdmiGroud = (ViewGroup) findViewById(R.id.home_ac_hdmi);
		mRealtekeHdmi = new ZidooHdmiDisPlay(HDMIActivity.this, hdmiGroud, null,ZidooHdmiDisPlay.TYPE_SURFACEVIEW);
		mRealtekeHdmi.startDisPlay();
	}

	@Override
	protected void onDestroy() {
		if (mRealtekeHdmi != null) {
			mRealtekeHdmi.exit();
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
		mRealtekeHdmi.startDisPlay();
		super.onResume();
	}

	public void onMyClick(View view) {
		switch (view.getId()) {
		case R.id.home_ac_video_hdmi_change_sound:
			changeSound();
			break;
		case R.id.home_ac_video_hdmi_change_size:
			changeSize();
			break;
		default:
			break;
		}
	}

	private void changeSize() {
		isFull = !isFull;
		mRealtekeHdmi.setSize(isFull);
	}

	private void changeSound() {
		isAudio = !isAudio;
		mRealtekeHdmi.setAudio(isAudio);
		mAudioView.setText(isAudio ? "Close audio" : "Open audio");
	}

}
