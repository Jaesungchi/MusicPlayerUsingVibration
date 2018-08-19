package com.ensharp.global_1.musicplayerusingvibration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

public class EqualizerActivity extends AppCompatActivity {
    private SeekBar seekBar_63Hz;
    private SeekBar seekBar_125Hz;
    private SeekBar seekBar_250Hz;
    private SeekBar seekBar_500Hz;
    private SeekBar seekBar_1KHz;
    private SeekBar seekBar_2KHz;

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        Log.e("equalizer start","");

        serviceIntent = new Intent(this, MusicActivity.class);

        // 데시벨 조정 seekBar 생성
        seekBar_63Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_63Hz);
        seekBar_125Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_125Hz);
        seekBar_250Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_250Hz);
        seekBar_500Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_500Hz);
        seekBar_1KHz = (SeekBar) findViewById(R.id.equalizer_seekbar_1KHz);
        seekBar_2KHz = (SeekBar) findViewById(R.id.equalizer_seekbar_2KHz);

        // 처음엔 다 0으로 초기화
        seekBar_63Hz.setProgress(0);
        seekBar_125Hz.setProgress(0);
        seekBar_250Hz.setProgress(0);
        seekBar_500Hz.setProgress(0);
        seekBar_1KHz.setProgress(0);
        seekBar_2KHz.setProgress(0);

        // 리스너
        seekBar_63Hz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_63Hz",seekBar_63Hz.getProgress());
                startService(serviceIntent);
            }
        });
        seekBar_125Hz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_125Hz",seekBar_125Hz.getProgress());
                startService(serviceIntent);
            }
        });
        seekBar_250Hz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_250Hz",seekBar_250Hz.getProgress());
                startService(serviceIntent);
            }
        });
        seekBar_500Hz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_500Hz",seekBar_500Hz.getProgress());
                startService(serviceIntent);
            }
        });
        seekBar_1KHz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_1KHz",seekBar_1KHz.getProgress());
                startService(serviceIntent);
            }
        });
        seekBar_2KHz.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer_2KHz",seekBar_2KHz.getProgress());
                startService(serviceIntent);
            }
        });
    }
}
