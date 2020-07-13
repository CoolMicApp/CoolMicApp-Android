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

package cc.echonet.coolmicapp.BackgroundService.Server;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Server;
import cc.echonet.coolmicapp.Configuration.Track;
import cc.echonet.coolmicapp.R;
import cc.echonet.coolmicdspjava.CallbackHandler;
import cc.echonet.coolmicdspjava.InputStreamAdapter;
import cc.echonet.coolmicdspjava.Wrapper;
import cc.echonet.coolmicdspjava.WrapperConstants;

final class Driver implements Closeable {
    private static final String TAG = "BGS/Driver";

    private final @NonNull Context context;
    private @NonNull Profile profile;
    private final @NonNull CallbackHandler callbackHandler;

    private WrapperConstants.WrapperInitializationStatus initWrapper() {
        final WrapperConstants.WrapperInitializationStatus previousStatus = Wrapper.getState();
        final WrapperConstants.WrapperInitializationStatus status = Wrapper.init();
        final Context applicationContext = context.getApplicationContext();

        Log.d(TAG, "initWrapper: status = " + previousStatus + " -> " + status);

        switch (status) {
            case WRAPPER_UNINITIALIZED:
                break;
            case WRAPPER_INITIALIZATION_ERROR:
                if (previousStatus == status) {
                    Toast.makeText(applicationContext, R.string.mainactivity_native_components_previnit_error, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "initWrapper: Wrapper's exception: " + Wrapper.getInitException().toString());
                    Toast.makeText(applicationContext, R.string.mainactivity_native_components_init_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case WRAPPER_INTITIALIZED:
                Log.d(TAG, "initWrapper: Wrapper is ready.");
                break;
            default:
                Toast.makeText(applicationContext, R.string.mainactivity_native_components_unknown_state, Toast.LENGTH_SHORT).show();
        }

        return status;
    }

    Driver(@NonNull Context context, @NonNull Profile profile, @NonNull CallbackHandler callbackHandler) {
        this.context = context;
        this.profile = profile;
        this.callbackHandler = callbackHandler;

        Log.d(TAG, "Driver() called with: context = [" + context + "], profile = [" + profile + "], callbackHandler = [" + callbackHandler + "]");

        initWrapper();
    }

    public void setProfile(@NonNull Profile profile) {
        this.profile = profile;
    }

    public boolean isReady() {
        return initWrapper() == WrapperConstants.WrapperInitializationStatus.WRAPPER_INTITIALIZED;
    }

    public boolean hasCore() {
        return isReady() && Wrapper.hasCore();
    }

    public void startStream() throws IOException {
        Server server = profile.getServer();

        String hostname = server.getHostname();
        int port_num = server.getPort();
        String username = server.getUsername();
        String password = server.getPassword();
        String mountpoint = server.getMountpoint();

        int sampleRate = profile.getAudio().getSampleRate();
        int channel = profile.getAudio().getChannels();
        double quality = profile.getCodec().getQuality();
        String title = profile.getTrack().getTitle();
        String artist = profile.getTrack().getArtist();
        String codec_string = profile.getCodec().getType();

        int buffersize = AudioRecord.getMinBufferSize(sampleRate, channel == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        Log.d(TAG, hostname);
        Log.d(TAG, Integer.toString(port_num));

        Log.d(TAG, "Minimum Buffer Size: " + buffersize);
        int status = Wrapper.init(callbackHandler, hostname, port_num, username, password, mountpoint, codec_string, sampleRate, channel, buffersize);

        hasCore();

        if (status != 0) {
            throw new IOException("Failed to init Core: " + status);
        }

        status = Wrapper.performMetaDataQualityUpdate(title, artist, quality, 0);

        if (status != 0) {
            throw new IOException(context.getString(R.string.exception_failed_metadata_quality, status));
        }

        if (profile.getServer().getReconnect()) {
            status = Wrapper.setReconnectionProfile("enabled");
        } else {
            status = Wrapper.setReconnectionProfile("disabled");
        }

        if (status != 0) {
            throw new IOException(context.getString(R.string.exception_failed_reconnect, status));
        }

        status = Wrapper.start();

        Log.d(TAG, "Status:" + status);

        if (status != 0) {
            throw new IOException(context.getString(R.string.exception_start_failed, status));
        }

        int interval = profile.getVUMeter().getInterval();

        /* Normalize interval to a sample rate of 48kHz (as per Opus specs). */
        interval = (interval * sampleRate) / 48000;

        Wrapper.setVuMeterInterval(interval);
    }

    public boolean stopStream() {
        if (hasCore()) {
            Wrapper.stop();
            Wrapper.unref();
            return true;
        }
        return false;
    }

    public void reloadParameters() {
        Track track;

        track = profile.getTrack();

        Wrapper.performMetaDataQualityUpdate(track.getTitle(), track.getArtist(), profile.getCodec().getQuality(), 1);

        if (profile.getServer().getReconnect()) {
            Wrapper.setReconnectionProfile("enabled");
        } else {
            Wrapper.setReconnectionProfile("disabled");
        }
    }

    public void setGain(int scale, int left, int right) {
        Wrapper.setMasterGainStereo(scale, left, right);
    }

    public void setGain(int scale, int gain) {
        Wrapper.setMasterGainMono(scale, gain);
    }

    public void nextSegment(@Nullable InputStream inputStream) {
        final InputStreamAdapter inputStreamAdapter;

        if (inputStream == null) {
            inputStreamAdapter = null;
        } else {
            inputStreamAdapter = new InputStreamAdapter(inputStream);
        }

        Wrapper.nextSegment(inputStreamAdapter);
    }

    @Override
    public void close() throws IOException {
        stopStream();
    }
}
