/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.hdmi.pip;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.zuishare_test.R;
import com.realtek.hardware.RtkHDMIRxManager;
import com.realtek.server.HDMIRxParameters;
import com.realtek.server.HDMIRxStatus;
import com.zidoo.test.zidooutil.MyLog;

public class PiPService extends Service {
	private final static String				TAG						= "PiPService";
	private IBinder							mBinder					= new LocalBinder();
	private Context							mContext				= null;
	private RtkHDMIRxManager				mHDMIRX					= null;
	private boolean							mIsPlaying				= false;
	// hdmi info
	private int								mFps					= 0;
	private int								mRecordFrameRate		= 0;
	private int								mWidth					= 0;
	private int								mHeight					= 0;
	// hdmi display
	private static final int				TYPE_SURFACEVIEW		= 0;
	private static final int				TYPE_TEXTUREVIEW		= 1;
	private int								mViewType				= TYPE_SURFACEVIEW;
	private WindowManager					mWindowManager			= null;
	private FloatingWindowView				mFloatingView			= null;
	private TextView						mSigleView				= null;
	public View								mPreview				= null;
	private SurfaceView						mSurfaceView			= null;
	private SurfaceHolder					mSurfaceHolder			= null;
	public FloatingWindowSurfaceCallback	mCallback				= null;
	public TextureView						mTextureView			= null;
	public FloatingWindowTextureListener	mListener				= null;
	public WindowManager.LayoutParams		wmParams				= null;
	// hdmi statu
	private boolean							isPreviewOn				= false;
	private boolean							isPip					= false;
	private boolean							isHdmiConnect			= false;
	private BroadcastReceiver				mHdmiRxHotPlugReceiver	= null;
	private Handler							mHandler				= null;
	private final static int				DISPLAY					= 0;
	private final static int				CLOSESOUND				= 1;
	private final static int				DISPLAYTIME				= 200;

	public class LocalBinder extends Binder {

		public PiPService getService() {

			return PiPService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private void init() {
		mContext = this;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DISPLAY: {
					if (isHdmiConnect) {
						play();
					}
				}
					break;
				case CLOSESOUND: {
					togglePreview(true, FloatingWindowView.isPipSound(mContext));
				}
					break;
				default:
					break;
				}
			}
		};
		createFloatingWindow();
		initHdmiConnect();
		play();
	}

	@Override
	public void onDestroy() {
		MyLog.v(TAG, "onDestroy ");
		release();
		super.onDestroy();
	}

	public void initHdmiConnect() {
		mHdmiRxHotPlugReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				boolean hdmiRxPlugged = intent.getBooleanExtra(HDMIRxStatus.EXTRA_HDMIRX_PLUGGED_STATE, false);
				MyLog.v(TAG, "RecorderInterface onReceive hdmiRxPlugged = " + hdmiRxPlugged);
				isHdmiConnect = hdmiRxPlugged;
				hdmiConncet(hdmiRxPlugged);
			}
		};

		isHdmiConnect = isConnect(mContext);

		IntentFilter hdmiRxFilter = new IntentFilter(HDMIRxStatus.ACTION_HDMIRX_PLUGGED);
		mContext.registerReceiver(mHdmiRxHotPlugReceiver, hdmiRxFilter);
	}

	public static boolean isConnect(Context context) {
		IntentFilter intentFilter = new IntentFilter(HDMIRxStatus.ACTION_HDMIRX_PLUGGED);
		Intent batteryStatus = context.registerReceiver(null, intentFilter);
		boolean hdmiRxPlugged = batteryStatus.getBooleanExtra(HDMIRxStatus.EXTRA_HDMIRX_PLUGGED_STATE, false);
		return hdmiRxPlugged;
	}

	public void hdmiConncet(boolean isConnect) {
		if (mSigleView != null) {
			mSigleView.setVisibility(isConnect ? View.GONE : View.VISIBLE);
		}
		if (isConnect) {
			play();
		} else {
			stop();
		}
	}

	public boolean play() {
		if (mWindowManager == null) {
			return false;
		}

		mPreview.setVisibility(View.VISIBLE);
		mHandler.removeMessages(DISPLAY);
		MyLog.v("play------------- mIsPlaying = " + mIsPlaying + " mPreviewOn = " + isPreviewOn);
		if (!mIsPlaying && isPreviewOn) {
			mHDMIRX = new RtkHDMIRxManager();
			HDMIRxStatus rxStatus = mHDMIRX.getHDMIRxStatus();
			if (rxStatus != null && rxStatus.status == HDMIRxStatus.STATUS_READY) {
				if (mHDMIRX.open() != 0) {
					mWidth = 0;
					mHeight = 0;
					mHDMIRX = null;
					mHandler.sendEmptyMessageDelayed(DISPLAY, DISPLAYTIME);
					return false;
				}
				HDMIRxParameters hdmirxGetParam = mHDMIRX.getParameters();
				getSupportedPreviewSize(hdmirxGetParam, rxStatus.width, rxStatus.height);
				mFps = getSupportedPreviewFrameRate(hdmirxGetParam);
			} else {
				mHandler.sendEmptyMessageDelayed(DISPLAY, DISPLAYTIME);
				return false;
			}
			try {
				if (mViewType == TYPE_SURFACEVIEW) {
					mHDMIRX.setPreviewDisplay(mSurfaceHolder);
				} else if (mViewType == TYPE_TEXTUREVIEW) {
					SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
					// mTextureView.setRotation(180);
					mHDMIRX.setPreviewDisplay3(surfaceTexture);
				}
				// configureTargetFormat
				HDMIRxParameters hdmirxParam = new HDMIRxParameters();
				MyLog.v(TAG, "hdmi setPreviewSize  mWidth = " + mWidth + "  mHeight = " + mHeight + "  mFps = " + mFps);
				hdmirxParam.setPreviewSize(mWidth, mHeight);
				mRecordFrameRate = mFps;
				if (mFps == 60) {
					mRecordFrameRate = 30;
				} else if (mFps == 50) {
					mRecordFrameRate = 25;
				}
				hdmirxParam.setPreviewFrameRate(mRecordFrameRate);
				// set sorce format
				mHDMIRX.setParameters(hdmirxParam);
				// configureTargetFormat end
				mHDMIRX.play();
				mIsPlaying = true;
				MyLog.v(TAG, "hdmi mIsPlaying  successfull");
				togglePreview(true, FloatingWindowView.isPipSound(mContext));
				// Make sure pip sound is displayed
				mHandler.sendEmptyMessageDelayed(CLOSESOUND, DISPLAYTIME);
			} catch (Exception e) {
				stop();
				e.printStackTrace();
				MyLog.e(TAG, "play erro = " + e.getMessage());
			}
		} else if (!isPreviewOn) {
			mHandler.sendEmptyMessageDelayed(DISPLAY, DISPLAYTIME);
			return false;
		} else {
			return false;
		}
		return true;
	}

	public boolean isPip() {
		return isPip;
	}

	public boolean stopPip() {
		isPip = false;
		release();
		return true;
	}

	public boolean startPip() {
		init();
		isPip = true;
		return true;
	}

	public void release() {
		MyLog.v(TAG, "release()------------------");
		try {
			if (mHdmiRxHotPlugReceiver != null) {
				mContext.unregisterReceiver(mHdmiRxHotPlugReceiver);
				mHdmiRxHotPlugReceiver = null;
				stop();
				if (mViewType == TYPE_SURFACEVIEW) {
					if (mSurfaceView != null && mSurfaceHolder != null && mCallback != null) {
						mSurfaceHolder.removeCallback(mCallback);
					}
				}
				if (mWindowManager != null && mFloatingView != null) {
					mWindowManager.removeView(mFloatingView);
					mFloatingView = null;
					mWindowManager = null;
				}
			}
			MyLog.v("stopService RecorderService successfull");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean stop() {
		mHandler.removeMessages(DISPLAY);
		if (mPreview != null) {
			mPreview.setVisibility(View.INVISIBLE);
		}
		boolean rlt = true;
		if (mHDMIRX != null) {
			mHDMIRX.stop();
			mHDMIRX.release();
			mHDMIRX = null;
		} else {
			rlt = false;
		}
		mIsPlaying = false;
		mFps = 0;
		mRecordFrameRate = 0;
		mWidth = 0;
		mHeight = 0;
		return rlt;
	}

	private void getSupportedPreviewSize(HDMIRxParameters hdmirxGetParam, int rxWidth, int rxHeight) {
		List<com.realtek.hardware.RtkHDMIRxManager.Size> previewSizes = hdmirxGetParam.getSupportedPreviewSizes();
		int retWidth = 0, retHeight = 0;
		if (previewSizes == null || previewSizes.size() <= 0)
			return;
		for (int i = 0; i < previewSizes.size(); i++) {
			if (previewSizes.get(i) != null && rxWidth == previewSizes.get(i).width) {
				retWidth = previewSizes.get(i).width;
				retHeight = previewSizes.get(i).height;
				if (rxHeight == previewSizes.get(i).height)
					break;
			}
		}
		if (retWidth == 0 && retHeight == 0) {
			if (previewSizes.get(previewSizes.size() - 1) != null) {
				retWidth = previewSizes.get(previewSizes.size() - 1).width;
				retHeight = previewSizes.get(previewSizes.size() - 1).height;
			}
		}

		mWidth = retWidth;
		mHeight = retHeight;
		MyLog.v(TAG, "input = mWidth = " + mWidth + "  mHeight = " + mHeight);
		// jiangbo
		if (mWidth > 1920) {
			mWidth = 1920;
			mHeight = 1080;
		}
		// jiangbo
	}

	private int getSupportedPreviewFrameRate(HDMIRxParameters hdmirxGetParam) {
		List<Integer> previewFrameRates = hdmirxGetParam.getSupportedPreviewFrameRates();
		int fps = 0;
		if (previewFrameRates != null && previewFrameRates.size() > 0)
			fps = previewFrameRates.get(previewFrameRates.size() - 1);
		else
			fps = 30;
		MyLog.v(TAG, "input = fps = " + fps);
		return fps;
	}

	private void createFloatingWindow() {
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mFloatingView = (FloatingWindowView) View.inflate(mContext, R.layout.floatingwindow, null);
		mSigleView = (TextView) mFloatingView.findViewById(R.id.home_ac_surfaceview_no_sigle);
		mFloatingView.mNoHdmiSignalView = mSigleView;
		mFloatingView.setOperateView(mFloatingView.findViewById(R.id.floating_recording_operate_view), this);
		ViewGroup surfaceRootview = (ViewGroup) mFloatingView.findViewById(R.id.floating_surfaceview);
		mSigleView.setVisibility(isHdmiConnect ? View.GONE : View.VISIBLE);
		// setup view type
		// setup view type
		if (mViewType == TYPE_SURFACEVIEW) {
			mSurfaceView = new SurfaceView(mContext);
			mSurfaceHolder = mSurfaceView.getHolder();
			mCallback = new FloatingWindowSurfaceCallback();
			mSurfaceHolder.addCallback(mCallback);
			mPreview = mSurfaceView;
		} else if (mViewType == TYPE_TEXTUREVIEW) {
			mTextureView = new TextureView(mContext);
			mListener = new FloatingWindowTextureListener();
			mTextureView.setSurfaceTextureListener(mListener);
			mPreview = mTextureView;
		}

		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mPreview.setLayoutParams(param);
		surfaceRootview.addView(mPreview);

		int flag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
		// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams = new WindowManager.LayoutParams(FloatingWindowView.getPIPW(mContext), // 192*3,//WindowManager.LayoutParams.MATCH_PARENT,
				FloatingWindowView.getPIPH(mContext), // 108*3,//WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_TOAST, flag, PixelFormat.TRANSLUCENT);
		wmParams.x = FloatingWindowView.getPIPX(mContext);
		wmParams.y = FloatingWindowView.getPIPY(mContext);
		wmParams.gravity = Gravity.TOP | Gravity.LEFT;
		mFloatingView.setBackgroundColor(Color.BLACK);
		try {
			// View thisView = getWindow().getDecorView();
			mFloatingView.setSystemUiVisibility((View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mWindowManager.addView(mFloatingView, wmParams);

	}

	class FloatingWindowTextureListener implements TextureView.SurfaceTextureListener {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			isPreviewOn = true;
			MyLog.v(TAG, "SurfaceTextureListener onSurfaceTextureAvailable");
			// play();
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			MyLog.v(TAG, "SurfaceTextureListener onSurfaceTextureDestroyed");
			// stop();
			isPreviewOn = false;
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	}

	class FloatingWindowSurfaceCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			MyLog.v(TAG, "SurfaceHolder surfaceCreated");
			isPreviewOn = true;
			// play();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			MyLog.v(TAG, "SurfaceHolder surfaceDestroyed");
			// stop();
			isPreviewOn = false;
		}
	}

	// video display or audio display
	public void togglePreview(boolean videoEn, boolean audioEn) {
		if (mHDMIRX != null && mIsPlaying) {
			mHDMIRX.setPlayback(videoEn, audioEn);
		}
	}

}
