/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi.video;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.zidoo.test.hdmi.ZidooHdmiDisPlay;

public class HdmiAndVideoActivity extends Activity implements OnClickListener {
	private PlayVideoTool		mPlayVideoTool	= null;
	private WebView				mWebView		= null;
	private ZidooHdmiDisPlay	mRealtekeHdmi	= null;
	private boolean				isHdmiAudio		= true;
	private TextView			mTimeCountView	= null;
	private Handler				mHandler		= null;
	private final static int	COUNT			= 0;
	private final static int	COUNTTIME		= 1*1000;
	private long mCount = 0 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hdmi_video_ac);
		initView();
	}

	private void initView() {
		mPlayVideoTool = new PlayVideoTool((VideoView) findViewById(R.id.home_ac_videoview));
		findViewById(R.id.sound_hdmi).setOnClickListener(this);
		mTimeCountView = (TextView) findViewById(R.id.home_ac_time);
		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.loadUrl("http://www.zidoo.tv");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				view.loadUrl(url);
				return true;
			}
		});

		ViewGroup hdmiGroud = (ViewGroup) findViewById(R.id.home_ac_hdmi);
		mRealtekeHdmi = new ZidooHdmiDisPlay(HdmiAndVideoActivity.this, hdmiGroud, null, ZidooHdmiDisPlay.TYPE_SURFACEVIEW);
		mRealtekeHdmi.startDisPlay();
		initCount();
	}

	private void initCount() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case COUNT:
					mHandler.removeMessages(COUNT);
					mCount++;
					mTimeCountView.setText(formatTime(mCount));
					mHandler.sendEmptyMessageDelayed(COUNT, COUNTTIME);
					break;

				default:
					break;
				}
			}
		};
		mHandler.sendEmptyMessage(COUNT);
	}

	private String formatTime(long time) {
		String timeStr = "";
		try {
			long h = time / 3600;
			long min = (time % 3600) / 60;
			long ss = time % 60;
			if (h < 10) {
				timeStr = "0" + h;
			} else {
				timeStr = "" + h;
			}
			if (min < 10) {
				timeStr = timeStr + ":0" + min;
			} else {
				timeStr = timeStr + ":" + min;
			}
			if (ss < 10) {
				timeStr = timeStr + ":0" + ss;
			} else {
				timeStr = timeStr + ":" + ss;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeStr;
	}
	
	@Override
	protected void onDestroy() {
		mHandler.removeMessages(COUNT);
		mPlayVideoTool.onDestroy();
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
