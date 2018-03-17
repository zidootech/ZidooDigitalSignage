/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.widget.VideoView;


public class VideoDecodeThread extends Thread {
    public static final String TAG = "VideoDecodeThread";
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private Surface surface;
    private String videoPath;
    private boolean eosThread = false;
    private boolean formatInited = false;
    private int freeRun = 0;
    private int outOfTimeThreshold = 30;//ms
    public int flag = 0;
    public long startMs = 0;//ms
    public long curTimeStamp = 0;
    private int terminateTimeout = 3;
    private int audioSessionID;
    private boolean isEOS = false;
    private BufferInfo decoderInfo;
    private HandlerThread decoderCallbackThread;
    private Handler decoderHandler;
    private boolean isCodecErr = false;
    private boolean mStartRealease = false;
    private PlayCompleteListener mPlayCompleteListener = null;
    
    public interface PlayCompleteListener
    {
    	public void playComplete();
    }
    
    public VideoDecodeThread(Surface surface, String path, int freeRun, int audioSessionId,PlayCompleteListener playCompleteListener) {
        this.surface = surface;
        this.videoPath = path;
        this.freeRun = freeRun;
        this.audioSessionID = audioSessionId;
        this.mPlayCompleteListener = playCompleteListener;
        Log.d(TAG, "audioSessionID " + this.audioSessionID);
    }

    @Override
    public void run() {
        extractor = new MediaExtractor();
        Log.d(TAG, "Start setDataSource ... " + videoPath);
        try {
            extractor.setDataSource(videoPath);
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.v("bob", "extractor.setDataSource != error");
            releaseResource();
            return;
        }
        Log.d(TAG, "End setDataSource ... " + videoPath);
        
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mime type " + mime);
            Log.v("bob", "extractor.getTrackCount() i = "+i);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                try {
                      if(audioSessionID != -1)
                      {
                         format.setFeatureEnabled(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback, true);
                         format.setInteger(MediaFormat.KEY_AUDIO_SESSION_ID, audioSessionID);
                      }
                      format.setInteger(MediaFormat.KEY_PUSH_BLANK_BUFFERS_ON_STOP,1);
                      decoder = MediaCodec.createDecoderByType(mime);
                      Log.d(TAG, "create decoder with mime " + mime);
                } catch (Exception e) {
                    Log.e(TAG, "Create decoder codec from type ERROR !!!");
                    releaseResource();
                    return;
                }

                decoderCallbackThread = new HandlerThread("DecoderHanlderThread");
                decoderCallbackThread.start();
                decoderHandler = new Handler(decoderCallbackThread.getLooper());
                setupDecoderCallback(decoderHandler);
                decoder.configure(format, surface, null, 0);
                break;
            }
        }

        if (decoder == null) {
        	Log.v("bob", "video decoder == null");
            Log.e(TAG, "Create decoder FAIL !! " + videoPath);
            releaseResource();
            return;
        }
        Log.v("bob", "video decoder != null");
        decoderInfo = new BufferInfo();
        decoder.start();
        startMs = System.currentTimeMillis();
        flag = 1;
    	Log.v("bob", "true = ---------------------------------------------------");
        while (true) {
            if ((decoderInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM " + videoPath);
                break;
            }
            else if(isEOS == true || eosThread == true)
            {
                if(audioSessionID != -1)
                    break;
                Log.d(TAG, "Waiting for OutputBuffer BUFFER_FLAG_END_OF_STREAM " + videoPath);
                terminateTimeout--;
                if(terminateTimeout < 0)
                {
                    Log.d(TAG, "Terminate thread occur TIMEOUT !! ");
                    break;
                }
            }

            try {
                    Thread.sleep(1000);
                } catch(InterruptedException  ex) {
                }
        }
        releaseResource();
    }

    public void releaseResource()
    {
        if(eosThread == false)
            eosThread = true;
        if(mStartRealease == false) {
            Log.d(TAG, "Video Thread start releasing !! " + videoPath);
            mStartRealease = true;
            if(decoder != null)
            {
                if(isCodecErr == false) {
                    decoder.flush();
                    decoder.stop();
                }
                decoder.release();
            }

            if(extractor != null)
                extractor.release();

            if(decoderCallbackThread != null)
            {
                decoderCallbackThread.quitSafely();
                try {
                    decoderCallbackThread.join();
                    } catch(InterruptedException  ex) {
                    }
            }
            flag = -1;
        }
        Log.d(TAG, "Video Thread normally Stop !! " + videoPath);
        Log.v("bob", "Video Thread normally Stop !! ");
        startMs = -1;
        if (mPlayCompleteListener != null) {
        	Log.v("bob", "mPlayCompleteListener != null");
        	mPlayCompleteListener.playComplete();
		}
    }

    private void setupDecoderCallback(Handler handle)
    {
        decoder.setCallback(new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) 
        {
            if(mStartRealease == true)
                return;
            if (!isEOS) {
                ByteBuffer buffer = mc.getInputBuffer(inputBufferId);
                int sampleSize = extractor.readSampleData(buffer, 0);
                flag = 2;
                if (sampleSize < 0) {
                    Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM " + videoPath);
                    mc.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isEOS = true;
                } else {
                    if(eosThread && formatInited)
                    {
                        Log.d(TAG, "InputBuffer force BUFFER_FLAG_END_OF_STREAM " + videoPath);
                        mc.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.getSampleTime(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    }
                    else
                    {
                        mc.queueInputBuffer(inputBufferId, 0, sampleSize, extractor.getSampleTime(), 0);
                        flag = 3;
                    }
                    extractor.advance();
                }
            }
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info) 
        {
            decoderInfo = info;
            if(mStartRealease == true)
                return;
            ByteBuffer buffer = mc.getOutputBuffer(outputBufferId);
            curTimeStamp = info.presentationTimeUs;
            if(freeRun == 0 && ((info.presentationTimeUs / 1000) + outOfTimeThreshold < System.currentTimeMillis() - startMs))
            {
                Log.v(TAG, "Video packet too late drop it ... " + videoPath);
                mc.releaseOutputBuffer(outputBufferId, false);
                flag = 4;
                return;
            }

            while ((info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) && !eosThread)
            {
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            if(mStartRealease == false)
                mc.releaseOutputBuffer(outputBufferId, true);
            flag = 4;
        }

        @Override
        public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) 
        {
            Log.d(TAG, "New format " + decoder.getOutputFormat() + videoPath);
            formatInited = true;
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e)
        {
            isCodecErr = true;
            e.printStackTrace();
        }
        }, handle);
    }

    public void stopThread() {
        eosThread = true;
    }
}
