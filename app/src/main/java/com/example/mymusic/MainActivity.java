package com.example.mymusic;

import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SlideLineView.SlideLinePercentageListener {

    @BindView(R.id.play_audio_time)
    TextView playAudioTime;
    @BindView(R.id.play_audio_slide_line)
    SlideLineView playAudioSlideLine;
    @BindView(R.id.play_audio_total_time)
    TextView playAudioTotalTime;
    @BindView(R.id.play_audio_start_pause_iv)
    ImageView playAudioStartPauseIv;
    @BindView(R.id.tv_multiple)
    TextView tvMultiple;
    @BindView(R.id.speedOptions)
    Spinner speedOptions;

    private String mAudioUrl = "https://webfs.yun.kugou.com/202103291439/a66879750d742b9d4dec3f1d05c5e5af/KGTX/CLTX001/80f6399fcc08e08dc39ae44f5162ceca.mp3";
    //    private String mAudioUrl = "https://webfs.yun.kugou.com/202103291518/2d65040da1598a52dc3892bac0a98680/G205/M06/15/19/rZQEAF5ZQaiAXqNUACKax-U3C3w480.mp3";
    //音频播放的总时间。单位是毫秒
    private int mAudioTotalTime = 0;
    //秒时间。由音频的duration转换来的
    private int mTotalSecondTime = 0;

    private int mCurrentSecondTime;
    //音频是否正在播放
    private boolean mAudioIsPlaying = true;
    //音频是否结束了
    private boolean mAudioIsFinish = true;
    //是否滑动
    private boolean isCanClickPlay = true;

    private static final int mPlayAudioStepCode = 20201022;

    private Long mDelayTime = Long.valueOf(1000);

    private String[] getSpeedStrings() {
        return new String[]{"1.0", "1.2", "1.4", "1.6", "1.8", "2.0"};
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case mPlayAudioStepCode:
                    if (playAudioSlideLine != null && !playAudioSlideLine.getIsNowSlide()) {
                        playAudioSlideLine.setStepMove(true, true, 1);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        playAudioStartPauseIv.setImageResource(R.mipmap.ic_player_bofang);
//        mAudioIsPlaying = false;
        mAudioIsFinish = false;
//        isCanClickPlay = false;
        mAudioTotalTime = 0;
        setViewStatus();
        playAudioSlideLine.setSlideLinePercentageListener(this);
        playAudioSlideLine.postDelayed(new Runnable() {
            @Override
            public void run() {
                SoundPlayer.getInstance().onPlay(mAudioUrl);
                mAudioTotalTime = SoundPlayer.getInstance().getVoiceDuration();
//                mAudioIsPlaying = true;
//                isCanClickPlay = true;
                setViewStatus();
                durationToSecondTime();
                playAudioTotalTime.setText(handleTimeShow(mAudioTotalTime));
                playAudioTime.setText(handleTimeShow(0));
                //传进去的是 秒
                playAudioSlideLine.setMaxValue(mAudioTotalTime / 1000f);
                mHandler.sendEmptyMessageDelayed(mPlayAudioStepCode, mDelayTime);
                setSpeedOptions();
            }
        }, 1000);
    }

    /**
     * 将音频时长转换成秒
     */
    private void durationToSecondTime() {
        //转换成秒的时间
        int t = mAudioTotalTime / 1000;
        //转换成秒后，多余出来的时间
        int e = mAudioTotalTime % 1000;
        if (e != 0) {
            t++;
        }
        mTotalSecondTime = t;
    }

    private void startHandler() {
        cancelHandler();
        mHandler.sendEmptyMessageDelayed(mPlayAudioStepCode, mDelayTime);
    }

    private void cancelHandler() {
        mHandler.removeMessages(mPlayAudioStepCode);
    }


    @OnClick({R.id.play_audio_start_pause_iv, R.id.play_audio_hou_tui, R.id.play_audio_kuai_jin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //开始 暂停
            case R.id.play_audio_start_pause_iv:
                if (!isCanClickPlay) {
                    return;
                }
                if (mAudioIsPlaying) {
                    //音频正在播放，需要暂停
                    mHandler.removeMessages(mPlayAudioStepCode);
                    audioPause();
                } else {
                    //音频没有播放，需要开始播放
                    if (mAudioIsFinish) {
                        //音频已经播放完了
                        SoundPlayer.getInstance().onStop();
                        mCurrentSecondTime = 0;
                        playAudioTime.setText(handleTimeShow(0));
                        playAudioSlideLine.resetViewStatus();
                        SoundPlayer.getInstance().onPlay(mAudioUrl);
                    } else {
                        //音频没有播放完
                        audioStart();
                    }
                    startHandler();
                }
                mAudioIsPlaying = !mAudioIsPlaying;
                setViewStatus();
                break;
            //快退
            case R.id.play_audio_hou_tui:
                if (!isCanClickPlay) {
                    return;
                }
                handleHouTuiOrKuaiJin(false);
                break;
            //快进
            case R.id.play_audio_kuai_jin:
                if (!isCanClickPlay) {
                    return;
                }
                handleHouTuiOrKuaiJin(true);
                break;
        }
    }

    /**
     * 设置控件的状态
     */
    private void setViewStatus() {
        if (mAudioIsPlaying) {
            playAudioStartPauseIv.setImageResource(R.mipmap.ic_player_zant);
        } else {
            playAudioStartPauseIv.setImageResource(R.mipmap.ic_player_bofang);
        }
    }

    /**
     * 处理时间的展示
     */
    private String handleTimeShow(int timeValue) {
//        if (timeValue == 0) {
//            return "00:00";
//        }
//        //分
//        int m = timeValue / 1000 / 60;
//        //秒
//        int s = (timeValue - (m * 60 * 1000)) / 1000;
//        //分钟值转换成的字符串
//        String mS = "";
//        if (m >= 10) {
//            mS = "" + m;
//        } else {
//            mS = "0" + m;
//        }
//        //秒值转换成的字符串
//        String sS = "";
//        if (s >= 10) {
//            sS = "" + s;
//        } else {
//            sS = "0" + s;
//        }
//        return mS + ":" + sS;
        return DateUtils.formatSec(timeValue / 1000);
    }

    /**
     * 处理后退、快进
     */
    private void handleHouTuiOrKuaiJin(Boolean isKuaiJin) {
        if (mAudioIsFinish) {
            //音频已经播放完了，就不做处理了
            return;
        }
        if ((!isKuaiJin && mCurrentSecondTime == 0) || (isKuaiJin && mCurrentSecondTime == mTotalSecondTime)) {
            /**
             * 以下两种情况，都不做处理
             * 1、点击后退，并且现在的时间位置是0
             * 2、点击快进，并且现在的时间位置是最大值
             */
            return;
        }
        cancelHandler();
        if (!mAudioIsPlaying) {
            audioStart();
            mAudioIsPlaying = true;
            setViewStatus();
        }
        playAudioSlideLine.setStepMove(true, isKuaiJin, 5);
        playAudioTime.setText(handleTimeShow(mCurrentSecondTime));
    }

    /**
     * 音频播放结束
     */
    private void handleAudioFinish() {
        cancelHandler();
        SoundPlayer.getInstance().onStop();
        playAudioSlideLine.setCanSlide(false);
        mAudioIsPlaying = false;
        mAudioIsFinish = true;
        setViewStatus();
        mCurrentSecondTime = mTotalSecondTime;
        playAudioTime.setText(handleTimeShow(mTotalSecondTime));
    }

    /**
     * 手指滑动结束后，停留位置的百分比
     * <p>
     * Attempt to perform seekTo in wrong state: mPlayer=0x0, mCurrentState=1
     * 该方法可以只可以在【 Prepared, Paused, Started,PlaybackCompleted】 状态进行调用
     */
    @Override
    public void slidePercentageResult(float nowPercentage, float min, float max) {
        if (nowPercentage >= max) {
            //当前的比例，超过了最大值，表示结束了。停止刷新进度、关闭剩声音
            handleAudioFinish();
        } else {
            mAudioIsFinish = false;
            mAudioIsPlaying = true;
            playAudioSlideLine.setCanSlide(true);
            //还不到结束，取消之前的handler，重新延迟发送
            startHandler();
            int nowPlayTime = (int) (mAudioTotalTime * nowPercentage);
            mCurrentSecondTime = (int) (mTotalSecondTime * nowPercentage);
            playAudioTime.setText(handleTimeShow(mCurrentSecondTime));
            SoundPlayer.getInstance().setVoiceDuration(nowPlayTime);
        }
    }

    @Override
    public void moveResult(float nowPercentage, boolean isStart, boolean isEnd, boolean isAdd, int stepNum) {
        //只要不是结束，就允许触摸滑动
        playAudioSlideLine.setCanSlide(!isEnd);
        if (isEnd) {
            handleAudioFinish();
        } else {
            mAudioIsFinish = false;
            mAudioIsPlaying = true;
            if (isAdd) {
                mCurrentSecondTime = mCurrentSecondTime + stepNum;
            } else {
                mCurrentSecondTime = mCurrentSecondTime - stepNum;
            }
            if (isStart || mCurrentSecondTime <= 0) {
                mCurrentSecondTime = 0;
            } else if (isEnd || mCurrentSecondTime >= mTotalSecondTime) {
                mCurrentSecondTime = mTotalSecondTime;
            }
            playAudioTime.setText(handleTimeShow(mCurrentSecondTime * 1000));
            if (stepNum == 5 && nowPercentage != 1f && !isEnd) {
                SoundPlayer.getInstance().setVoiceDuration((int) (mAudioTotalTime * nowPercentage));
            }
            mHandler.sendEmptyMessageDelayed(mPlayAudioStepCode, mDelayTime);
        }
    }

    @Override
    public void audioStart() {
        SoundPlayer.getInstance().onStart();
    }

    @Override
    public void audioPause() {
        SoundPlayer.getInstance().onPause();
    }

    /**
     * 设置下拉框
     */
    private void setSpeedOptions() {
        String[] speeds = getSpeedStrings();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, speeds);
        speedOptions.setAdapter(arrayAdapter);
        speedOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                float speed = Float.parseFloat(speedOptions.getItemAtPosition(i).toString());
                String[] split = (speed + "").split(".");
                int count = 0;
                if (split.length > 1 && "0".equals(split[1])) {
                    int num = (int) speed;
                    count = num * 1000;
                } else {
                    count = Math.round(1000 / speed);
                }
                mDelayTime = Long.valueOf(count + "");
                Log.e("TAG", "-----" + count + "------" + mDelayTime);
                SoundPlayer.getInstance().changeplayerSpeed(speed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}
