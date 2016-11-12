/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.screen.resolution;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.example.zuishare_test.R.id;
import com.example.zuishare_test.R.layout;
import com.zidoo.share.ZidooShareListener;
import com.zidoo.share.ZidooShareService;
import com.zidoo.share.aidl.IZidooShareService;
import com.zidoo.share.tool.ZidooResolutionTool;

public class ResolutionActivity extends Activity {
	private ListView			mListViewView			= null;
	private ZidooResolutionTool	mZidooResolutionTool	= null;
	private ZidooShareService	mZidooShareService		= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resolution_ac);
		mListViewView = (ListView) findViewById(R.id.list);
		initService();
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

	private void initService() {

		mZidooShareService = new ZidooShareService(this);
		mZidooShareService.registerShareService(new ZidooShareListener() {

			@Override
			public void registerSuccess() {
				mZidooResolutionTool = mZidooShareService.getZidooResolutionTool();
				initList();
			}

			@Override
			public void registerFaile(int arg0) {

			}
		});
	}

	private void initList() {
		try {
			final String[] resolution = mZidooResolutionTool.getResolutionList();
			if (resolution != null) {
				final MyAdapter myAdapter = new MyAdapter(resolution);
				myAdapter.setmIndex(mZidooResolutionTool.getResolution());
				mListViewView.setAdapter(myAdapter);
				mListViewView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
						try {
							mZidooResolutionTool.setResolution(position);

							Dialog dialog = new AlertDialog.Builder(ResolutionActivity.this).setMessage("Change resolution to   ： " + resolution[position])
									.setNegativeButton("OK", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											Log.v("bob", "ok");
											myAdapter.setmIndex(position);
											myAdapter.notifyDataSetChanged();
										};
									}).setPositiveButton("Cancel", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											Log.v("bob", "cancel");
											try {
												mZidooResolutionTool.setResolution(myAdapter.getmIndex());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}).create();

							dialog.show();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyAdapter extends BaseAdapter {

		String[]	mResolution	= null;
		int			mIndex		= 0;

		public MyAdapter(String[] mResolution) {
			super();
			this.mResolution = mResolution;
		}

		public void setmIndex(int mIndex) {
			this.mIndex = mIndex;
		}

		public int getmIndex() {
			return mIndex;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mResolution.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView textView = new TextView(ResolutionActivity.this);
			textView.setTextColor(Color.BLACK);
			textView.setTextSize(30);
			String msg = mResolution[position];
			if (position == mIndex) {
				msg = mResolution[position] + "          current";
			}
			textView.setText(msg);
			return textView;
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

	// 预留方法
	// 当前标识
	// Log.v("bob",
	// "flag ------------- = "+mIZidooShareService.getResolutionFlag());
	// 设置分辨率通过当前标识
	// if (mIZidooShareService.setResolutionByFlag("9")) {
	// Log.v("bob", "flag ------------- = 成功");
	// }else {
	// Log.v("bob", "flag ------------- = 不支持的分辨率");
	// }
	// 对应关系
	// "0", // TV_SYS_HDMI_AUTO_DETECT,
	// "1", // TV_SYS_NTSC,
	// "2", // TV_SYS_PAL,
	// "3", // TV_SYS_480P,
	// "4", // TV_SYS_576P,
	// "5", // TV_SYS_720P_50HZ,
	// "6", // TV_SYS_720P_60HZ,
	// "7", // TV_SYS_1080I_50HZ,
	// "8", // TV_SYS_1080I_60HZ,
	// "9", // TV_SYS_1080P_50HZ,
	// "10", // TV_SYS_1080P_60HZ,
	// "11", // TV_SYS_2160P_24HZ,
	// "12", // TV_SYS_2160P_25HZ,
	// "13", // TV_SYS_2160P_30HZ,
	// "14", // TV_SYS_4096_2160P_24HZ
	// "15", // TV_SYS_1080P_24HZ
	// "16", // TV_SYS_720P_59HZ
	// "17", // TV_SYS_1080I_59HZ
	// "18", // TV_SYS_1080P_23HZ
	// "19", // TV_SYS_1080P_59HZ
	// "20", // TV_SYS_2160P_23HZ
	// "21", // TV_SYS_2160P_29HZ
	// "22", // TV_SYS_2160P_60HZ

}
