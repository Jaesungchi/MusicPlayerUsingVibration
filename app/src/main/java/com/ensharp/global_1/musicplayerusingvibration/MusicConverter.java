package com.ensharp.global_1.musicplayerusingvibration;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.Serializable;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class MusicConverter extends AsyncTask<Void, double[], Void> implements Serializable {
    private SamplesLoader mLoader;

    // 한 프레임 당 sample 수
    private int blockSize = 0;
    // FFT 처리 객체
    private RealDoubleFFT transformer;
    // 재생 중인 프레임
    private int frame;

    private double[] normalized;
    private boolean pausing;
    private boolean destorying = false;
    // 노래 완료 상태
    private boolean completePlay = false;


    // 기준 주파수
    final int[] standardFrequencies = new int[]{63,125,250,500,1000,2000};

    public MusicConverter() {
        super();
        frame = 0;
        mLoader = new SamplesLoader();
        pausing = true;
    }

    public void pause() {
        pausing = true;
    }

    public void play() {
        pausing = false;
    }

    public boolean isPlaying() {
        return !pausing;
    }

    public boolean isCompletePlay() {
        return completePlay;
    }

    // 음악 경로를 설정한다.
    public void setMusicPath(String path) {
        pausing = true;
        completePlay = false;
        frame = 0;

        Log.e("conv", "변환 중...");
        mLoader.Open(path);
        blockSize = mLoader.musicbuffers[0].length;
        transformer = new RealDoubleFFT(blockSize);
        normalized = new double[blockSize];
        Log.e("conv", "변환 완료");

        pausing = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        short[] buffer;
        double[] toTransform;

        while(true) {
            // 정지 상태
            while(pausing);

            // 현재 frame의 sample 값들을 저장
            buffer = mLoader.musicbuffers[frame++];

            // FFT를 처리하기 전에 0~1 사이 값으로 정규화
            toTransform = normalization(buffer);
            transformer.ft(toTransform);

            MainActivity.btService.sendData(makeSignal(toTransform));

            Log.e("conv", makeSignal(toTransform));

            // 음악과 싱크를 맞추기 위한 sleep
            try {
                Thread.sleep(21);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 노래가 다 끝났으면
            if(frame >= mLoader.musicbuffers.length) {
                pausing = true;
                completePlay = true;
            }


            // 프로그램 종료 시
            if(destorying)
                break;
        }

        mLoader.Close();
        return null;
    }

    // 현재 진행하고 있는 프레임을 이동한다.
    public void setFrame(double playRate) {
        frame = (int)(blockSize * playRate);
    }

    public double[] normalization(short[] buffer) {
        for (int i = 0; i < blockSize; i++) {
            normalized[i] = (double) buffer[i] / Short.MAX_VALUE; // 부호 있는 16비트
        }
        return normalized;
    }

    public double[] copyArray(double[] array) {
        double[] copied = new double[array.length];
        for(int k=0; k<array.length; k++)
            copied[k] = array[k];
        return copied;
    }

    // 하드웨어로 보낼 신호 문자열을 반환한다.
    public String makeSignal(double[] frequencies) {
        String signal = "";
        String temp = "";

        for(int part = 0; part < 6; part++) {
            int gap = standardFrequencies[part] / 10;
            double max = 0.0;
            for(int freq = 9 * gap; freq < 11 * gap; freq++) {
                if(max < frequencies[freq])
                    max = frequencies[freq];
            }

            temp = (int)max + "";
            if(temp.length() == 1)
                temp = '0' + temp;
            if(temp.length() >= 3)
                temp = "99";

            signal += temp;
        }
        return signal;
    }

    public void destroy() {
        destorying = true;
    }

}
