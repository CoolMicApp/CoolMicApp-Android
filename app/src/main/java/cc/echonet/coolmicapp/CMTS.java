package cc.echonet.coolmicapp;

import android.content.Context;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Server;

public class CMTS {
    private static final String[] CMTSHosts = {"coolmic.net", "echonet.cc", "64.142.100.248", "64.142.100.249", "46.165.219.118"};

    public static boolean isCMTSConnection(Profile profile) {
        String serverName = profile.getServer().getHostname();

        for (String cmtsHost : CMTSHosts) {
            if (serverName.endsWith(cmtsHost)) {
                return true;
            }
        }

        return false;
    }

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

    public static void loadCMTSData(Profile profile) {
        String randomComponent = generateShortUuid();
        Context context = profile.getContext();
        Server server;

        /* copy profile so we have our own edit state */
        profile = new Profile(profile);

        profile.edit();

        profile.getCodec().setType(context.getString(R.string.pref_default_audio_codec));
        profile.getAudio().setSampleRate(Integer.parseInt(context.getString(R.string.pref_default_audio_samplerate)));
        server = profile.getServer();
        server.setAddress(context.getString(R.string.pref_default_connection_address));
        server.setUsername(context.getString(R.string.pref_default_connection_username));
        server.setPassword(context.getString(R.string.pref_default_connection_password));
        server.setMountpoint(context.getString(R.string.pref_default_connection_mountpoint, randomComponent));

        profile.apply();
    }
}
