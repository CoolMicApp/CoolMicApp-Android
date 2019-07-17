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

public class Profile {
    private static final int DEFAULT_VOLUME = 100;

    private Context context;
    private SharedPreferences prefs;

    Profile(Context context, String name) {
        this.context = context;
        this.prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);

        if (!this.prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
            PreferenceManager.setDefaultValues(context, name, Context.MODE_PRIVATE, R.xml.pref_all, true);

            this.prefs.edit().putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        }
    }

    private String getString(String key) {
        return getString(key, "");
    }

    private String getString(String key, String def) {
        return prefs.getString(key, def);
    }

    private String getString(String key, int def) {
        return getString(key, context.getString(def));
    }

    private boolean getBoolean(String key, boolean def) {
        return prefs.getBoolean(key, def);
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

    public String getTrackArtist() {
        return getString("general_artist");
    }

    public String getTrackTitle() {
        return getString("general_title");
    }


    public String getServerProtocol() {
        // TODO: This is static for now but may change in future.
        return "http";
    }

    public String getServerHostname() {
        String serverName = getString("connection_address");

        if (serverName.indexOf(':') > 0) {
            serverName = serverName.split(":", 2)[0];
        }

        return serverName;
    }

    public int getServerPort() {
        String serverName = getString("connection_address");

        if (serverName.indexOf(':') > 0) {
            return  Integer.parseInt(serverName.split(":", 2)[1]);
        }

        return 8000;
    }

    public String getServerUsername() {
        return getString("connection_username");
    }

    public String getServerPassword() {
        return getString("connection_password");
    }

    public boolean getServerReconnect() {
        return getBoolean("connection_reconnect", false);
    }

    public String getServerMountpoint() {
        return getString("connection_mountpoint");
    }

    public boolean isServerSet() {
        if (!getServerHostname().isEmpty() && !getServerMountpoint().isEmpty() && !getServerUsername().isEmpty() && !getServerPassword().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public URL getServerStreamURL() throws MalformedURLException {
        return new URL(getServerProtocol(), getServerHostname(), getServerPort(), getServerMountpoint());
    }

    public int getAudioSampleRate() {
        return Integer.parseInt(getString("audio_samplerate", R.string.pref_default_audio_samplerate));
    }

    public int getAudioChannels() {
        return Integer.parseInt(getString("audio_channels", R.string.pref_default_audio_channels));
    }


    public String getCodecType() {
        return getString("audio_codec", R.string.pref_default_audio_codec);
    }

    public double getCodecQuality() {
        return Double.parseDouble(getString("audio_quality", R.string.pref_default_audio_quality));
    }


    public int getVUMeterInterval() {
        return Integer.parseInt(getString("audio_interval", R.string.pref_default_vumeter_interval));
    }


    public int getVolumeLeft() {
        return prefs.getInt("volume_left", DEFAULT_VOLUME);
    }

    public int getVolumeRight() {
        return prefs.getInt("volume_left", DEFAULT_VOLUME);
    }

    public void setVolumeLeft(int volume) {
        prefs.edit().putInt("volume_left", volume).apply();
    }

    public void setVolumeRight(int volume) {
        prefs.edit().putInt("volume_right", volume).apply();
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
