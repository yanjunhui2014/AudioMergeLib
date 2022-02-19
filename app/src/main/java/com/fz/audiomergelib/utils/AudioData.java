package com.fz.audiomergelib.utils;

import androidx.core.util.Pools;

/**
 * Created by zhouhl on 2018/2/28.
 * AudioData
 */

public class AudioData implements Cloneable{

    private byte[] data;

    private int size;

    private double volume; //分贝

    public AudioData() {
    }

    public AudioData(byte[] data, int size) {
        this.data = data;
        this.size = size;
    }

    public AudioData(byte[] data, int size, double volume) {
        this.data = data;
        this.size = size;
        this.volume = volume;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        if (this.data == null) {
            this.data = new byte[data.length];
        }
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    private static final Pools.SynchronizedPool<AudioData> sPool =
            new Pools.SynchronizedPool<>(10);

    public static AudioData obtain() {
        AudioData instance = sPool.acquire();
        return (instance != null) ? instance : new AudioData();
    }

    public void recycle() {
        sPool.release(this);
    }
}
