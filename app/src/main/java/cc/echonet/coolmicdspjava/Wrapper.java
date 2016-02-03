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
