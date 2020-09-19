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

package cc.echonet.coolmicapp;

import android.content.Context;
import android.util.Base64;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Server;

public final class CMTS {
    private static final String[] CMTSHosts = {"coolmic.net", "echonet.cc", "64.142.100.248", "64.142.100.249", "46.165.219.118"};

    public static boolean isCMTSConnection(@NotNull Profile profile) {
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

    public static void loadCMTSData(@NotNull Profile profile) {
        final @NotNull String randomComponent = generateShortUuid();
        final @NotNull Context context = profile.getContext();
        final @NotNull Server server;

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
