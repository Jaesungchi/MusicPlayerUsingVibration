//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ensharp.global_1.musicplayerusingvibration.BandPassFilter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TarsosDSPAudioFormat {
    protected Encoding encoding;
    protected float sampleRate;
    protected int sampleSizeInBits;
    protected int channels;
    protected int frameSize;
    protected float frameRate;
    protected boolean bigEndian;
    private HashMap<String, Object> properties;
    public static final int NOT_SPECIFIED = -1;

    public TarsosDSPAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
        this.properties = null;
    }

    public TarsosDSPAudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
        this(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
        this.properties = new HashMap(properties);
    }

    public TarsosDSPAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
        this(signed ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED, sampleRate, sampleSizeInBits, channels, channels != -1 && sampleSizeInBits != -1 ? (sampleSizeInBits + 7) / 8 * channels : -1, sampleRate, bigEndian);
    }

    public Encoding getEncoding() {
        return this.encoding;
    }

    public float getSampleRate() {
        return this.sampleRate;
    }

    public int getSampleSizeInBits() {
        return this.sampleSizeInBits;
    }

    public int getChannels() {
        return this.channels;
    }

    public int getFrameSize() {
        return this.frameSize;
    }

    public float getFrameRate() {
        return this.frameRate;
    }

    public boolean isBigEndian() {
        return this.bigEndian;
    }

    public Map<String, Object> properties() {
        Object ret;
        if (this.properties == null) {
            ret = new HashMap(0);
        } else {
            ret = (Map)this.properties.clone();
        }

        return Collections.unmodifiableMap((Map)ret);
    }

    public Object getProperty(String key) {
        return this.properties == null ? null : this.properties.get(key);
    }

    public boolean matches(TarsosDSPAudioFormat format) {
        return format.getEncoding().equals(this.getEncoding()) && (format.getSampleRate() == -1.0F || format.getSampleRate() == this.getSampleRate()) && format.getSampleSizeInBits() == this.getSampleSizeInBits() && format.getChannels() == this.getChannels() && format.getFrameSize() == this.getFrameSize() && (format.getFrameRate() == -1.0F || format.getFrameRate() == this.getFrameRate()) && (format.getSampleSizeInBits() <= 8 || format.isBigEndian() == this.isBigEndian());
    }

    public String toString() {
        String sEncoding = "";
        if (this.getEncoding() != null) {
            sEncoding = this.getEncoding().toString() + " ";
        }

        String sSampleRate;
        if (this.getSampleRate() == -1.0F) {
            sSampleRate = "unknown sample rate, ";
        } else {
            sSampleRate = this.getSampleRate() + " Hz, ";
        }

        String sSampleSizeInBits;
        if ((float)this.getSampleSizeInBits() == -1.0F) {
            sSampleSizeInBits = "unknown bits per sample, ";
        } else {
            sSampleSizeInBits = this.getSampleSizeInBits() + " bit, ";
        }

        String sChannels;
        if (this.getChannels() == 1) {
            sChannels = "mono, ";
        } else if (this.getChannels() == 2) {
            sChannels = "stereo, ";
        } else if (this.getChannels() == -1) {
            sChannels = " unknown number of channels, ";
        } else {
            sChannels = this.getChannels() + " channels, ";
        }

        String sFrameSize;
        if ((float)this.getFrameSize() == -1.0F) {
            sFrameSize = "unknown frame size, ";
        } else {
            sFrameSize = this.getFrameSize() + " bytes/frame, ";
        }

        String sFrameRate = "";
        if ((double)Math.abs(this.getSampleRate() - this.getFrameRate()) > 1.0E-5D) {
            if (this.getFrameRate() == -1.0F) {
                sFrameRate = "unknown frame rate, ";
            } else {
                sFrameRate = this.getFrameRate() + " frames/second, ";
            }
        }

        String sEndian = "";
        if ((this.getEncoding().equals(Encoding.PCM_SIGNED) || this.getEncoding().equals(Encoding.PCM_UNSIGNED)) && (this.getSampleSizeInBits() > 8 || this.getSampleSizeInBits() == -1)) {
            if (this.isBigEndian()) {
                sEndian = "big-endian";
            } else {
                sEndian = "little-endian";
            }
        }

        return sEncoding + sSampleRate + sSampleSizeInBits + sChannels + sFrameSize + sFrameRate + sEndian;
    }

    public static class Encoding {
        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
        public static final Encoding ULAW = new Encoding("ULAW");
        public static final Encoding ALAW = new Encoding("ALAW");
        private String name;

        public Encoding(String name) {
            this.name = name;
        }

        public final boolean equals(Object obj) {
            if (this.toString() == null) {
                return obj != null && obj.toString() == null;
            } else {
                return obj instanceof Encoding ? this.toString().equals(obj.toString()) : false;
            }
        }

        public final int hashCode() {
            return this.toString() == null ? 0 : this.toString().hashCode();
        }

        public final String toString() {
            return this.name;
        }
    }
}
