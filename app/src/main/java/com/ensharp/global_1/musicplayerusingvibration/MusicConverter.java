package com.ensharp.global_1.musicplayerusingvibration;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ensharp.global_1.musicplayerusingvibration.DSP.AudioEvent;
import com.ensharp.global_1.musicplayerusingvibration.DSP.BandPass;
import com.ensharp.global_1.musicplayerusingvibration.DSP.PercussionOnsetDetector;
import com.ensharp.global_1.musicplayerusingvibration.DSP.TarsosDSPAudioFloatConverter;
import com.ensharp.global_1.musicplayerusingvibration.DSP.TarsosDSPAudioFormat;
import com.ensharp.global_1.musicplayerusingvibration.DSP.WindowFunction;

import java.io.Serializable;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class MusicConverter extends AsyncTask<Void, double[], Void> implements Serializable {
    private SamplesLoader mLoader;
    private AudioEvent audioEvent;
    private WindowFunction window;
    private PercussionOnsetDetector percussionDetector;
    private BandPass bandPass;
    private AudioManager audioManager;

    // 한 프레임 당 sample 수
    private int blockSize = 0;
    private int frameCount = 0;
    // FFT 처리 객체
    private RealDoubleFFT transformer;
    // 재생 중인 프레임
    private int frame;
    // 현재 필터
    private int filter;
    // 미디어 볼륨
    private int volume;

    private boolean pausing;
    private boolean destorying = false;
    // 노래 완료 상태
    private boolean completePlay = false;
    private boolean converting = false;

    // AudioTrack 변수
    private int minSize;
    private AudioTrack audioTrack;

    // 변환 필터 상수
    static final int TOUGH = 0;
    static final int DELICACY = 1;

    private PlayerService pService = null;

    // 기준 주파수
    final int[] standardFrequencies = new int[]{63,125,250,500,1000,2000};
    final int SAMPLE_RATE = 44100;

    // 이퀄라이저 주파수 별 데시벨 값
    public int equalizer_63Hz;
    public int equalizer_125Hz;
    public int equalizer_250Hz;
    public int equalizer_500Hz;
    public int equalizer_1KHz;
    public int equalizer_2KHz;

    public MusicConverter(PlayerService mService) {
        super();
        frame = 0;
        mLoader = new SamplesLoader();
        window = new WindowFunction();
        pausing = true;

        minSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
        audioEvent = new AudioEvent(new TarsosDSPAudioFormat(TarsosDSPAudioFloatConverter.PCM_FLOAT, SAMPLE_RATE, 256, AudioFormat.CHANNEL_IN_STEREO, blockSize, 40, true));
        bandPass = new BandPass(SAMPLE_RATE);

        pService = mService;
    }

    public void pause() {
        pausing = true;
        PlayerService.PLAY_STATE = false;
    }

    public void play() {
        pausing = false;
        PlayerService.PLAY_STATE = true;
    }

    public void setVolume(int volume) {
        this.volume = volume;
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
        converting = true;

        Log.i("conv", "변환 중...");
        mLoader.Open(path);
        frameCount = mLoader.musicbuffers.length;
        blockSize = mLoader.musicbuffers[0].length;
        //transformer = new RealDoubleFFT(blockSize);
        percussionDetector = new PercussionOnsetDetector(SAMPLE_RATE, blockSize);
        Log.i("conv", "변환 완료");
        pausing = false;
        converting = false;
        PlayerService.PLAY_STATE = true;
    }

    public boolean isConverting() {
        return converting;
    }

    public double getCurrentProgressRate() {
        return (double)frame / frameCount;
    }

    public float[] convertToFloatArray(double[] array) {
        float[] result = new float[array.length];
        for(int i=0; i<array.length; i++)
            result[i] = (float)array[i];
        return result;
    }

    private float[] normalizationToFloat(short[] buffer) {
        float[] result = new float[buffer.length];
        for (int i = 0; i < buffer.length; i++)
            result[i] = (float) buffer[i] / Short.MAX_VALUE;
        return result;
    }

    private float[] normalizationToFloat(short[] buffer, float[] weights) {
        float[] result = new float[buffer.length];
        for(int i = 0; i < buffer.length; i++)
            result[i] = (buffer[i] / Short.MAX_VALUE) * weights[i];
        return result;
    }

    private float[] filter(short[] buffer) {
        float[] result = new float[buffer.length];

        // HammingWindow 전처리 적용
        if(filter == TOUGH)
            result = normalizationToFloat(buffer, window.generateCurve(WindowFunction.KAISER, buffer.length));

        // Band-Pass Filter, Kaiser window 전처리 적용
        else if(filter == DELICACY) {
            float[] buf = new float[buffer.length];

            bandPass.setF1(100);
            bandPass.setF2(20000);
            bandPass.setBW(1);
            buf = bandPass.filter(buf, normalizationToFloat(buffer), blockSize, 0);
            audioEvent.setFloatBuffer(buf);
            for(int i=0; i < blockSize; i++)
                result[i] = buf[i] * (float) audioEvent.getRMS();
        }
        return result;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        short[] buffer;
        float[] processed;

        while(true) {
            // 정지 상태
            while(pausing);

            volume = pService.getCurrentVolume();

            if(mLoader.musicbuffers == null || frame >= mLoader.musicbuffers.length)
                continue;

            // 현재 frame의 sample 값들을 저장
            buffer = mLoader.musicbuffers[frame++];

            // FFT를 처리하기 전에 0~1 사이 값으로 정규화하고 필터를 적용한다.
            audioEvent.setFloatBuffer(filter(buffer));
            processed = percussionDetector.process(audioEvent);

            String signal = makeSignal(processed);
            pService.sendData(signal);

            //Log.e("conv", signal);

            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

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
        frame = (int)(frameCount * playRate);
    }

    public double[] copyArray(double[] array) {
        double[] copied = new double[array.length];
        for(int k=0; k<array.length; k++)
            copied[k] = array[k];
        return copied;
    }

    // 하드웨어로 보낼 신호 문자열을 반환한다.
    public String makeSignal(float[] frequencies) {
        String signal = "";
        String temp;

        for (int part = 0; part < 6; part++) {
            int gap = standardFrequencies[part] / 10;
            double max = 0.0;

            for(int freq = 9 * gap; freq < 11 * gap; freq++) {
                if(frequencies.length <= freq)
                    break;
                if(max < frequencies[freq])
                    max = frequencies[freq];
            }

            max *= volume;

            // 이퀄라이저값이 기본값이 아니고 각 이퀄라이저 조정 범위와 맞는 part의 신호면
            if (equalizer_63Hz != 0 && part == 0) {
                max += equalizer_63Hz * 5;
            }
            if (equalizer_125Hz != 0 && part == 1) {
                max += equalizer_125Hz * 5;
            }
            if (equalizer_250Hz != 0 && part == 2) {
                max += equalizer_250Hz * 5;
            }
            if (equalizer_500Hz != 0 && part == 3) {
                max += equalizer_500Hz * 5;
            }
            if (equalizer_1KHz != 0 && part == 4) {
                max += equalizer_1KHz * 5;
            }
            if (equalizer_2KHz != 0 && part == 5) {
                max += equalizer_2KHz * 5;
            }

            temp = (int)max + "";

            if (temp.length() == 1)
                temp = '0' + temp;
            if (temp.length() >= 3)
                temp = "99";

            // signal이 0 미만이거나 99를 초과하면 최대,최소값으로 조정해줌
            if(Integer.parseInt(temp) < 0) {
                temp = "0";
            }
            if(Integer.parseInt(temp) > 99) {
                temp = "99";
            }

            signal += temp;
        }

        return signal;
    }

    public void destroy() {
        destorying = true;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }
}
