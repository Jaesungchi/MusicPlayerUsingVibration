//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ensharp.global_1.BandPassFilter;

import com.ensharp.global_1.BandPassFilter.TarsosDSPAudioFormat.Encoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public abstract class TarsosDSPAudioFloatConverter {
    public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
    private TarsosDSPAudioFormat format;

    public TarsosDSPAudioFloatConverter() {
    }

    public static TarsosDSPAudioFloatConverter getConverter(TarsosDSPAudioFormat format) {
        TarsosDSPAudioFloatConverter conv = null;
        if (format.getFrameSize() == 0) {
            return null;
        } else if (format.getFrameSize() != (format.getSampleSizeInBits() + 7) / 8 * format.getChannels()) {
            return null;
        } else {
            if (format.getEncoding().equals(Encoding.PCM_SIGNED)) {
                if (format.isBigEndian()) {
                    if (format.getSampleSizeInBits() <= 8) {
                        conv = new AudioFloatConversion8S();
                    } else if (format.getSampleSizeInBits() > 8 && format.getSampleSizeInBits() <= 16) {
                        conv = new AudioFloatConversion16SB();
                    } else if (format.getSampleSizeInBits() > 16 && format.getSampleSizeInBits() <= 24) {
                        conv = new AudioFloatConversion24SB();
                    } else if (format.getSampleSizeInBits() > 24 && format.getSampleSizeInBits() <= 32) {
                        conv = new AudioFloatConversion32SB();
                    } else if (format.getSampleSizeInBits() > 32) {
                        conv = new AudioFloatConversion32xSB((format.getSampleSizeInBits() + 7) / 8 - 4);
                    }
                } else if (format.getSampleSizeInBits() <= 8) {
                    conv = new AudioFloatConversion8S();
                } else if (format.getSampleSizeInBits() > 8 && format.getSampleSizeInBits() <= 16) {
                    conv = new AudioFloatConversion16SL();
                } else if (format.getSampleSizeInBits() > 16 && format.getSampleSizeInBits() <= 24) {
                    conv = new AudioFloatConversion24SL();
                } else if (format.getSampleSizeInBits() > 24 && format.getSampleSizeInBits() <= 32) {
                    conv = new AudioFloatConversion32SL();
                } else if (format.getSampleSizeInBits() > 32) {
                    conv = new AudioFloatConversion32xSL((format.getSampleSizeInBits() + 7) / 8 - 4);
                }
            } else if (format.getEncoding().equals(Encoding.PCM_UNSIGNED)) {
                if (format.isBigEndian()) {
                    if (format.getSampleSizeInBits() <= 8) {
                        conv = new AudioFloatConversion8U();
                    } else if (format.getSampleSizeInBits() > 8 && format.getSampleSizeInBits() <= 16) {
                        conv = new AudioFloatConversion16UB();
                    } else if (format.getSampleSizeInBits() > 16 && format.getSampleSizeInBits() <= 24) {
                        conv = new AudioFloatConversion24UB();
                    } else if (format.getSampleSizeInBits() > 24 && format.getSampleSizeInBits() <= 32) {
                        conv = new AudioFloatConversion32UB();
                    } else if (format.getSampleSizeInBits() > 32) {
                        conv = new AudioFloatConversion32xUB((format.getSampleSizeInBits() + 7) / 8 - 4);
                    }
                } else if (format.getSampleSizeInBits() <= 8) {
                    conv = new AudioFloatConversion8U();
                } else if (format.getSampleSizeInBits() > 8 && format.getSampleSizeInBits() <= 16) {
                    conv = new AudioFloatConversion16UL();
                } else if (format.getSampleSizeInBits() > 16 && format.getSampleSizeInBits() <= 24) {
                    conv = new AudioFloatConversion24UL();
                } else if (format.getSampleSizeInBits() > 24 && format.getSampleSizeInBits() <= 32) {
                    conv = new AudioFloatConversion32UL();
                } else if (format.getSampleSizeInBits() > 32) {
                    conv = new AudioFloatConversion32xUL((format.getSampleSizeInBits() + 7) / 8 - 4);
                }
            } else if (format.getEncoding().equals(PCM_FLOAT)) {
                if (format.getSampleSizeInBits() == 32) {
                    if (format.isBigEndian()) {
                        conv = new AudioFloatConversion32B();
                    } else {
                        conv = new AudioFloatConversion32L();
                    }
                } else if (format.getSampleSizeInBits() == 64) {
                    if (format.isBigEndian()) {
                        conv = new AudioFloatConversion64B();
                    } else {
                        conv = new AudioFloatConversion64L();
                    }
                }
            }

            if ((format.getEncoding().equals(Encoding.PCM_SIGNED) || format.getEncoding().equals(Encoding.PCM_UNSIGNED)) && format.getSampleSizeInBits() % 8 != 0) {
                conv = new AudioFloatLSBFilter((TarsosDSPAudioFloatConverter)conv, format);
            }

            if (conv != null) {
                ((TarsosDSPAudioFloatConverter)conv).format = format;
            }

            return (TarsosDSPAudioFloatConverter)conv;
        }
    }

    public TarsosDSPAudioFormat getFormat() {
        return this.format;
    }

    public abstract float[] toFloatArray(byte[] var1, int var2, float[] var3, int var4, int var5);

    public float[] toFloatArray(byte[] in_buff, float[] out_buff, int out_offset, int out_len) {
        return this.toFloatArray(in_buff, 0, out_buff, out_offset, out_len);
    }

    public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_len) {
        return this.toFloatArray(in_buff, in_offset, out_buff, 0, out_len);
    }

    public float[] toFloatArray(byte[] in_buff, float[] out_buff, int out_len) {
        return this.toFloatArray(in_buff, 0, out_buff, 0, out_len);
    }

    public float[] toFloatArray(byte[] in_buff, float[] out_buff) {
        return this.toFloatArray(in_buff, 0, out_buff, 0, out_buff.length);
    }

    public abstract byte[] toByteArray(float[] var1, int var2, int var3, byte[] var4, int var5);

    public byte[] toByteArray(float[] in_buff, int in_len, byte[] out_buff, int out_offset) {
        return this.toByteArray(in_buff, 0, in_len, out_buff, out_offset);
    }

    public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff) {
        return this.toByteArray(in_buff, in_offset, in_len, out_buff, 0);
    }

    public byte[] toByteArray(float[] in_buff, int in_len, byte[] out_buff) {
        return this.toByteArray(in_buff, 0, in_len, out_buff, 0);
    }

    public byte[] toByteArray(float[] in_buff, byte[] out_buff) {
        return this.toByteArray(in_buff, 0, in_buff.length, out_buff, 0);
    }

    private static class AudioFloatConversion16SB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion16SB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                out_buff[ox++] = (float)((short)(in_buff[ix++] << 8 | in_buff[ix++] & 255)) * 3.051851E-5F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)((double)in_buff[ix++] * 32767.0D);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion16SL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion16SL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int len = out_offset + out_len;

            for(int ox = out_offset; ox < len; ++ox) {
                out_buff[ox] = (float)((short)(in_buff[ix++] & 255 | in_buff[ix++] << 8)) * 3.051851E-5F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ox = out_offset;
            int len = in_offset + in_len;

            for(int ix = in_offset; ix < len; ++ix) {
                int x = (int)((double)in_buff[ix] * 32767.0D);
                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion16UB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion16UB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                out_buff[ox++] = (float)(x - 32767) * 3.051851E-5F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = 32767 + (int)((double)in_buff[ix++] * 32767.0D);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion16UL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion16UL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8;
                out_buff[ox++] = (float)(x - 32767) * 3.051851E-5F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = 32767 + (int)((double)in_buff[ix++] * 32767.0D);
                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion24SB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion24SB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                if (x > 8388607) {
                    x -= 16777216;
                }

                out_buff[ox++] = (float)x * 1.192093E-7F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 8388607.0F);
                if (x < 0) {
                    x += 16777216;
                }

                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion24SL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion24SL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16;
                if (x > 8388607) {
                    x -= 16777216;
                }

                out_buff[ox++] = (float)x * 1.192093E-7F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 8388607.0F);
                if (x < 0) {
                    x += 16777216;
                }

                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion24UB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion24UB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                x -= 8388607;
                out_buff[ox++] = (float)x * 1.192093E-7F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 8388607.0F);
                x += 8388607;
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion24UL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion24UL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16;
                x -= 8388607;
                out_buff[ox++] = (float)x * 1.192093E-7F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 8388607.0F);
                x += 8388607;
                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32B extends TarsosDSPAudioFloatConverter {
        ByteBuffer bytebuffer;
        FloatBuffer floatbuffer;

        private AudioFloatConversion32B() {
            this.bytebuffer = null;
            this.floatbuffer = null;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int in_len = out_len * 4;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < in_len) {
                this.bytebuffer = ByteBuffer.allocate(in_len).order(ByteOrder.BIG_ENDIAN);
                this.floatbuffer = this.bytebuffer.asFloatBuffer();
            }

            this.bytebuffer.position(0);
            this.floatbuffer.position(0);
            this.bytebuffer.put(in_buff, in_offset, in_len);
            this.floatbuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int out_len = in_len * 4;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < out_len) {
                this.bytebuffer = ByteBuffer.allocate(out_len).order(ByteOrder.BIG_ENDIAN);
                this.floatbuffer = this.bytebuffer.asFloatBuffer();
            }

            this.floatbuffer.position(0);
            this.bytebuffer.position(0);
            this.floatbuffer.put(in_buff, in_offset, in_len);
            this.bytebuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }
    }

    private static class AudioFloatConversion32L extends TarsosDSPAudioFloatConverter {
        ByteBuffer bytebuffer;
        FloatBuffer floatbuffer;

        private AudioFloatConversion32L() {
            this.bytebuffer = null;
            this.floatbuffer = null;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int in_len = out_len * 4;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < in_len) {
                this.bytebuffer = ByteBuffer.allocate(in_len).order(ByteOrder.LITTLE_ENDIAN);
                this.floatbuffer = this.bytebuffer.asFloatBuffer();
            }

            this.bytebuffer.position(0);
            this.floatbuffer.position(0);
            this.bytebuffer.put(in_buff, in_offset, in_len);
            this.floatbuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int out_len = in_len * 4;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < out_len) {
                this.bytebuffer = ByteBuffer.allocate(out_len).order(ByteOrder.LITTLE_ENDIAN);
                this.floatbuffer = this.bytebuffer.asFloatBuffer();
            }

            this.floatbuffer.position(0);
            this.bytebuffer.position(0);
            this.floatbuffer.put(in_buff, in_offset, in_len);
            this.bytebuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }
    }

    private static class AudioFloatConversion32SB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion32SB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 24 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                out_buff[ox++] = (byte)(x >>> 24);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32SL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion32SL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 24;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 24);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32UB extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion32UB() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 24 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                x -= 2147483647;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                x += 2147483647;
                out_buff[ox++] = (byte)(x >>> 24);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32UL extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion32UL() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 24;
                x -= 2147483647;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                x += 2147483647;
                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 24);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32xSB extends TarsosDSPAudioFloatConverter {
        final int xbytes;

        public AudioFloatConversion32xSB(int xbytes) {
            this.xbytes = xbytes;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 24 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                ix += this.xbytes;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                out_buff[ox++] = (byte)(x >>> 24);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;

                for(int j = 0; j < this.xbytes; ++j) {
                    out_buff[ox++] = 0;
                }
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32xSL extends TarsosDSPAudioFloatConverter {
        final int xbytes;

        public AudioFloatConversion32xSL(int xbytes) {
            this.xbytes = xbytes;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                ix += this.xbytes;
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 24;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);

                for(int j = 0; j < this.xbytes; ++j) {
                    out_buff[ox++] = 0;
                }

                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 24);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32xUB extends TarsosDSPAudioFloatConverter {
        final int xbytes;

        public AudioFloatConversion32xUB(int xbytes) {
            this.xbytes = xbytes;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                int x = (in_buff[ix++] & 255) << 24 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 8 | in_buff[ix++] & 255;
                ix += this.xbytes;
                x -= 2147483647;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)((double)in_buff[ix++] * 2.147483647E9D);
                x += 2147483647;
                out_buff[ox++] = (byte)(x >>> 24);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)x;

                for(int j = 0; j < this.xbytes; ++j) {
                    out_buff[ox++] = 0;
                }
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion32xUL extends TarsosDSPAudioFloatConverter {
        final int xbytes;

        public AudioFloatConversion32xUL(int xbytes) {
            this.xbytes = xbytes;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                ix += this.xbytes;
                int x = in_buff[ix++] & 255 | (in_buff[ix++] & 255) << 8 | (in_buff[ix++] & 255) << 16 | (in_buff[ix++] & 255) << 24;
                x -= 2147483647;
                out_buff[ox++] = (float)x * 4.656613E-10F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                int x = (int)(in_buff[ix++] * 2.14748365E9F);
                x += 2147483647;

                for(int j = 0; j < this.xbytes; ++j) {
                    out_buff[ox++] = 0;
                }

                out_buff[ox++] = (byte)x;
                out_buff[ox++] = (byte)(x >>> 8);
                out_buff[ox++] = (byte)(x >>> 16);
                out_buff[ox++] = (byte)(x >>> 24);
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion64B extends TarsosDSPAudioFloatConverter {
        ByteBuffer bytebuffer;
        DoubleBuffer floatbuffer;
        double[] double_buff;

        private AudioFloatConversion64B() {
            this.bytebuffer = null;
            this.floatbuffer = null;
            this.double_buff = null;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int in_len = out_len * 8;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < in_len) {
                this.bytebuffer = ByteBuffer.allocate(in_len).order(ByteOrder.BIG_ENDIAN);
                this.floatbuffer = this.bytebuffer.asDoubleBuffer();
            }

            this.bytebuffer.position(0);
            this.floatbuffer.position(0);
            this.bytebuffer.put(in_buff, in_offset, in_len);
            if (this.double_buff == null || this.double_buff.length < out_len + out_offset) {
                this.double_buff = new double[out_len + out_offset];
            }

            this.floatbuffer.get(this.double_buff, out_offset, out_len);
            int out_offset_end = out_offset + out_len;

            for(int i = out_offset; i < out_offset_end; ++i) {
                out_buff[i] = (float)this.double_buff[i];
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int out_len = in_len * 8;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < out_len) {
                this.bytebuffer = ByteBuffer.allocate(out_len).order(ByteOrder.BIG_ENDIAN);
                this.floatbuffer = this.bytebuffer.asDoubleBuffer();
            }

            this.floatbuffer.position(0);
            this.bytebuffer.position(0);
            if (this.double_buff == null || this.double_buff.length < in_offset + in_len) {
                this.double_buff = new double[in_offset + in_len];
            }

            int in_offset_end = in_offset + in_len;

            for(int i = in_offset; i < in_offset_end; ++i) {
                this.double_buff[i] = (double)in_buff[i];
            }

            this.floatbuffer.put(this.double_buff, in_offset, in_len);
            this.bytebuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }
    }

    private static class AudioFloatConversion64L extends TarsosDSPAudioFloatConverter {
        ByteBuffer bytebuffer;
        DoubleBuffer floatbuffer;
        double[] double_buff;

        private AudioFloatConversion64L() {
            this.bytebuffer = null;
            this.floatbuffer = null;
            this.double_buff = null;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int in_len = out_len * 8;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < in_len) {
                this.bytebuffer = ByteBuffer.allocate(in_len).order(ByteOrder.LITTLE_ENDIAN);
                this.floatbuffer = this.bytebuffer.asDoubleBuffer();
            }

            this.bytebuffer.position(0);
            this.floatbuffer.position(0);
            this.bytebuffer.put(in_buff, in_offset, in_len);
            if (this.double_buff == null || this.double_buff.length < out_len + out_offset) {
                this.double_buff = new double[out_len + out_offset];
            }

            this.floatbuffer.get(this.double_buff, out_offset, out_len);
            int out_offset_end = out_offset + out_len;

            for(int i = out_offset; i < out_offset_end; ++i) {
                out_buff[i] = (float)this.double_buff[i];
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int out_len = in_len * 8;
            if (this.bytebuffer == null || this.bytebuffer.capacity() < out_len) {
                this.bytebuffer = ByteBuffer.allocate(out_len).order(ByteOrder.LITTLE_ENDIAN);
                this.floatbuffer = this.bytebuffer.asDoubleBuffer();
            }

            this.floatbuffer.position(0);
            this.bytebuffer.position(0);
            if (this.double_buff == null || this.double_buff.length < in_offset + in_len) {
                this.double_buff = new double[in_offset + in_len];
            }

            int in_offset_end = in_offset + in_len;

            for(int i = in_offset; i < in_offset_end; ++i) {
                this.double_buff[i] = (double)in_buff[i];
            }

            this.floatbuffer.put(this.double_buff, in_offset, in_len);
            this.bytebuffer.get(out_buff, out_offset, out_len);
            return out_buff;
        }
    }

    private static class AudioFloatConversion8S extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion8S() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                out_buff[ox++] = (float)in_buff[ix++] * 0.007874016F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                out_buff[ox++] = (byte)((int)(in_buff[ix++] * 127.0F));
            }

            return out_buff;
        }
    }

    private static class AudioFloatConversion8U extends TarsosDSPAudioFloatConverter {
        private AudioFloatConversion8U() {
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < out_len; ++i) {
                out_buff[ox++] = (float)((in_buff[ix++] & 255) - 127) * 0.007874016F;
            }

            return out_buff;
        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            int ix = in_offset;
            int ox = out_offset;

            for(int i = 0; i < in_len; ++i) {
                out_buff[ox++] = (byte)((int)(127.0F + in_buff[ix++] * 127.0F));
            }

            return out_buff;
        }
    }

    private static class AudioFloatLSBFilter extends TarsosDSPAudioFloatConverter {
        private TarsosDSPAudioFloatConverter converter;
        private final int offset;
        private final int stepsize;
        private final byte mask;
        private byte[] mask_buffer;

        public AudioFloatLSBFilter(TarsosDSPAudioFloatConverter converter, TarsosDSPAudioFormat format) {
            int bits = format.getSampleSizeInBits();
            boolean bigEndian = format.isBigEndian();
            this.converter = converter;
            this.stepsize = (bits + 7) / 8;
            this.offset = bigEndian ? this.stepsize - 1 : 0;
            int lsb_bits = bits % 8;
            if (lsb_bits == 0) {
                this.mask = 0;
            } else if (lsb_bits == 1) {
                this.mask = -128;
            } else if (lsb_bits == 2) {
                this.mask = -64;
            } else if (lsb_bits == 3) {
                this.mask = -32;
            } else if (lsb_bits == 4) {
                this.mask = -16;
            } else if (lsb_bits == 5) {
                this.mask = -8;
            } else if (lsb_bits == 6) {
                this.mask = -4;
            } else if (lsb_bits == 7) {
                this.mask = -2;
            } else {
                this.mask = -1;
            }

        }

        public byte[] toByteArray(float[] in_buff, int in_offset, int in_len, byte[] out_buff, int out_offset) {
            byte[] ret = this.converter.toByteArray(in_buff, in_offset, in_len, out_buff, out_offset);
            int out_offset_end = in_len * this.stepsize;

            for(int i = out_offset + this.offset; i < out_offset_end; i += this.stepsize) {
                out_buff[i] &= this.mask;
            }

            return ret;
        }

        public float[] toFloatArray(byte[] in_buff, int in_offset, float[] out_buff, int out_offset, int out_len) {
            if (this.mask_buffer == null || this.mask_buffer.length < in_buff.length) {
                this.mask_buffer = new byte[in_buff.length];
            }

            System.arraycopy(in_buff, 0, this.mask_buffer, 0, in_buff.length);
            int in_offset_end = out_len * this.stepsize;

            for(int i = in_offset + this.offset; i < in_offset_end; i += this.stepsize) {
                this.mask_buffer[i] &= this.mask;
            }

            float[] ret = this.converter.toFloatArray(this.mask_buffer, in_offset, out_buff, out_offset, out_len);
            return ret;
        }
    }
}
