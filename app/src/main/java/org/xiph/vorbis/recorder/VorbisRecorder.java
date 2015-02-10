package org.xiph.vorbis.recorder;

import android.os.Handler;
import android.os.Process;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import org.xiph.vorbis.encoder.EncodeFeed;
import org.xiph.vorbis.encoder.VorbisEncoder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The VorbisRecorder is responsible for receiving raw pcm data from the {@link AudioRecord} and feeding that data
 * to the native {@link VorbisEncoder}
 * <p/>
 * This class is primarily intended as a demonstration of how to work with the JNI java interface {@link VorbisEncoder}
 * <p/>
 * User: vincent
 * Date: 3/28/13
 * Time: 12:47 PM
 */
public class VorbisRecorder {
    /**
     * Vorbis recorder status flag to notify handler to start encoding
     */
    public static final int START_ENCODING = 1;

    /**
     * Vorbis recorder status flag to notify handler to that it has stopped encoding
     */
    public static final int STOP_ENCODING = 2;

    /**
     * Vorbis recorder status flag to notify handler that the recorder has finished successfully
     */
    public static final int FINISHED_SUCCESSFULLY = 0;

    /**
     * Vorbis recorder status flag to notify handler that the encoder has failed for an unknown reason
     */
    public static final int FAILED_FOR_UNKNOWN_REASON = -2;

    /**
     * Vorbis recorder status flag to notify handler that the encoder couldn't initialize an {@link AudioRecord}
     */
    public static final int UNSUPPORTED_AUDIO_TRACK_RECORD_PARAMETERS = -3;

    /**
     * Vorbis recorder status flag to notify handler that the encoder has failed to initialize properly
     */
    public static final int ERROR_INITIALIZING = -1;

    /**
     * Whether the recording will encode with a quality percent or average bitrate
     */
    private static enum RecordingType {
        WITH_QUALITY, WITH_BITRATE
    }

    /**
     * The record handler to post status updates to
     */
    private final Handler recordHandler;

    /**
     * The sample rate of the recorder
     */
    private long sampleRate;

    /**
     * The number of channels for the recorder
     */
    private long numberOfChannels;

    /**
     * The output quality of the encoding
     */
    private float quality;

    /**
     * The target encoding bitrate
     */
    private long bitrate;

    /**
     * Whether the recording will encode with a quality percent or average bitrate
     */
    private RecordingType recordingType;

    /**
     * The state of the recorder
     */
    private static enum RecorderState {
        RECORDING, STOPPED, STOPPING
    }

    /**
     * Logging tag
     */
    private static final String TAG = "VorbisRecorder";

    /**
     * The encode feed to feed raw pcm and write vorbis data
     */
    private final EncodeFeed encodeFeed;

    /**
     * The current state of the recorder
     */
    private final AtomicReference<RecorderState> currentState = new AtomicReference<RecorderState>(RecorderState.STOPPED);

    /**
     * Title metadata
     */
    private String metaTitle;

    /**
     * Artist metadata
     */
    private String metaArtist;

    /**
     * Helper class that implements {@link EncodeFeed} that will write the processed vorbis data to a file and will
     * read raw PCM data from an {@link AudioRecord}
     */
    private class FileEncodeFeed implements EncodeFeed {
        /**
         * The file to write to
         */
        private final File fileToSaveTo;

        /**
         * The output stream to write the vorbis data to
         */
        private OutputStream outputStream;

        /**
         * The audio recorder to pull raw pcm data from
         */
        private AudioRecord audioRecorder;

        /**
         * Constructs a file encode feed to write the encoded vorbis output to
         *
         * @param fileToSaveTo the file to save to
         */
        public FileEncodeFeed(File fileToSaveTo) {
            if (fileToSaveTo == null) {
                throw new IllegalArgumentException("File to save to must not be null");
            }
            this.fileToSaveTo = fileToSaveTo;
        }

        @Override
        public long readPCMData(byte[] pcmDataBuffer, int amountToRead) {
            //If we are no longer recording, return 0 to let the native encoder know
            if (isStopped() || isStopping()) {
                return 0;
            }

            //Otherwise read from the audio recorder
            int read = audioRecorder.read(pcmDataBuffer, 0, amountToRead);
            switch (read) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    Log.e(TAG, "Invalid operation on AudioRecord object");
                    return 0;
                case AudioRecord.ERROR_BAD_VALUE:
                    Log.e(TAG, "Invalid value returned from audio recorder");
                    return 0;
                case -1:
                    return 0;
                default:
                    //Successfully read from audio recorder
                    return read;
            }
        }

        @Override
        public int writeVorbisData(byte[] vorbisData, int amountToWrite) {
            //If we have data to write and we are recording, write the data
            if (vorbisData != null && amountToWrite > 0 && outputStream != null && !isStopped()) {
                try {
                    //Write the data to the output stream
                    outputStream.write(vorbisData, 0, amountToWrite);
                    return amountToWrite;
                } catch (IOException e) {
                    //Failed to write to the file
                    Log.e(TAG, "Failed to write data to file, stopping recording", e);
                    stop();
                }
            }
            //Otherwise let the native encoder know we are done
            return 0;
        }

        @Override
        public void stop() {
            recordHandler.sendEmptyMessage(STOP_ENCODING);

            if (isRecording() || isStopping()) {
                //Set our state to stopped
                currentState.set(RecorderState.STOPPED);

                //Close the output stream
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close output stream", e);
                    }
                    outputStream = null;
                }

                //Stop and clean up the audio recorder
                if (audioRecorder != null) {
                    audioRecorder.stop();
                    audioRecorder.release();
                }
            }
        }

        @Override
        public void stopEncoding() {
            if (isRecording()) {
                //Set our state to stopped
                currentState.set(RecorderState.STOPPING);
            }
        }

        @Override
        public void start() {
            if (isStopped()) {
                recordHandler.sendEmptyMessage(START_ENCODING);

                //Creates the audio recorder
                int channelConfiguration = numberOfChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
                int bufferSize = AudioRecord.getMinBufferSize((int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT);

                if(bufferSize < 0) {
                    recordHandler.sendEmptyMessage(UNSUPPORTED_AUDIO_TRACK_RECORD_PARAMETERS);
                }

                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, (int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                //Start recording
                currentState.set(RecorderState.RECORDING);
                audioRecorder.startRecording();

                //Create the output stream
                if (outputStream == null) {
                    try {
                        outputStream = new BufferedOutputStream(new FileOutputStream(fileToSaveTo));
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "Failed to write to file", e);
                    }
                }
            }
        }
    }

    /**
     * Helper class that implements {@link EncodeFeed} that will write the processed vorbis data to an output stream
     * and will read raw PCM data from an {@link AudioRecord}
     */
    private class OutputStreamEncodeFeed implements EncodeFeed {
        /**
         * The output stream to write the vorbis data to
         */
        private OutputStream outputStream;

        /**
         * The audio recorder to pull raw pcm data from
         */
        private AudioRecord audioRecorder;

        /**
         * Constructs a file encode feed to write the encoded vorbis output to
         *
         * @param outputStream the {@link OutputStream} to write the encoded information to
         */
        public OutputStreamEncodeFeed(OutputStream outputStream) {
            if (outputStream == null) {
                throw new IllegalArgumentException("The output stream must not be null");
            }
            this.outputStream = outputStream;
        }

        @Override
        public long readPCMData(byte[] pcmDataBuffer, int amountToRead) {
            //If we are no longer recording, return 0 to let the native encoder know
            if (isStopped() || isStopping()) {
                return 0;
            }

            //Otherwise read from the audio recorder
            int read = audioRecorder.read(pcmDataBuffer, 0, amountToRead);
            switch (read) {
                case AudioRecord.ERROR_INVALID_OPERATION:
                    Log.e(TAG, "Invalid operation on AudioRecord object");
                    return 0;
                case AudioRecord.ERROR_BAD_VALUE:
                    Log.e(TAG, "Invalid value returned from audio recorder");
                    return 0;
                case -1:
                    return 0;
                default:
                    //Successfully read from audio recorder
                    return read;
            }
        }

        @Override
        public int writeVorbisData(byte[] vorbisData, int amountToWrite) {
            //If we have data to write and we are recording, write the data
            if (vorbisData != null && amountToWrite > 0 && outputStream != null && !isStopped()) {
                try {
                    //Write the data to the output stream
                    outputStream.write(vorbisData, 0, amountToWrite);
                    return amountToWrite;
                } catch (IOException e) {
                    //Failed to write to the file
                    Log.e(TAG, "Failed to write data to file, stopping recording", e);
                    stop();
                }
            }
            //Otherwise let the native encoder know we are done
            return 0;
        }

        @Override
        public void stop() {
            recordHandler.sendEmptyMessage(STOP_ENCODING);

            if (isRecording() || isStopping()) {
                //Set our state to stopped
                currentState.set(RecorderState.STOPPED);

                //Close the output stream
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close output stream", e);
                    }
                    outputStream = null;
                }

                //Stop and clean up the audio recorder
                if (audioRecorder != null) {
                    audioRecorder.stop();
                    audioRecorder.release();
                }
            }
        }

        @Override
        public void stopEncoding() {
            if (isRecording()) {
                //Set our state to stopped
                currentState.set(RecorderState.STOPPING);
            }
        }

        @Override
        public void start() {
		    if (isStopped()) {
		        recordHandler.sendEmptyMessage(START_ENCODING);
		
		        //Creates the audio recorder
		        int channelConfiguration = numberOfChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
		        int bufferSize = AudioRecord.getMinBufferSize((int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT);
		        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, (int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		
		        //Start recording
		        currentState.set(RecorderState.RECORDING);
		        audioRecorder.startRecording();
		    }
        }
    }

    /**
     * Constructs a recorder that will record an ogg file
     *
     * @param fileToSaveTo  the file to save to
     * @param recordHandler the handler for receiving status updates about the recording process
     */
    public VorbisRecorder(File fileToSaveTo, Handler recordHandler, String title, String artist) {
        if (fileToSaveTo == null) {
            throw new IllegalArgumentException("File to play must not be null.");
        }

        //Delete the file if it exists
        if (fileToSaveTo.exists()) {
            fileToSaveTo.deleteOnExit();
        }

        this.encodeFeed = new FileEncodeFeed(fileToSaveTo);
        this.recordHandler = recordHandler;
        this.metaTitle = title;
        this.metaArtist = artist;
    }

    /**
     * Constructs a recorder that will record an ogg output stream
     *
     * @param streamToWriteTo the output stream to write the encoded information to
     * @param recordHandler   the handler for receiving status updates about the recording process
     */
    public VorbisRecorder(OutputStream streamToWriteTo, Handler recordHandler, String title, String artist) {
        if (streamToWriteTo == null) {
            throw new IllegalArgumentException("File to play must not be null.");
        }

        this.encodeFeed = new OutputStreamEncodeFeed(streamToWriteTo);
        this.recordHandler = recordHandler;
        this.metaTitle = title;
        this.metaArtist = artist;
    }

    /**
     * Constructs a vorbis recorder with a custom {@link EncodeFeed}
     *
     * @param encodeFeed    the custom {@link EncodeFeed}
     * @param recordHandler the handler for receiving status updates about the recording process
     */
    public VorbisRecorder(EncodeFeed encodeFeed, Handler recordHandler) {
        if (encodeFeed == null) {
            throw new IllegalArgumentException("Encode feed must not be null.");
        }

        this.encodeFeed = encodeFeed;
        this.recordHandler = recordHandler;
    }

    /**
     * Starts the recording/encoding process
     *
     * @param sampleRate       the rate to sample the audio at, should be greater than <code>0</code>
     * @param numberOfChannels the nubmer of channels, must only be <code>1/code> or <code>2</code>
     * @param quality          the quality at which to encode, must be between <code>-0.1</code> and <code>1.0</code>
     */
    @SuppressWarnings("all")
    public synchronized void start(long sampleRate, long numberOfChannels, float quality) {
        if (isStopped()) {
            if (numberOfChannels != 1 && numberOfChannels != 2) {
                throw new IllegalArgumentException("Channels can only be one or two");
            }
            if (sampleRate <= 0) {
                throw new IllegalArgumentException("Invalid sample rate, must be above 0");
            }
            if (quality < -0.1f || quality > 1.0f) {
                throw new IllegalArgumentException("Quality must be between -0.1 and 1.0");
            }

            this.sampleRate = sampleRate;
            this.numberOfChannels = numberOfChannels;
            this.quality = quality;
            this.recordingType = RecordingType.WITH_QUALITY;

            //Starts the recording process
            new Thread(new AsyncEncoding()).start();
        }
    }

    /**
     * Starts the recording/encoding process
     *
     * @param sampleRate       the rate to sample the audio at, should be greater than <code>0</code>
     * @param numberOfChannels the nubmer of channels, must only be <code>1/code> or <code>2</code>
     * @param bitrate          the bitrate at which to encode, must be greater than <code>-0</code>
     */
    @SuppressWarnings("all")
    public synchronized void start(long sampleRate, long numberOfChannels, long bitrate) {
        if (isStopped()) {
            if (numberOfChannels != 1 && numberOfChannels != 2) {
                throw new IllegalArgumentException("Channels can only be one or two");
            }
            if (sampleRate <= 0) {
                throw new IllegalArgumentException("Invalid sample rate, must be above 0");
            }
            if (bitrate <= 0) {
                throw new IllegalArgumentException("Target bitrate must be greater than 0");
            }

            this.sampleRate = sampleRate;
            this.numberOfChannels = numberOfChannels;
            this.bitrate = bitrate;
            this.recordingType = RecordingType.WITH_BITRATE;

            //Starts the recording process
            new Thread(new AsyncEncoding()).start();
        }
    }

    /**
     * Stops the audio recorder and notifies the {@link EncodeFeed}
     */
    public synchronized void stop() {
        encodeFeed.stopEncoding();
    }

    /**
     * Starts the encoding process in a background thread
     */
    private class AsyncEncoding implements Runnable {
        @Override
        public void run() {
            //Start the native encoder
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            int result = 0;
            switch (recordingType) {
                case WITH_BITRATE:
                    result = VorbisEncoder.startEncodingWithBitrate(sampleRate, numberOfChannels, bitrate, encodeFeed, metaTitle, metaArtist);
                    break;
                case WITH_QUALITY:
                    result = VorbisEncoder.startEncodingWithQuality(sampleRate, numberOfChannels, quality, encodeFeed, metaTitle, metaArtist);
                    break;
            }
            switch (result) {
                case EncodeFeed.SUCCESS:
                    Log.d(TAG, "Encoder successfully finished");
                    recordHandler.sendEmptyMessage(FINISHED_SUCCESSFULLY);
                    break;
                case EncodeFeed.ERROR_INITIALIZING:
                    recordHandler.sendEmptyMessage(ERROR_INITIALIZING);
                    Log.e(TAG, "There was an error initializing the native encoder");
                    break;
                default:
                    recordHandler.sendEmptyMessage(FAILED_FOR_UNKNOWN_REASON);
                    Log.e(TAG, "Encoder returned an unknown result code");
                    break;
            }
        }
    }

    /**
     * Checks whether the recording is currently recording
     *
     * @return <code>true</code> if recording, <code>false</code> otherwise
     */
    public synchronized boolean isRecording() {
        return currentState.get() == RecorderState.RECORDING;
    }

    /**
     * Checks whether the recording is currently stopped (not recording)
     *
     * @return <code>true</code> if stopped, <code>false</code> otherwise
     */
    public synchronized boolean isStopped() {
        return currentState.get() == RecorderState.STOPPED;
    }

    /**
     * Checks whether the recording is currently stopping (not recording)
     *
     * @return <code>true</code> if stopping, <code>false</code> otherwise
     */
    public synchronized boolean isStopping() {
        return currentState.get() == RecorderState.STOPPING;
    }
}
