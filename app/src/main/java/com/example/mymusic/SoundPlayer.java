package com.example.mymusic;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;

/**
 * @Author: longyu
 * @CreateDate: 2021/3/29 9:30
 * @Description:
 */
public class SoundPlayer {

    private MediaPlayer mediaPlayer;

    private static SoundPlayer mSoundPlayer = null;

    private SoundPlayer() {
        mediaPlayer = new MediaPlayer();

    }

    public static SoundPlayer getInstance() {
        if (mSoundPlayer == null) {
            synchronized (SoundPlayer.class) {
                mSoundPlayer = new SoundPlayer();
            }
        }
        return mSoundPlayer;
    }

    /**
     * 播放
     */
    public void onPlay(String path) {
        try {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying() == true) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MyApplication.getInstance(), Uri.parse(path));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //不循环
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取进度
     * @return
     */
    public int getVoiceDuration() {
        int duration = mediaPlayer.getDuration();
        return duration;
    }

    /**
     * 设置进度
     * @param position
     */
    public void setVoiceDuration(int position){
        mediaPlayer.seekTo(position);
    }

    /**
     * 开始
     */
    public void onStart() {
        mediaPlayer.start();
    }

    /**
     * 暂停
     */
    public void onPause() {
        mediaPlayer.pause();
    }

    /**
     * 停止
     */
    public void onStop() {
        if (mediaPlayer == null || mediaPlayer.isPlaying() == true) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mSoundPlayer = null;
        }
    }

    /**
     * 改变倍速
     * @param speed
     */
    public void changeplayerSpeed(float speed) {
        if (mediaPlayer == null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams playbackParams = mediaPlayer.getPlaybackParams();
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPlaybackParams(playbackParams.setSpeed(speed));
            } else {
                mediaPlayer.setPlaybackParams(playbackParams.setSpeed(speed));
                mediaPlayer.pause();
            }
        }
    }

}
