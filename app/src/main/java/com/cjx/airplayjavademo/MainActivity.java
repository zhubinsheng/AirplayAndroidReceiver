package com.cjx.airplayjavademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cjx.airplayjavademo.model.NALPacket;
import com.cjx.airplayjavademo.model.PCMPacket;
import com.cjx.airplayjavademo.player.AudioPlayer;
import com.cjx.airplayjavademo.player.VideoPlayer;
import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import com.github.serezhka.airplay.server.AirPlayConfig;
import com.github.serezhka.airplay.server.AirPlayServer;
import com.github.serezhka.airplay.server.AirPlayConsumer;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private AirPlayServer airPlayServer;
    private static String TAG = "airplay";
    private VideoPlayer mVideoPlayer;
    private AudioPlayer mAudioPlayer;
    private final LinkedList<NALPacket> mVideoCacheList = new LinkedList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.start();

        AirPlayConfig airPlayConfig = new AirPlayConfig();
        airPlayConfig.setServerName("hero");
        airPlayConfig.setWidth(1080);
        airPlayConfig.setHeight(1920);
        airPlayConfig.setFps(30);

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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            PCMPacket pcmPacket=new PCMPacket();
            pcmPacket.data=audio;
            if (mAudioPlayer != null) {
                mAudioPlayer.addPacker(pcmPacket);
            }

        }

        @Override
        public void onAudioSrcDisconnect() {

        }

        @Override
        public void onMediaPlaylist(String playlistUri) {
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