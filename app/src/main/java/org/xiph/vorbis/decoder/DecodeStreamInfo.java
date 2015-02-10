package org.xiph.vorbis.decoder;

/**
 * User: vincent
 * Date: 3/29/13
 * Time: 8:17 PM
 */
public class DecodeStreamInfo {
    private long sampleRate;

    private long channels;

    private String vendor;

    public DecodeStreamInfo(long sampleRate, long channels, String vendor) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.vendor = vendor;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    public long getChannels() {
        return channels;
    }

    public void setChannels(long channels) {
        this.channels = channels;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
