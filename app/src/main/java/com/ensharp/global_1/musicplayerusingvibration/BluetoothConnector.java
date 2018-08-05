package com.ensharp.global_1.musicplayerusingvibration;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.ensharp.global_1.musicplayerusingvibration.DeviceListActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnector {
    // Debugging
    private static final String TAG = "BluetoothService";

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Handler mHandler;
    private Activity tempMainActivity;
    private Handler tempHandler;

    private int mState;
    // 상태를 나타내는 상태 변수
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3; // now connected to a remote device

    OutputStream mOutputStream = null; //출력을 위한 아웃풋 스트림.

    static boolean checkdouble = false;
    public boolean checkOnline = false;

    // Constructors
    public BluetoothConnector(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;
        tempHandler = h;
        tempMainActivity = ac;
        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setActivity(Activity musicActivity, Handler mHandler) {
        this.mActivity = musicActivity;
        this.mHandler = mHandler;
    }

    public void setMainActivity(){
        mActivity = tempMainActivity;
        mHandler = tempHandler;
    }

    public void enableBluetooth() {
        Log.d(TAG, "qqqqqq0");
        if(!checkdouble&&!checkOnline) {
            Log.d(TAG, "qqqqqq1");
            checkdouble = true;
            if (btAdapter.isEnabled()) {
                //블투가 켜져있는 상태.
                scanDevice();
            } else { //블루투스가 켜져있지 않은 상태.
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
            }
        }
    }

    public void scanDevice() {
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void getDeviceInfo(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        connect(device);
    }
    // Bluetooth 상태 set
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }
    // Bluetooth 상태 get
    public synchronized int getState() {
        return mState;
    }
    public synchronized void start() {
        Log.d(TAG, "start");
        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
    // ConnectThread 초기화 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null; }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);


    }
    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }
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
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    // 연결 실패했을때
    private void connectionFailed() {
        enableBluetooth();
        checkOnline = false;
        setState(STATE_LISTEN);
    }
    // 연결을 잃었을 때
    private void connectionLost() {
        enableBluetooth();
        checkOnline = false;
        setState(STATE_LISTEN);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // 디바이스 정보를 얻어서 BluetoothSocket 생성
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다
            btAdapter.cancelDiscovery();
            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다.
                mmSocket.connect();
                checkdouble = false;
                checkOnline = true;
                Log.d(TAG, "Connect Success");
            } catch (IOException e) {
                connectionFailed();
                // 연결 실패시 불러오는 메소드
                Log.d(TAG, "Connect Fail");
                // socket을 닫는다.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // 연결중? 혹은 연결 대기상태인 메소드를 호출한다.
                this.start();
                return;
            }
            // ConnectThread 클래스를 reset한다.
            synchronized (this) {
                mConnectThread = null;
            }
            // ConnectThread를 시작한다.
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try { mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
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
            } catch (IOException e) {
            }
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
    void sendData(String msg) {
        msg += "\n";  // 문자열 종료표시 (\n)
        try{
            mOutputStreamsou.write(msg.getBytes()); // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
        }catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
            if(!checkdouble&&!checkOnline)
                enableBluetooth();
        }
    }
}
