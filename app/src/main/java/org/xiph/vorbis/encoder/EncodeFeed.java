package org.xiph.vorbis.encoder;

/**
 * This feed is to be used by the native {@link VorbisEncoder} which will read raw pcm data and write encoded vorbis
 * data.
 * User: vincent
 * Date: 3/27/13
 * Time: 2:11 PM
 */
public interface EncodeFeed {
    /**
     * Everything was a success
     */
    public static final int SUCCESS = 0;

    /**
     * If there was an error initializing the encoder
     */
    public static final int ERROR_INITIALIZING = -44;

    /**
     * Triggered by the native {@link VorbisEncoder} when it needs to read raw pcm data
     *
     * @param pcmDataBuffer the buffer to write the raw pcm data to
     * @param amountToWrite the amount of pcm data to write
     * @return how much was actually written, <code>0</code> to stop the native {@link VorbisEncoder}
     */
    public long readPCMData(byte[] pcmDataBuffer, int amountToWrite);

    /**
     * Triggered by the native {@link VorbisEncoder} when encoded vorbis data is ready to be written
     *
     * @param vorbisData   the encoded vorbis data
     * @param amountToRead the amount of encoded vorbis data that can be read
     * @return how much was actually written
     */
    public int writeVorbisData(byte[] vorbisData, int amountToRead);

    /**
     * To be called by the native encoder notifying the encode feed is complete
     */
    public void stop();

    /**
     * To be called to stop the encoder
     */
    public void stopEncoding();

    /**
     * To be called when the encoding has started
     */
    public void start();
}
