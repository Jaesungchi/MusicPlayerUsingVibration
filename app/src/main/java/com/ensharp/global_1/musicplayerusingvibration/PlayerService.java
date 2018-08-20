package com.ensharp.global_1.musicplayerusingvibration;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerService extends Service {
    private IBinder mBinder = new LocalBinder();

    private MusicConverter mConverter;
    private ArrayList<MusicVO> mMusicList;
    static public int currentMusicPosition;
    private NotificationPlayer mNotificationPlayer;
    private AudioManager audioManager;
    private MusicActivity currentMusicActivity = null;

    static final int PLAY_BUTTON = 0;
    static final int PAUSE_BUTTON = 1;
    static final int PREVIOUS_BUTTON = 2;
    static final int NEXT_BUTTON = 3;

    private boolean bluetoothConnected = false;
    private static final String TAG = "BluetoothService";

    private ConnectedThread mConnectedThread;

    private int mState;

    // 상태를 나타내는 상태 변수
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3; // now connected to a remote device

    // SharedPreferences 파일 변수
    private SharedPreferences preferences;

    public static boolean PLAY_STATE = false;

    public OutputStream mOutputStream;

    public void changeFilter(int filter) {
        mConverter.setFilter(filter);
    }

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

        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        mConverter = new MusicConverter(this);
        mConverter.setVolume(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        setEqualizer();
        mConverter.execute();
        mMusicList = null;
        mNotificationPlayer = new NotificationPlayer(this);
        mConverter.setFilter(MusicConverter.DELICACY);
    }

    public int getCurrentVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public void changeMusicActivity(MusicActivity musicActivity) {
        if(currentMusicActivity != null)
            currentMusicActivity.finish();
        currentMusicActivity = musicActivity;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();

        // MusicList를 받아온다
        if (mMusicList == null)
            mMusicList = (ArrayList<MusicVO>) bundle.getSerializable("MusicList");

        // 블루투스 연결이 안 됐으면
        if (!bluetoothConnected) {
            if (MainActivity.btConnector.getmSocket() != null) {
                connected(MainActivity.btConnector.mmSocket, MainActivity.btConnector.mmDevice);
                bluetoothConnected = true;
            }
        }

        // notification bar controller 에서 버튼을 눌렀을 때
        if (action != null) {
            if (CommandActions.TOGGLE_PLAY.equals(action)) {
                if (isPlaying()) {
                    mConverter.pause();
                    if(currentMusicActivity != null)
                        currentMusicActivity.syncWithNotification(PAUSE_BUTTON);
                }
                else {
                    mConverter.play();
                    if(currentMusicActivity != null)
                        currentMusicActivity.syncWithNotification(PLAY_BUTTON);
                }
            }
            else if (CommandActions.REWIND.equals(action)) {
                if(currentMusicActivity != null)
                    currentMusicActivity.syncWithNotification(PREVIOUS_BUTTON);
                setPreviousMusic();
            }
            else if (CommandActions.FORWARD.equals(action)) {
                if(currentMusicActivity != null)
                    currentMusicActivity.syncWithNotification(NEXT_BUTTON);
                setNextMusic();
            }

            else if (CommandActions.CLOSE.equals(action)) {
                mConverter.destroy();
                removeNotificationPlayer();
                return START_NOT_STICKY;
            }

            updateCurrentMusicFile();
            updateNotificationPlayer();

            return START_REDELIVER_INTENT;
        }

        // MainActivity, MusicActivity에서 버튼을 눌렀다면
        if (bundle.containsKey("PlayerButton")) {
            int button = bundle.getInt("PlayerButton");

            switch (button) {
                case PLAY_BUTTON:
                    mConverter.play();
                    PLAY_STATE = true;
                    break;
                case PAUSE_BUTTON:
                    mConverter.pause();
                    PLAY_STATE = false;
                    break;
                case PREVIOUS_BUTTON:
                    setPreviousMusic();
                    updateCurrentMusicFile();
                    break;
                case NEXT_BUTTON:
                    setNextMusic();
                    updateCurrentMusicFile();
                    break;
            }
            updateNotificationPlayer();
            return START_REDELIVER_INTENT;
        }

        // 리스트에서 누른 노래를 재생
        if (bundle.containsKey("position")) {
            Log.e("service", "position");
            int position = bundle.getInt("position");
            currentMusicPosition = position;
            PLAY_STATE = true;
            mConverter.setMusicPath(mMusicList.get(position).getFilePath());
            updateNotificationPlayer();
            updateCurrentMusicFile();
        }

        // Equalizer 설정
        if(bundle.containsKey("equalizer"))
            setEqualizer();

        return START_REDELIVER_INTENT;
    }

    private void setEqualizer(){
        mConverter.equalizer_63Hz = getPreferencesData("equalizer_63Hz");
        mConverter.equalizer_125Hz = getPreferencesData("equalizer_125Hz");
        mConverter.equalizer_250Hz = getPreferencesData("equalizer_250Hz");
        mConverter.equalizer_500Hz = getPreferencesData("equalizer_500Hz");
        mConverter.equalizer_1KHz = getPreferencesData("equalizer_1KHz");
        mConverter.equalizer_2KHz = getPreferencesData("equalizer_2KHz");
    }

    // SharedPreferences Data 불러오기
    public int getPreferencesData(String key){
        return preferences.getInt(key, 0);
    }

    public void updateCurrentMusicFile() {
        // 현재 음악파일 VO
        MusicVO currentMusicVO = mMusicList.get(currentMusicPosition);
        SharedPreferences.Editor editor = preferences.edit();

        // preferences 값 모두 삭제하기
        editor.clear();
        editor.commit();

        // preferences에 현재 재생 음악 정보 저장하기
        editor.putString("currentMusicTitle", currentMusicVO.getTitle());
        editor.putString("currentMusicSinger", currentMusicVO.getArtist());
        editor.putString("currentMusicAlbum", currentMusicVO.getAlbumId());
        editor.commit();
    }

    public void setPreviousMusic() {
        currentMusicPosition -= 1;
        if (currentMusicPosition < 0)
            currentMusicPosition = mMusicList.size() - 1;
        mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
        updateNotificationPlayer();
    }

    public void setNextMusic() {
        currentMusicPosition += 1;
        if (currentMusicPosition >= mMusicList.size())
            currentMusicPosition = 0;
        mConverter.setMusicPath(mMusicList.get(currentMusicPosition).getFilePath());
        updateNotificationPlayer();
    }

    public int getCurrentMusicPosition() {
        return currentMusicPosition;
    }

    public ArrayList<MusicVO> getMusicList() {
        return mMusicList;
    }

    public boolean isConverting() {
        return mConverter.isConverting();
    }

    public boolean isCompletePlay() {
        return mConverter.isCompletePlay();
    }

    public boolean isPlaying() {
        return mConverter.isPlaying();
    }

    // 음악을 변경하기 전 호출되는 함수이며, 그 뒤 서비스에서 Position이 변경된다.
    public MusicVO getCurrentMusicVO(int pressedButton) {
        if(pressedButton == PlayerService.NEXT_BUTTON) {
            if (mMusicList.size() <= currentMusicPosition + 1)
                return mMusicList.get(0);
            return mMusicList.get(currentMusicPosition + 1);
        }
        else if(pressedButton == PlayerService.PREVIOUS_BUTTON) {
            if (currentMusicPosition - 1 < 0)
                return mMusicList.get(mMusicList.size() - 1);
            return mMusicList.get(currentMusicPosition - 1);
        }
        else
            return mMusicList.get(currentMusicPosition);
    }

    public int getCurrentProgress() {
        return (int) (mConverter.getCurrentProgressRate() * 200);
    }

    public void setFrame(int progress) {
        mConverter.setFrame(progress / 200.0);
    }

    private void updateNotificationPlayer() {
        if(mNotificationPlayer != null)
            mNotificationPlayer.updateNotificationPlayer();
    }

    private void removeNotificationPlayer() {
        if(mNotificationPlayer != null)
            mNotificationPlayer.removeNotificationPlayer();
    }

    // Bluetooth 상태 set
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }
    // 모든 thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }
    // 연결을 잃었을 때
    private void connectionLost() {
        MainActivity.btConnector.enableBluetooth();
        MainActivity.btConnector.checkOnline = false;
        bluetoothConnected = false;
        stop();
        setState(STATE_LISTEN);
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mOutputStream = tmpOut; // 1. 데이터를 보내기 위한 OutputStrem
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {//연결이 끊어진 경우
                    connectionLost();
                    break;
                }
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    // 문자열 전송하는 함수
    public void sendData(String msg) {
        msg += "\n";  // 문자열 종료표시 (\n)
        try{
            mOutputStream.write(msg.getBytes()); // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
        }catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
           connectionLost();
        }
    }
}
