package com.air.player.player;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;


import com.air.player.model.NALPacket;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoPlayer {
    private static final String TAG = "VideoPlayer";
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static final int mVideoWidth = 1080;
    public static final int mVideoHeight = 1920;
    private final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec mDecoder = null;
    private final Surface mSurface;
    private BlockingQueue<NALPacket> packets = new LinkedBlockingQueue<>(500);
    private final HandlerThread mDecodeThread = new HandlerThread("VideoDecoder");

    private final MediaCodec.Callback mDecoderCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            try {
                NALPacket packet = packets.take();
                codec.getInputBuffer(index).put(packet.nalData);
                mDecoder.queueInputBuffer(index, 0, packet.nalData.length, packet.pts, 0);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted when is waiting");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            try {
                codec.releaseOutputBuffer(index, true);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            Log.e(TAG, "Decode error", e);
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

        }
    };

    public VideoPlayer(Surface surface, int width, int heigth) {
//        this.mVideoWidth=width;
//        this.mVideoHeight=heigth;
        mSurface = surface;
    }

    public void initDecoder() {
        mDecodeThread.start();
        try {
            // 解码分辨率
            Log.i(TAG, "initDecoder: mVideoWidth=" + mVideoWidth + "---mVideoHeight=" + mVideoHeight);
            mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mVideoWidth, mVideoHeight);
            mDecoder.configure(format, mSurface, null, 0);
            mDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mDecoder.setCallback(mDecoderCallback, new Handler(mDecodeThread.getLooper()));
            mDecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPacker(NALPacket nalPacket) {
        try {
            packets.put(nalPacket);
        } catch (InterruptedException e) {
            // 队列满了
            Log.e(TAG, "run: put error:", e);
        }
    }

    public void start() {
        initDecoder();
    }

    public void stopVideoPlay() {
        try {
            mDecoder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mDecoder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDecodeThread.quit();
        packets.clear();
    }

}
