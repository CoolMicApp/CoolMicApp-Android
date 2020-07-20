/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
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

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import java.io.Closeable;

/**
 * Created by stephanj on 2/22/15.
 */
public final class Wrapper implements Closeable {
    private static @NonNull WrapperConstants.WrapperInitializationStatus state;
    private static @Nullable Throwable initException;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private long nativeObject = 0x0;

    static {
        try {
            System.loadLibrary("ogg");
            System.loadLibrary("vorbis");
            System.loadLibrary("shout");
            System.loadLibrary("coolmic-dsp");
            System.loadLibrary("coolmic-dsp-java");

            initException = null;
            state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED;
        } catch (Throwable ex) {
            initException = ex;
            state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR;
        }
    }

    public synchronized @NonNull WrapperConstants.WrapperInitializationStatus getState() {
        return state;
    }

    public synchronized @Nullable Throwable getInitException() {
        return initException;
    }

    public synchronized native int start();

    @Override
    public synchronized native void close();

    public synchronized native boolean hasCore();

    public synchronized native int setVuMeterInterval(int interval);

    public synchronized native int performMetaDataQualityUpdate(String title, String artist, double quality, int restart);

    public synchronized native int setReconnectionProfile(String profile);

    public synchronized native int setMasterGainMono(int scale, int gain);

    public synchronized native int setMasterGainStereo(int scale, int gain_left, int gain_right);

    public synchronized native int nextSegment(InputStreamAdapter inputStreamAdapter);

    public synchronized native int init(CallbackHandler handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize);
}
