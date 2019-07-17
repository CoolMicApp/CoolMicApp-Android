package cc.echonet.coolmicapp.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import cc.echonet.coolmicapp.R;
import cc.echonet.coolmicapp.Utils;

public class Profile extends ProfileBase {
    private static final int DEFAULT_VOLUME = 100;

    public Profile(Context context, String profile) {
        super(context, profile);
    }

    //Stolen from: http://stackoverflow.com/a/23704728 & http://stackoverflow.com/a/18879453 (removed the shortening because it did not work reliably)
    private static String generateShortUuid() {
        UUID uuid = UUID.randomUUID();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return uuid.toString();
        }


        md.update(uuid.toString().getBytes());
        byte[] digest = md.digest();

        return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING).substring(0, 20);
    }

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

    public void loadCMTSData() {
        SharedPreferences.Editor editor = prefs.edit();

        String randomComponent = generateShortUuid();

        editor.putString("connection_address", context.getString(R.string.pref_default_connection_address));
        editor.putString("connection_username", context.getString(R.string.pref_default_connection_username));
        editor.putString("connection_password", context.getString(R.string.pref_default_connection_password));
        editor.putString("connection_mountpoint", context.getString(R.string.pref_default_connection_mountpoint, randomComponent));
        editor.putString("audio_codec", context.getString(R.string.pref_default_audio_codec));
        editor.putString("audio_samplerate", context.getString(R.string.pref_default_audio_samplerate));

        editor.apply();
    }
}
