/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.decoder;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.zuishare_test.R;
import com.zidoo.test.decoder.VideoDecodeThread.PlayCompleteListener;
import com.zidoo.test.zidooutil.AppContants;

public class MediaCodecActivity extends Activity implements SurfaceHolder.Callback {

	private SurfaceView				mSurfaceView[]			= new SurfaceView[8];
	private Button					mAudioButtonView[]		= new Button[8];
	private SurfaceHolder			mSurfaceHolder[]		= new SurfaceHolder[8];
	private VideoDecodeThread		mVideoDecodeThread[]	= new VideoDecodeThread[8];
	private AudioDecodeThread		mAudioDecodeThread[]	= new AudioDecodeThread[8];

	private final static String		PATH[]					= { AppContants.VIDEOPATH, AppContants.VIDEOPATH, AppContants.VIDEOPATH, AppContants.VIDEOPATH, AppContants.VIDEOPATH,
			AppContants.VIDEOPATH, AppContants.VIDEOPATH, AppContants.VIDEOPATH };

	private final static int		MAXCOUNT				= 4;

	private int						mCount					= 0;
	// 是否有声音，点击视频时切换声音开关
	private final static boolean	ISAUDIO					= true;
	// 是否重复
	private final static boolean	ISREPEATE				= true;

	private boolean					isFinish				= false;

	private Handler					mHandler				= new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decorde_ac);
		initView();
	}

	private void initView() {

		mSurfaceView[0] = (SurfaceView) findViewById(R.id.display_1).findViewById(R.id.display);
		mAudioButtonView[0] = (Button) findViewById(R.id.display_1).findViewById(R.id.audio);
		mSurfaceView[1] = (SurfaceView) findViewById(R.id.display_2).findViewById(R.id.display);
		mAudioButtonView[1] = (Button) findViewById(R.id.display_2).findViewById(R.id.audio);
		mSurfaceView[2] = (SurfaceView) findViewById(R.id.display_3).findViewById(R.id.display);
		mAudioButtonView[2] = (Button) findViewById(R.id.display_3).findViewById(R.id.audio);
		mSurfaceView[3] = (SurfaceView) findViewById(R.id.display_4).findViewById(R.id.display);
		mAudioButtonView[3] = (Button) findViewById(R.id.display_4).findViewById(R.id.audio);
		mSurfaceView[4] = (SurfaceView) findViewById(R.id.display_5).findViewById(R.id.display);
		mAudioButtonView[4] = (Button) findViewById(R.id.display_5).findViewById(R.id.audio);
		mSurfaceView[5] = (SurfaceView) findViewById(R.id.display_6).findViewById(R.id.display);
		mAudioButtonView[5] = (Button) findViewById(R.id.display_6).findViewById(R.id.audio);
		mSurfaceView[6] = (SurfaceView) findViewById(R.id.display_7).findViewById(R.id.display);
		mAudioButtonView[6] = (Button) findViewById(R.id.display_7).findViewById(R.id.audio);
		mSurfaceView[7] = (SurfaceView) findViewById(R.id.display_8).findViewById(R.id.display);
		mAudioButtonView[7] = (Button) findViewById(R.id.display_8).findViewById(R.id.audio);

		for (int i = 0; i < MAXCOUNT; i++) {
			mSurfaceHolder[i] = mSurfaceView[i].getHolder();
			mSurfaceHolder[i].setFixedSize(1920, 1080);
			mSurfaceHolder[i].addCallback(this);
			if (ISAUDIO) {
				// 切换声音
				final int c = i;
				mAudioButtonView[i].setVisibility(View.VISIBLE);
				mAudioButtonView[i].setText("Open Audio");
				mAudioButtonView[i].setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.v("bob", "switchMute  c = " + c);
						if (mAudioDecodeThread[c] != null && mAudioDecodeThread[c].isAlive()) {
							if (mAudioDecodeThread[c].switchMute()) {
								mAudioButtonView[c].setText("Close Audio");
							} else {
								mAudioButtonView[c].setText("Open Audio");
							}
						}
					}
				});
			}
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mCount++;
		Log.v("bob", "surfaceChanged-------------mCount = " + mCount);
		if (MAXCOUNT == mCount) {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					startPlay();
				}
			});
		}
	}

	private void startPlay() {

		for (int i = 0; i < MAXCOUNT; i++) {
			if (!new File(PATH[i]).exists()) {
				continue;
			}
			Log.v("bob", "startPlay-------------i = " + i);
			play(i, false);
		}

	}

	private void play(final int i, boolean isAudio) {
		if (mVideoDecodeThread[i] == null || !mVideoDecodeThread[i].isAlive()) {

			if (mAudioDecodeThread[i] != null && mAudioDecodeThread[i].isAlive()) {
				mAudioDecodeThread[i].stopThread();
				while (mAudioDecodeThread[i].isAlive()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}

			mVideoDecodeThread[i] = new VideoDecodeThread(mSurfaceHolder[i].getSurface(), PATH[i], 0, -1, new PlayCompleteListener() {

				@Override
				public void playComplete() {
					// 重复播放
					Log.v("bob", "isFinish == " + isFinish + "  ISREPEATE = " + ISREPEATE);
					if (!isFinish && ISREPEATE) {

						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								play(i, mAudioDecodeThread[i] == null ? false : mAudioDecodeThread[i].isMute());
							}
						}, 100);

					}
				}
			});
			mVideoDecodeThread[i].start();
		}
		Log.v("bob", "startPlay-------------ISAUDIO = ");
		if (ISAUDIO) {

			if (mAudioDecodeThread[i] == null || !mAudioDecodeThread[i].isAlive()) {
				while (mVideoDecodeThread[i].startMs == 0) {
					try {
						if (mVideoDecodeThread[i].startMs == -1) {
							return;
						}
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
			Log.v("bob", "startPlay-------------ISAUDIO = 1 ");
			mAudioDecodeThread[i] = new AudioDecodeThread(PATH[i], mVideoDecodeThread[i].startMs, 0, -1);
			mAudioDecodeThread[i].start();

			while (!mVideoDecodeThread[i].isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			// 设置声音开关
			mAudioDecodeThread[i].setMute(isAudio);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isFinish = true;
		forceStopAllDecodeThread();
		super.onPause();
	}

	private void forceStopAllDecodeThread() {
		int timeout = 5;
		for (int i = 0; i < MAXCOUNT; i++) {
			if (mVideoDecodeThread[i] != null) {
				if (mVideoDecodeThread[i].isAlive()) {
					mVideoDecodeThread[i].stopThread();
				}
			}
			if (ISAUDIO) {
				if (mAudioDecodeThread[i] != null) {
					if (mAudioDecodeThread[i].isAlive()) {
						mAudioDecodeThread[i].stopThread();
					}
				}
			}
		}

		for (int i = 0; i < MAXCOUNT; i++) {
			timeout = 5;
			if (mVideoDecodeThread[i] != null) {
				while (mVideoDecodeThread[i].isAlive()) {
					try {
						Thread.sleep(500);
						timeout--;
					} catch (InterruptedException ex) {
					}

					if (timeout < 0) {
						mVideoDecodeThread[i].releaseResource();
						break;
					}
				}
				mVideoDecodeThread[i] = null;
			}
			if (ISAUDIO) {
				timeout = 5;
				if (mAudioDecodeThread[i] != null) {
					while (mAudioDecodeThread[i].isAlive()) {
						try {
							Thread.sleep(500);
							timeout--;
						} catch (InterruptedException ex) {
						}

						if (timeout < 0) {
							mAudioDecodeThread[i].releaseResource();
							break;
						}
					}
					mAudioDecodeThread[i] = null;
				}
			}
		}
		Log.v("bob", "exit-------------");
	}

}
