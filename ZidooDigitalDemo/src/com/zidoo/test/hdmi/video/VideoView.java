/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoView extends SurfaceView {
	private Uri							mUri						= null;
	private String						mPath						= null;
	private SurfaceHolder				mSurfaceHolder				= null;
	public MediaPlayer					mMediaPlayer				= null;
	private OnPreparedListener			mOnPreparedListener			= null;
	private OnErrorListener				mOnErrorListener			= null;
	private OnInfoListener				mOnInfoListener				= null;
	private OnBufferingUpdateListener	mOnBufferingUpdateListener	= null;
	private OnCompletionListener		mCompletionListener			= null;
	private OnSeekCompleteListener		mSeekCompleteListener		= null;
	private Context						mContext					= null;

	public VideoView(Context context) {
		super(context);
		initVideoView(context);
	}

	public VideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initVideoView(context);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVideoView(context);
	}

	private void initVideoView(Context ctx) {
		mContext = ctx;
		getHolder().addCallback(mSHCallback);
		requestFocus();
		if (ctx instanceof Activity)
			((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	public void setVideoUrl(String url) {
		try {
			mUri = Uri.parse(url);
			openVideo();
		} catch (Exception e) {
		}
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.pause();
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public void seekTo(long seek) {
		if (mMediaPlayer != null) {
			mMediaPlayer.seekTo((int) seek);
		}
	}

	public void start() {
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}

	public void pause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null)
			return;
		// Intent i = new Intent("com.android.music.musicservicecommand");
		// i.putExtra("command", "pause");
		// mContext.sendBroadcast(i);
		try {
			mMediaPlayer = new MediaPlayer();
//			AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//			if (am.isSpeakerphoneOn()) {
//				am.setSpeakerphoneOn(false);
//			}
//			am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
//			am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,0, AudioManager.FLAG_PLAY_SOUND); 
			// mMediaPlayer.setAudioStreamType(streamtype)
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnErrorListener(mOnErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
			mMediaPlayer.setOnInfoListener(mOnInfoListener);
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	OnPreparedListener	mPreparedListener	= new OnPreparedListener() {
												public void onPrepared(MediaPlayer mp) {
													mMediaPlayer.start();
													if (mOnPreparedListener != null)
														mOnPreparedListener.onPrepared(mMediaPlayer);
												}
											};

	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		mSeekCompleteListener = l;
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		mCompletionListener = l;
	}

	public void setOnPreparedListener(OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		mOnBufferingUpdateListener = l;
	}

	public void setOnInfoListener(OnInfoListener l) {
		mOnInfoListener = l;
	}

	SurfaceHolder.Callback		mSHCallback					= new SurfaceHolder.Callback() {
																public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
																	// holder.setFixedSize(w,
																	// h);
																}

																public void surfaceCreated(SurfaceHolder holder) {
																	mSurfaceHolder = holder;
																	if (mMediaPlayer != null) {
																		mMediaPlayer.setDisplay(mSurfaceHolder);
																	} else {
																		if (mUri != null) {
																			openVideo();
																		}
																	}
																}

																public void surfaceDestroyed(SurfaceHolder holder) {
																	mSurfaceHolder = null;
																}
															};

	OnVideoSizeChangedListener	onVideoSizeChangedListener	= new OnVideoSizeChangedListener() {
																public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
																}
															};
}
