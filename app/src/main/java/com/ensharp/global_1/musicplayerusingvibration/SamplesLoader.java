package com.ensharp.global_1.musicplayerusingvibration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

// 음악의 전체 sample 값들을 불러와 저장하는 클래스
class SamplesLoader {
    static class Sample {
        private short[] buffer;
        private int size;

        public Sample(short[] buf, int s) {
            buffer = buf.clone();
            size = s;
        }

        public short[] GetBuffer() {
            return buffer;
        }

        public int GetSize() {
            return size;
        }
    }

    public static final int BUFFER_SIZE = 44100000;

    private Decoder decoder;
    private ArrayList<Sample> samples;
    public short[][] musicbuffers;
    private int size;

    public SamplesLoader() {

    }

    public SamplesLoader(String path) {
        Open(path);
    }

    public boolean IsInvalid() {
        //return (decoder == null || out == null || samples == null || !out.isOpen());
        return (decoder == null || samples == null);
    }

    // 음악으로부터 sample 값들을 얻는다.
    protected boolean GetSamples(String path) {
        if(IsInvalid())
            return false;
        try {
            Header header;
            SampleBuffer sb;
            FileInputStream in = new FileInputStream(path);
            Bitstream bitstream = new Bitstream(in);
            if((header = bitstream.readFrame()) == null)
                return false;
            while(size < BUFFER_SIZE && header != null) {
                sb = (SampleBuffer)decoder.decodeFrame(header, bitstream);
                samples.add(new Sample(sb.getBuffer(), sb.getBufferLength()));
                size++;
                bitstream.closeFrame();
                header = bitstream.readFrame();
            }
        } catch(FileNotFoundException e) {
            return false;
        } catch(BitstreamException e) {
            return false;
        } catch(DecoderException e) {
            return false;
        }
        return true;
    }

    public boolean Open(String path) {
        // 열고 있는 음악이 있으면 닫는다.
        Close();

        decoder = new Decoder();
        samples = new ArrayList<Sample>(BUFFER_SIZE);
        size = 0;
        GetSamples(path);

        // 음악의 전체 sample 값들을 얻는다.
        musicbuffers = GetMusicBuffers();

        return true;
    }

    public short[][] GetMusicBuffers() {
        short[][] musicBuffer = new short[size][];
        for(int i=0; i < size; i++) {
            short[] buffers = samples.get(i).GetBuffer();
            musicBuffer[i] = buffers;
        }
        return musicBuffer;
    }

    public void Close() {
        size = 0;
        samples = null;
        decoder = null;
    }
}
