/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2021
 *       on behalf of Jordan Erickson.
 *
 * This file is part of Cool Mic.
 *
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cc.echonet.coolmicdspjava;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class wraps libcoolmic-dsp's simple API.
 * <P>
 * The general workflow consists of calling:
 * <ul>
 *     <li>{@link #prepare(CallbackHandler, String, int, String, String, String, String, int, int, int, String, String, String, String[])} to prepare the stream.</li>
 *     <li>Optionally call setters for additional settings.</li>
 *     <li>{@link #start()} to start recording.</li>
 *     <li>Optionally call setters for updating the stream or {@link #nextSegment(InputStreamAdapter)} to start a new segment.</li>
 *     <li>{@link #close()} when done.</li>
 * </ul>
 */
public final class Wrapper implements Closeable {
    private static final @NotNull String[] auxiliaryLibraries = new String[]{"ogg", "vorbis", "shout", "coolmic-dsp"};
    private static final @NotNull String[] mainLibraries = new String[]{"coolmic-dsp-java"};
    private static final @NotNull Logger LOGGER = Logger.getLogger(Wrapper.class.getName());

    private static @NonNull WrapperConstants.WrapperInitializationStatus state;
    private static @Nullable Throwable initException;

    public static final int CONNECTION_STATE_CONNECTING = 1;
    public static final int CONNECTION_STATE_CONNECTED = 2;
    public static final int CONNECTION_STATE_DISCONNECTING = 3;
    public static final int CONNECTION_STATE_DISCONNECTED = 4;
    public static final int CONNECTION_STATE_CONNECTION_ERROR = 5;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private long nativeObject = 0x0;

    static {
        for (final @NotNull String library : auxiliaryLibraries) {
            try {
                System.loadLibrary(library);
            } catch (Throwable ex) {
                LOGGER.log(Level.SEVERE, "Can not load auxiliary library: " + library, ex);
            }
        }

        try {
            for (final @NotNull String library : mainLibraries) {
                try {
                    System.loadLibrary(library);
                } catch (Throwable ex) {
                    LOGGER.log(Level.SEVERE, "Can not load main library: " + library, ex);
                    throw ex;
                }
            }

            initException = null;
            state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED;
        } catch (Throwable ex) {
            initException = ex;
            state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR;
        }
    }

    private @NotNull String[] mapToArray(@NotNull Map<String, String> map) {
        final @NotNull String[] array = new String[map.size() * 2];
        int i = 0;

        for (final @NotNull Map.Entry<String, String> entry : map.entrySet()) {
            array[i++] = entry.getKey();
            array[i++] = entry.getValue();
        }

        return array;
    }

    /**
     * Gets the state of the object.
     * This should be {@link WrapperConstants.WrapperInitializationStatus#WRAPPER_INTITIALIZED}.
     * @return The state.
     */
    public synchronized @NonNull WrapperConstants.WrapperInitializationStatus getState() {
        return state;
    }

    /**
     * Gets the exception cough when trying to initialise.
     * @return The exception or {@code null}.
     */
    public synchronized @Nullable Throwable getInitException() {
        return initException;
    }

    /**
     * Gets the static initialization state.
     * This is only useful for debugging.
     * @return The initialization state as {@link String}.
     */
    @ApiStatus.Experimental
    public static @NotNull String getStaticInitializationState() {
        if (initException == null) {
            return state.toString();
        } else {
            return state + " " + initException;
        }
    }

    private synchronized native int prepare(CallbackHandler handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize, String softwareName, String softwareVersion, String softwareComment, String[] station);

    /**
     * Prepares the wrapper for streaming.
     *
     * @param handler The callback handler used for updates from the library to the Java world.
     * @param hostname The hostname to connect to.
     * @param port The port to connect to.
     * @param username The username to connect with.
     * @param password The password to connect with.
     * @param mount The mount point to connect to.
     * @param codec The codec to use.
     * @param rate The sample rate to use.
     * @param channels The number of channels to use.
     * @param buffersize The buffer site to use.
     * @param station {@link Map} of station metadata. The keys must be from {@code SHOUT_META_}*.
     * @return TODO
     */
    public synchronized int prepare(CallbackHandler handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize, String softwareName, String softwareVersion, String softwareComment, Map<String, String> station) {
        return prepare(handler, hostname, port, username, password, mount, codec, rate, channels, buffersize, softwareName, softwareVersion, softwareComment, mapToArray(station));
    }
    /**
     * This starts streaming.
     * @return TODO
     */
    public synchronized native int start();

    /**
     * This stops streaming and closes the connection.
     */
    @Override
    public synchronized native void close();

    /**
     * Gets the state of the wrapper.
     * Will return true once {@link #prepare(CallbackHandler, String, int, String, String, String, String, int, int, int, String, String, String, String[])}
     * was called and {@link #close()} has not yet been called.
     *
     * @return Whether the wrapper is in prepared state.
     */
    public synchronized native boolean isPrepared();

    /**
     * Sets the interval for generating VU-Meter events.
     * @param interval The interval [magic] or zero to disable.
     * @return TODO
     */
    public synchronized native int setVuMeterInterval(int interval);

    /**
     * Updates the metadata and quality settings for the stream and applies those parameters.
     * @param track {@link Map} of track metadata.
     * @param quality The new quality to use in the range of [-0.1:+1.0].
     * @param restart Whether the settings should be applied.
     * @return TODO
     */
    public synchronized int performMetaDataQualityUpdate(@NotNull Map<@NotNull String, @NotNull String> track, double quality, boolean restart) {
        return performMetaDataQualityUpdate(mapToArray(track), quality, restart ? 1 : 0);
    }

    private synchronized native int performMetaDataQualityUpdate(String[] track, double quality, int restart);

    /**
     * Sets the profile for reconnecting to the server in case the connection is lost.
     * @param profile The profile to use or {@code "disabled"}, {@code "enabled"}, or {@code "default"}.
     * @return TODO
     */
    public synchronized native int setReconnectionProfile(String profile);

    /**
     * Sets the master gain.
     * @param scale The base unit to use of {@code gain}.
     * @param gain The value to set the master gain for all channels to in [scale].
     * @return TODO
     */
    public synchronized native int setMasterGain(int scale, int gain);

    /**
     * Sets the master gain.
     * @param scale The base unit to use of {@code gain}.
     * @param gainLeft The value to set the master gain for the left channel to in [scale].
     * @param gainRight The value to set the master gain for the right channel to in [scale].
     * @return TODO
     */
    public synchronized native int setMasterGain(int scale, int gainLeft, int gainRight);

    /**
     * Schedules and switches to the next segment.
     * @param inputStreamAdapter The adapter for the next segment or {@code null} for a live segment.
     * @return TODO
     */
    public synchronized native int nextSegment(@Nullable InputStreamAdapter inputStreamAdapter);

    /**
     * Schedules and switches to the next segment.
     * @param inputStream The {@link InputStream} for the next segment.
     * @return TODO
     * @see #nextSegment(InputStreamAdapter)
     */
    public synchronized int nextSegment(@NonNull InputStream inputStream) {
        return nextSegment(new InputStreamAdapter(inputStream));
    }

    /**
     * Schedules and switches to a live segment.
     * @return TODO
     * @see #nextSegment(InputStreamAdapter)
     */
    public synchronized int nextSegment() {
        return nextSegment((InputStreamAdapter) null);
    }
}
