/* Copyright 2017 Eddy Xiao <bewantbe@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ensharp.global_1.melody_extraction;

import android.content.res.Resources;
import android.media.MediaRecorder;
import android.util.Log;

import com.ensharp.global_1.musicplayerusingvibration.R;

/**
 * Basic properties of Analyzer.
 */

public class AnalyzerParameters {
    int sampleRate = 44100;
    int fftLen = 8192;
    int hopLen = 4096;
    double overlapPercent = 50;  // = (1 - hopLen/fftLen) * 100%
    String wndFuncName = "Blackman";
    public int nFFTAverage = 2;
    public boolean isAWeighting = false;
    final int BYTE_OF_SAMPLE = 2;
    final double SAMPLE_VALUE_MAX = 32767.0;   // Maximum signal value
    double spectrogramDuration = 4.0;

    public double[] micGainDB;  // should have fftLen/2+1 elements, i.e. include DC.
    String calibName = null;
}
