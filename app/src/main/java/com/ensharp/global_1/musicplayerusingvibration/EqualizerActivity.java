package com.ensharp.global_1.musicplayerusingvibration;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);

        serviceIntent = new Intent(this, PlayerService.class);
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        editor = preferences.edit();

        // 데시벨 조정 seekBar 생성
        seekBar_63Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_63Hz);
        seekBar_125Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_125Hz);
        seekBar_250Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_250Hz);
        seekBar_500Hz = (SeekBar) findViewById(R.id.equalizer_seekbar_500Hz);
        seekBar_1KHz = (SeekBar) findViewById(R.id.equalizer_seekbar_1KHz);
        seekBar_2KHz = (SeekBar) findViewById(R.id.equalizer_seekbar_2KHz);

        // SharedPreferences에 저장된 값으로 초기화
        seekBar_63Hz.setProgress(getPreferencesData("equalizer_63Hz"));
        seekBar_125Hz.setProgress(getPreferencesData("equalizer_125Hz"));
        seekBar_250Hz.setProgress(getPreferencesData("equalizer_250Hz"));
        seekBar_500Hz.setProgress(getPreferencesData("equalizer_500Hz"));
        seekBar_1KHz.setProgress(getPreferencesData("equalizer_1KHz"));
        seekBar_2KHz.setProgress(getPreferencesData("equalizer_2KHz"));

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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_63Hz");
                setPreferencesData("equalizer_63Hz",seekBar_63Hz.getProgress());

                // seekBar.getProgress() 값을 얻어서 데시벨 조정
                serviceIntent.putExtra("equalizer", "set");
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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_125Hz");
                setPreferencesData("equalizer_125Hz",seekBar_125Hz.getProgress());

                serviceIntent.putExtra("equalizer", "set");
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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_250Hz");
                setPreferencesData("equalizer_250Hz",seekBar_250Hz.getProgress());

                serviceIntent.putExtra("equalizer", "set");
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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_500Hz");
                setPreferencesData("equalizer_500Hz",seekBar_500Hz.getProgress());

                serviceIntent.putExtra("equalizer", "set");
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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_1KHz");
                setPreferencesData("equalizer_1KHz",seekBar_1KHz.getProgress());

                serviceIntent.putExtra("equalizer", "set");
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
                // SharedPreferences에 이퀄라이저 데이터 저장
                deletePreferencesData("equalizer_2KHz");
                setPreferencesData("equalizer_2KHz",seekBar_2KHz.getProgress());

                serviceIntent.putExtra("equalizer", "set");
                startService(serviceIntent);
            }
        });
    }

    // SharedPreferences Data 저장하기
    private void  setPreferencesData(String key, int data){
        editor.putInt(key, data);
        editor.commit();
    }

    // SharedPreferences Data 삭제하기
    private void deletePreferencesData(String key){
        editor.remove(key);
        editor.commit();
    }

    // SharedPreferences Data 불러오기
    public int getPreferencesData(String key){
        return preferences.getInt(key, 0);
    }

}
