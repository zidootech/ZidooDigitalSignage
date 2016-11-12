/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.example.zuishare_test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zidoo.test.decoder.MediaCodecActivity;
import com.zidoo.test.digitalsignage.DigitalSignageActivity;
import com.zidoo.test.hdmi.HDMIActivity;
import com.zidoo.test.hdmi.pip.PiPActivity;
import com.zidoo.test.hdmi.pip.PiPService;
import com.zidoo.test.hdmi.video.HdmiAndVideoActivity;
import com.zidoo.test.screen.resolution.ResolutionActivity;
import com.zidoo.test.screen.rotate.ScreenRotateActivity;
import com.zidoo.test.screen.scale.ScreenScaleActivity;
import com.zidoo.test.zidooutil.AppContants;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	// Initialization demo
	private void init() {
		if (!new File(AppContants.VIDEOPATH).exists()) {
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Initialization...");
			progressDialog.show();
			new Thread(new Runnable() {
				@Override
				public void run() {

					File dirFile = new File(AppContants.VIDEODIR);
					if (!dirFile.exists()) {
						dirFile.mkdirs();
						try {
							Runtime.getRuntime().exec("chmod 777 " + AppContants.VIDEODIR);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (!copyAssetsFile(MainActivity.this, AppContants.VIDEOPATH, "test.mp4")) {
						MainActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(MainActivity.this, "Initialization faile...", Toast.LENGTH_LONG).show();
								progressDialog.dismiss();
								MainActivity.this.finish();
							}
						});
					}
					MainActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(MainActivity.this, "Initialization successful...", Toast.LENGTH_LONG).show();
							progressDialog.dismiss();
						}
					});
				}
			}).start();
		}
	}

	private static boolean copyAssetsFile(Context context, String path, String fileName) {
		boolean bRet = false;
		try {
			InputStream is = context.getAssets().open(fileName);
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			try {
				Runtime.getRuntime().exec("chmod 777 " + path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileOutputStream fos = new FileOutputStream(file);

			byte[] temp = new byte[4096];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.flush();
			fos.close();
			is.close();
			bRet = true;
		} catch (IOException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}

	public void onMyClick(View view) {
		switch (view.getId()) {
		case R.id.screenrotation:
			startActivity(new Intent(this, ScreenRotateActivity.class));
			break;
		case R.id.screenscale:
			startActivity(new Intent(this, ScreenScaleActivity.class));
			break;
		case R.id.screenresolution:
			startActivity(new Intent(this, ResolutionActivity.class));
			break;
		case R.id.hdmi:
			stopService(new Intent(this, PiPService.class));
			startActivity(new Intent(this, HDMIActivity.class));
			break;
		case R.id.pip:
			startActivity(new Intent(this, PiPActivity.class));
			break;
		case R.id.hdmi_video_web:
			stopService(new Intent(this, PiPService.class));
			startActivity(new Intent(this, HdmiAndVideoActivity.class));
			break;
		case R.id.videos:
			startActivity(new Intent(this, MediaCodecActivity.class));
			break;
		case R.id.digital:
			stopService(new Intent(this, PiPService.class));
			startActivity(new Intent(this, DigitalSignageActivity.class));
			break;

		default:
			break;
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

}
