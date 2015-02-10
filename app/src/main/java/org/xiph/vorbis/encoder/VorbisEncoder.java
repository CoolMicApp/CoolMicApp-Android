package org.xiph.vorbis.encoder;

/**
 * The native encoder to interface via JNI
 * User: vincent
 * Date: 3/27/13
 * Time: 9:07 AM
 */
public class VorbisEncoder {
    /**
     * Load our vorbis-jni library as well as the other dependent libraries
     */
    static {
        System.loadLibrary("ogg"); 
        System.loadLibrary("vorbis");
        System.loadLibrary("vorbis-jni");
    }

    /**
     * The native JNI method call to the encoder to start encoding raw pcm data to encoded vorbis data
     *
     * @param sampleRate       the sample rate which the incoming pcm data will arrive
     * @param numberOfChannels the number of channels
     * @param quality          the quality to encode the output vorbis data
     * @param encodeFeed       the custom encoder feed
     */
    public static native int startEncodingWithQuality(long sampleRate, long numberOfChannels, float quality, EncodeFeed encodeFeed, String title, String artist);

    /**
     * The native JNI method call to the encoder to start encoding raw pcm data to encoded vorbis data
     *
     * @param sampleRate       the sample rate which the incoming pcm data will arrive
     * @param numberOfChannels the number of channels
     * @param bitrate          the bitrate of the output vorbis data
     * @param encodeFeed       the custom encoder feed
     */
    public static native int startEncodingWithBitrate(long sampleRate, long numberOfChannels, long bitrate, EncodeFeed encodeFeed, String title, String artist);
}
