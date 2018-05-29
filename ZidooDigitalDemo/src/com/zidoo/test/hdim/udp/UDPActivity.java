/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdim.udp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.zidoo.test.hdmi.ZidooHdmiDisPlay;

public class UDPActivity extends Activity {
	private ZidooHdmiDisPlay	mRealtekeHdmi	= null;
	private Button				mUDPView		= null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udp_ac);
		mUDPView = (Button) findViewById(R.id.home_ac_video_hdmi_udp);
		TextView msgTextView = (TextView) findViewById(R.id.home_ac_video_hdmi_udp_msg);
		msgTextView.setText("udp://"+UdpTool.IP+":"+UdpTool.PORT);
		initService();
	}

	private void initService() {
		ViewGroup hdmiGroud = (ViewGroup) findViewById(R.id.home_ac_hdmi);
		mRealtekeHdmi = new ZidooHdmiDisPlay(UDPActivity.this, hdmiGroud, null, ZidooHdmiDisPlay.TYPE_SURFACEVIEW,new UdpTool(this));
		mRealtekeHdmi.startDisPlay();
	}

	@Override
	protected void onDestroy() {
		if (mRealtekeHdmi != null) {
			mRealtekeHdmi.stopUdp();
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
		case R.id.home_ac_video_hdmi_udp:
			if (mRealtekeHdmi.isUdping()) {
				mRealtekeHdmi.stopUdp();
			}else {
				mRealtekeHdmi.startUdp();
			}
			mUDPView.setText(mRealtekeHdmi.isUdping()?"Stop UDP":"Start UDP");
			break;
		default:
			break;
		}
	}

}
