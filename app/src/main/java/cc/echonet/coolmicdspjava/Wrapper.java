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

/**
 * Created by stephanj on 2/22/15.
 */
public final class Wrapper {

    private static WrapperConstants.WrapperInitializationStatus state = WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED;
    private static Throwable initException = null;

    public static synchronized WrapperConstants.WrapperInitializationStatus getState() {
        return state;
    }

    public static synchronized Throwable getInitException() {
        return initException;
    }

    public static synchronized WrapperConstants.WrapperInitializationStatus init() {
        if (state == WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED) {
            try {
                System.loadLibrary("ogg");
                System.loadLibrary("vorbis");
                System.loadLibrary("shout");
                System.loadLibrary("coolmic-dsp");
                System.loadLibrary("coolmic-dsp-java");

                initNative();

                state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED;
            } catch (Throwable ex) {
                initException = ex;
                state = WrapperConstants.WrapperInitializationStatus.WRAPPER_INITIALIZATION_ERROR;
            }
        }

        return state;
    }

    public static synchronized native int start();

    public static synchronized native int stop();

    public static synchronized native int ref();

    public static synchronized native int unref();

    public static synchronized native boolean hasCore();

    public static synchronized native int setVuMeterInterval(int interval);

    public static synchronized native int performMetaDataQualityUpdate(String title, String artist, double quality, int restart);

    public static synchronized native int setReconnectionProfile(String profile);

    public static synchronized native int setMasterGainMono(int scale, int gain);

    public static synchronized native int setMasterGainStereo(int scale, int gain_left, int gain_right);

    public static synchronized native int resetMasterGain();

    public static synchronized native int nextSegment(InputStreamAdapter inputStreamAdapter);

    public static synchronized native void initNative();

    public static synchronized native int init(CallbackHandler handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize);
}
