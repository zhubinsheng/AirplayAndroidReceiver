package com.air.player.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriteHelper {
    private static final String AUDIO_PCM = ".pcm";

    private FileOutputStream mFileOutputStream;
    private File mAudioFile;

    public boolean createAudioFile(Context applicationContext, String fileName) {
        try {
            mAudioFile = createFile(applicationContext, AUDIO_PCM, fileName);
            mFileOutputStream = new FileOutputStream(mAudioFile);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TAG", "开始录音时，创建文件失败。", e);
            return false;
        }
        return true;
    }

    public void write2File(byte[] bytes) {
        try {
            mFileOutputStream.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release(){
        try {
            mFileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String obtainFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    private File createFile(Context applicationContext, String fileExtType, String fileName) throws IOException {
        File audioFile = new File(applicationContext.getCacheDir().getAbsolutePath()
                + "/" + obtainFileName() + fileName+ fileExtType);

        File parentPath = audioFile.getParentFile();
        if (!parentPath.exists()) {
            parentPath.mkdirs();
        }

        audioFile.createNewFile();

        return audioFile;
    }

    public static FileWriteHelper createNewHelper(){
        return new FileWriteHelper();
    }

}
