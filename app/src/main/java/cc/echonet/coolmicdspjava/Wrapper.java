/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
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
 */
package cc.echonet.coolmicdspjava;

/**
 * Created by stephanj on 2/22/15.
 */
public class Wrapper {

    private static WrapperConstants.WrapperInitializationStatus state = WrapperConstants.WrapperInitializationStatus.WRAPPER_UNINITIALIZED;
    private static Throwable initException = null;

    public static WrapperConstants.WrapperInitializationStatus getState() {
        return state;
    }

    public static Throwable getInitException() {
        return initException;
    }

    public static WrapperConstants.WrapperInitializationStatus init() {
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

        return state;
    }

    public static native int start();

    public static native int stop();

    public static native int ref();

    public static native int unref();

    public static native boolean hasCore();

    public static native int setVuMeterInterval(int interval);

    public static native int performMetaDataQualityUpdate(String title, String artist, double quality, int restart);

    public static native int setReconnectionProfile(String profile);

    public static native int setMasterGainMono(int scale, int gain);

    public static native int setMasterGainStereo(int scale, int gain_left, int gain_right);

    public static native int resetMasterGain();

    public static native void initNative();

    public static native int init(Object handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize);
}
