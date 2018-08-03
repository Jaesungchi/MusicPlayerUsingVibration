package com.ensharp.global_1.musicplayerusingvibration;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class PlayerService extends Service {
    private IBinder mBinder = new LocalBinder();

    private MusicConverter mConverter;
    private ArrayList<MusicVO> mMusicList;
    private int currentMusicPosition;

    static final int PLAY_BUTTON = 0;
    static final int PAUSE_BUTTON = 1;
    static final int PREVIOUS_BUTTON = 2;
    static final int NEXT_BUTTON = 3;

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public PlayerService() {
    }

    @Override
    public void onCreate() {
        mConverter = new MusicConverter();
        mConverter.execute();
        mMusicList = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();

        // MusicList를 받아온다
        if(mMusicList == null)
            mMusicList = (ArrayList<MusicVO>) bundle.getSerializable("MusicList");

        // 리스트에서 누른 노래를 재생
        if(bundle.containsKey("position")) {
            int position = bundle.getInt("position");
            currentMusicPosition = position;
            mConverter.setMusicPath(mMusicList.get(position).getFilePath());
        }

        // MusicActivity에서 버튼을 눌렀다면
        if(bundle.containsKey("PlayerButton")) {
            int button = bundle.getInt("PlayerButton");
            switch (button) {
                case PLAY_BUTTON:
                    mConverter.play();
                    break;
                case PAUSE_BUTTON:
                    mConverter.pause();
                    break;
                case PREVIOUS_BUTTON:
                    currentMusicPosition -= 1;
                    if(currentMusicPosition < 0)
                        currentMusicPosition = mMusicList.size() - 1;
                    mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
                    break;
                case NEXT_BUTTON:
                    currentMusicPosition += 1;
                    if(currentMusicPosition >= mMusicList.size())
                        currentMusicPosition = 0;
                    mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
                    break;
            }
        }

        return START_REDELIVER_INTENT;
    }

    public int getCurrentMusicPosition() {
        return currentMusicPosition;
    }

    public ArrayList<MusicVO> getMusicList() {
        return mMusicList;
    }

    public boolean isCompletePlay() {
        return mConverter.isCompletePlay();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
