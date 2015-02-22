package cc.echonet.coolmicdspjava;

/**
 * Created by stephanj on 2/22/15.
 */
public class Wrapper {
    static {
        System.loadLibrary("ogg");
        System.loadLibrary("vorbis");
        System.loadLibrary("shout");
        System.loadLibrary("coolmic-dsp");
        System.loadLibrary("coolmic-dsp-java");
    }

    public static native int start();
    public static native int stop();
    public static native int ref();
    public static native int unref();
    public static native void init(String codec, int rate, int channels);
}
