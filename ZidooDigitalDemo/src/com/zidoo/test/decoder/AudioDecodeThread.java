/**
 * Copyright (C) 2016 Zidoo (www.zidoo.tv)
 * Created by : jiangbo@zidoo.tv
 */

package com.zidoo.test.decoder;

import java.io.IOException;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.util.Log;
import android.os.Handler;
import android.os.HandlerThread;


public class AudioDecodeThread extends Thread {
    public static final String TAG = "AudioDecodeThread";
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private String audioPath;
    private AudioTrack mplayAudioTrack = null;
    private MediaFormat mMediaFormat;
    private boolean eosThread = false;
    private boolean muteFlag = true;
    private boolean formatInited = false;
    private int freeRun = 0;
    private int outOfTimeThreshold = 30;//ms
    private int terminateTimeout = 3;
    public long startMs = 0;
    public int flag = 0;
    public long curTimeStamp = 0;
    private int audioSessionId = 0;
    private int bufferSize;
    private boolean isEOS = false;
    private BufferInfo decoderInfo;
    private Handler decoderHandler;
    private HandlerThread decoderCallbackThread;
    private boolean isCodecErr = false;
    private boolean mStartRelease = false;

    public AudioDecodeThread(String path, long startTime, int freeRun, int audioSessionId) {
        this.audioPath = path;
        this.startMs = startTime;
        this.freeRun = freeRun;
        this.audioSessionId = audioSessionId;
        Log.d(TAG, "audioSessionId " + audioSessionId);
    }

    @Override
    public void run() {
        extractor = new MediaExtractor();
        Log.d(TAG, "Start setDataSource ... " + audioPath);
        try {
            extractor.setDataSource(audioPath);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            releaseResource();
            return;
        }
        Log.d(TAG, "End setDataSource ... " + audioPath);
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                extractor.selectTrack(i);
                try {
                    decoder = MediaCodec.createDecoderByType(mime);
                    Log.d(TAG, "create decoder with mime " + mime + audioPath);
                } catch (Exception e) {
                    Log.e(TAG, "Create codec from type ERROR !!! " + audioPath);
                    releaseResource();
                    return;
                }
                if(audioSessionId != -1)
                {
                    if(decoder.getName().contains("google"))//SW decode not support tunnel mode
                        audioSessionId = -1;
                    else
                        format.setInteger(MediaFormat.KEY_AUDIO_SESSION_ID, audioSessionId);
                }
                mMediaFormat = format;
                decoderCallbackThread = new HandlerThread("DecoderHanlderThread");
                decoderCallbackThread.start();
                decoderHandler = new Handler(decoderCallbackThread.getLooper());
                
                setupDecoderCallback(decoderHandler);               
                decoder.configure(mMediaFormat, null, null, 0);
                break;
            }
        }

        if (decoder == null) {
            Log.e(TAG, "Create decoder FAIL !! " + audioPath);
            releaseResource();
            return;
        }

        int channelCount = mMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int channelMaskConfig = 0;

        switch(channelCount)
        {
            case 1:
                channelMaskConfig = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channelMaskConfig = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case 3:
                channelMaskConfig = (AudioFormat.CHANNEL_OUT_STEREO | AudioFormat.CHANNEL_OUT_FRONT_CENTER);
                break;
            case 4:
                channelMaskConfig = AudioFormat.CHANNEL_OUT_QUAD;
                break;
            case 5:
                channelMaskConfig = (AudioFormat.CHANNEL_OUT_QUAD | AudioFormat.CHANNEL_OUT_FRONT_CENTER);
                break;
            case 6:
                channelMaskConfig = AudioFormat.CHANNEL_OUT_5POINT1;
                break;
            case 7:
                channelMaskConfig = (AudioFormat.CHANNEL_OUT_5POINT1 | AudioFormat.CHANNEL_OUT_BACK_CENTER);
                break;
            case 8:
            	//AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
                channelMaskConfig = AudioFormat.CHANNEL_OUT_7POINT1_SURROUND;
                break;
            default:
                channelMaskConfig = AudioFormat.CHANNEL_OUT_STEREO;
                break;
        }
        int sampleRate = mMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                channelMaskConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        bufferSize = 4 * minBufferSize;
        if(audioSessionId == -1)
        {
            mplayAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate,
            channelMaskConfig,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize, AudioTrack.MODE_STREAM);
        }
        else
        {
            AudioAttributes audioAttributes = (new AudioAttributes.Builder())
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .setFlags(AudioAttributes.FLAG_HW_AV_SYNC)
                            .build();
            AudioFormat audioFormat = (new AudioFormat.Builder())
                            .setChannelMask(channelMaskConfig)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .build();
            mplayAudioTrack = new AudioTrack(audioAttributes, audioFormat, bufferSize,
                            AudioTrack.MODE_STREAM, audioSessionId);
        }
        
        mplayAudioTrack.play();
        decoderInfo = new BufferInfo();
        decoder.start();
        flag = 1;
        while (true) {
            if ((decoderInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM " + audioPath);
                break;
            }
            else if((isEOS == true) || eosThread == true)
            {
                if(audioSessionId != -1)
                    break;
                Log.d(TAG, "Waiting for OutputBuffer BUFFER_FLAG_END_OF_STREAM " + audioPath);
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
        if(mStartRelease == false) {
            mStartRelease = true;
            Log.d(TAG, "Audio Thread start releasing !! " + audioPath);
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

            if(mplayAudioTrack != null) {
                mplayAudioTrack.stop();
                mplayAudioTrack.release();
            }
            if(decoderCallbackThread != null)
            {
                decoderCallbackThread.quitSafely();
                try {
                    decoderCallbackThread.join();
                    } catch(InterruptedException  ex) {
                    }
            }
        }
        flag = -1;
        Log.d(TAG, "Audio Thread normally Stop !! " + audioPath);
    }

    private void setupDecoderCallback(Handler handle)
    {
        decoder.setCallback(new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec mc, int inputBufferId) 
        {
            if(mStartRelease == true)
                return;
            if (!isEOS) {
                ByteBuffer buffer = mc.getInputBuffer(inputBufferId);
                int sampleSize = extractor.readSampleData(buffer, 0);
                flag = 2;
                if (sampleSize < 0) {
                    Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM " + audioPath);
                    mc.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isEOS = true;
                } else {
                    if(eosThread && formatInited)
                    {
                        Log.d(TAG, "InputBuffer force BUFFER_FLAG_END_OF_STREAM " + audioPath);
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
            if(mStartRelease == true)
                return;
            ByteBuffer buffer = mc.getOutputBuffer(outputBufferId);
            curTimeStamp = info.presentationTimeUs;
            if(audioSessionId == -1)
            {
                if(freeRun == 0 && ((info.presentationTimeUs / 1000) + outOfTimeThreshold < System.currentTimeMillis() - startMs))
                {
                    Log.v(TAG, "Audio packet too late drop it ... " + audioPath);
                    mc.releaseOutputBuffer(outputBufferId, false);
                    flag = 5;
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
            }
            
            if(info.size > 0 && muteFlag)
            {
                if(info.size < bufferSize)
                {
                    mplayAudioTrack.write(buffer, info.size, AudioTrack.WRITE_NON_BLOCKING);
                    flag = 4;
                }
                else
                    Log.v(TAG, "Audio buffer " + info.size +"over buffersize " + bufferSize + " " + audioPath);
                buffer.clear();
            }
            if(mStartRelease == false)
                mc.releaseOutputBuffer(outputBufferId, false);
            flag = 5;
        }

        @Override
        public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) 
        {
            Log.d(TAG, "New format " + decoder.getOutputFormat() + audioPath);
            formatInited = true;
            mMediaFormat = decoder.getOutputFormat();
            mplayAudioTrack.setPlaybackRate(mMediaFormat
                        .getInteger(MediaFormat.KEY_SAMPLE_RATE));
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e)
        {
            isCodecErr = true;
            e.printStackTrace();
        }
        }, handle);
    }
    
    public boolean switchMute() {
        if(muteFlag == true)
            muteFlag = false;
        else
            muteFlag = true;
        Log.d(TAG, "Audio switchMute " + muteFlag + "  " + audioPath);
        return muteFlag;
    }
    
    public void setMute(boolean muteFlag) {
    	this.muteFlag = muteFlag;
    }

    
    public boolean isMute()
    {
    	return muteFlag;
    }
    
    public void stopThread() {
        eosThread = true;
    }
}
