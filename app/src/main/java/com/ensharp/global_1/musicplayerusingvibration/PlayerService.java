package com.ensharp.global_1.musicplayerusingvibration;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class PlayerService extends Service {
    private IBinder mBinder = new LocalBinder();

    private MusicConverter mConverter;
    private ArrayList<MusicVO> mMusicList;
    private int currentMusicPosition;
    private NotificationPlayer mNotificationPlayer;

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
        super.onCreate();

        mConverter = new MusicConverter();
        mConverter.execute();
        mMusicList = null;
        mNotificationPlayer = new NotificationPlayer(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("service", "startService");
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();

        // MusicList를 받아온다
        if(mMusicList == null)
            mMusicList = (ArrayList<MusicVO>) bundle.getSerializable("MusicList");

        // 리스트에서 누른 노래를 재생
        if(bundle.containsKey("position")) {
            Log.e("service", "position");
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
                    setPreviousMusic();
                    break;
                case NEXT_BUTTON:
                    setNextMusic();
                    break;
            }
            updateNotificationPlayer();
        }

        // notification bar controller에서 버튼을 눌렀을 때
        if(action == null) {
            if(CommandActions.TOGGLE_PLAY.equals(action)) {
                if(isPlaying())
                    mConverter.pause();
                else
                    mConverter.play();
            }
            else if(CommandActions.REWIND.equals(action))
                setPreviousMusic();
            else if(CommandActions.FORWARD.equals(action))
                setNextMusic();
            else if(CommandActions.CLOSE.equals(action)) {
                mConverter.destroy();
                removeNotificationPlayer();
            }
        }

        return START_REDELIVER_INTENT;
    }

    public void setPreviousMusic() {
        currentMusicPosition -= 1;
        if(currentMusicPosition < 0)
            currentMusicPosition = mMusicList.size() - 1;
        mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
    }

    public void setNextMusic() {
        currentMusicPosition += 1;
        if(currentMusicPosition >= mMusicList.size())
            currentMusicPosition = 0;
        mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
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

    public boolean isPlaying() {
        return mConverter.isPlaying();
    }

    public MusicVO getCurrentMusicVO() {
        return mMusicList.get(currentMusicPosition);
    }

    private void updateNotificationPlayer() {
        if(mNotificationPlayer != null)
            mNotificationPlayer.updateNotificationPlayer();
    }

    private void removeNotificationPlayer() {
        if(mNotificationPlayer != null)
            mNotificationPlayer.removeNotificationPlayer();
    }
}
