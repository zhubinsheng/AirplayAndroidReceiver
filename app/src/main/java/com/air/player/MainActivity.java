package com.air.player;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.air.player.model.AACPacket;
import com.air.player.model.NALPacket;
import com.air.player.model.PCMPacket;
import com.air.player.player.AudioPlayer;
import com.air.player.player.VideoPlayer;
import com.air.player.utils.FileWriteHelper;
import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import com.github.serezhka.airplay.server.AirPlayConfig;
import com.github.serezhka.airplay.server.AirPlayServer;
import com.github.serezhka.airplay.server.AirPlayConsumer;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.util.LinkedList;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private AspectFrameLayout mSurfaceView;
    private AirPlayServer airPlayServer;
    private static String TAG = "airplay";
    private VideoPlayer mVideoPlayer;
    private AudioPlayer mAudioPlayer;
    private final LinkedList<NALPacket> mVideoCacheList = new LinkedList<>();

    private FileWriteHelper fileWriteHelper = FileWriteHelper.createNewHelper();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setAspectRatio(9f/16f);
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.start();

        AirPlayConfig airPlayConfig = new AirPlayConfig();
        airPlayConfig.setServerName("hero");
        airPlayConfig.setWidth(VideoPlayer.mVideoWidth);
        airPlayConfig.setHeight(VideoPlayer.mVideoHeight);
        airPlayConfig.setFps(45);

        airPlayServer = new AirPlayServer(airPlayConfig, airplayDataConsumer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    airPlayServer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "start-server-thread").start();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        fileWriteHelper.createAudioFile(this, "test");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioPlayer.stopPlay();
        mAudioPlayer = null;
        mVideoPlayer.stopVideoPlay();
        mVideoPlayer = null;
        airplayDataConsumer = null;
        airPlayServer.stop();
    }
    private AirPlayConsumer airplayDataConsumer = new AirPlayConsumer() {
        @Override
        public void onVideo(byte[] video) {
            Log.i(TAG, "rev video length: " + video.length);
            NALPacket nalPacket=new NALPacket();
            nalPacket.nalData=video;
            if (mVideoPlayer != null) {
                while (!mVideoCacheList.isEmpty()) {
                    mVideoPlayer.addPacker(mVideoCacheList.removeFirst());
                }
                mVideoPlayer.addPacker(nalPacket);
            } else {
                mVideoCacheList.add(nalPacket);
            }

        }

        @Override
        public void onVideoSrcDisconnect() {

        }

        @Override
        public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
            Log.i(TAG, "rev onVideoFormat: " + videoStreamInfo.toString());
        }

        @Override
        public void onAudio(byte[] audio) {
            Log.i(TAG, "rev audio length: " + audio.length);
            AACPacket pcmPacket=new AACPacket();
            pcmPacket.data=audio;

            fileWriteHelper.write2File(audio);
            if (mAudioPlayer != null) {
                mAudioPlayer.addPacker(pcmPacket);
            }
            FFmpegFrameGrabber fFmpegFrameRecorder = new FFmpegFrameGrabber();
            fFmpegFrameRecorder.s
        }

        @Override
        public void onAudioSrcDisconnect() {

        }

        @Override
        public void onMediaPlaylist(String playlistUri) {
            Log.i(TAG, "rev onMediaPlaylist: " + playlistUri.toString());
            AirPlayConsumer.super.onMediaPlaylist(playlistUri);
        }

        @Override
        public void onAudioFormat(AudioStreamInfo audioInfo) {
            Log.i(TAG, "rev AudioStreamInfo: " + audioInfo.toString());
        }
    };


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (mVideoPlayer == null) {
            Log.i(TAG, "surfaceChanged: width:" + width + "---height" + height);
            mVideoPlayer = new VideoPlayer(holder.getSurface(), width, height);
            mVideoPlayer.start();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}