package com.air.player.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;


import com.air.player.model.AACPacket;
import com.air.player.model.NALPacket;
import com.air.player.model.PCMPacket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioPlayer extends Thread {
    private static final String TAG = "AudioPlayer";
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;

    private final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private MediaCodec mDecoder = null;
    private BlockingQueue<AACPacket> packets = new LinkedBlockingQueue<>(500);
    private final HandlerThread mDecodeThread = new HandlerThread("AudioDecoder");

    private final MediaCodec.Callback mDecoderCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            try {
                AACPacket packet = packets.take();
                codec.getInputBuffer(index).put(packet.data);
                mDecoder.queueInputBuffer(index, 0, packet.data.length, packet.pts, 0);
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

    public void initDecoder() {
        mDecodeThread.start();
        try {
            // 解码分辨率
            mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mVideoWidth, mVideoHeight);
            mDecoder.configure(format, mSurface, null, 0);
            mDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mDecoder.setCallback(mDecoderCallback, new Handler(mDecodeThread.getLooper()));
            mDecoder.start();

            MediaFormat mediaFormat  = MediaFormat.createAudioFormat(mediaType,kSampleRates[3],1);
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, kBitRates[1]);
            mDecoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mDecoder.set
            mDecoder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPacker(AACPacket nalPacket) {
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
