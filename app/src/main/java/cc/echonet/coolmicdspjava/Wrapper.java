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

import android.app.Activity;

/**
 * Created by stephanj on 2/22/15.
 */
public class Wrapper {
    static {
        System.loadLibrary("ogg");
        System.loadLibrary("vorbis");
        System.loadLibrary("shout");
        System.loadLibrary("cc.echonet.coolmicapp-dsp");
        System.loadLibrary("cc.echonet.coolmicapp-dsp-java");
    }

    public static native int start();
    public static native int stop();
    public static native int ref();
    public static native int unref();
    public static native void init(Activity handler, String hostname, int port, String username, String password, String mount, String codec, int rate, int channels, int buffersize);
}
