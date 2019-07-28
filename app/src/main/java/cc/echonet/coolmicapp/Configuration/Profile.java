package cc.echonet.coolmicapp.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import cc.echonet.coolmicapp.R;

public class Profile extends ProfileBase {
    private static final int DEFAULT_VOLUME = 100;

    public Profile(Profile profile) {
        super(profile);
    }

    public Profile(Context context, String profile) {
        super(context, profile);
    }

    //Stolen from: http://stackoverflow.com/a/23704728 & http://stackoverflow.com/a/18879453 (removed the shortening because it did not work reliably)
    public Track getTrack() {
        return new Track(this);
    }

    public Server getServer() {
        return new Server(this);
    }

    public Audio getAudio() {
        return new Audio(this);
    }

    public Codec getCodec() {
        return new Codec(this, getAudio());
    }

    public VUMeter getVUMeter() {
        return new VUMeter(this);
    }

    public Volume getVolume() {
        return new Volume(this, getAudio());
    }
}
