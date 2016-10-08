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
package cc.echonet.coolmicapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CoolMic {
     SharedPreferences prefs = null;
     Context context = null;

     public CoolMic(Context context, String settingskey) {
        this.prefs = context.getSharedPreferences(settingskey, Context.MODE_PRIVATE);
        this.context = context;

         if (!this.prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
             PreferenceManager.setDefaultValues(context, settingskey, Context.MODE_PRIVATE, R.xml.pref_all, true);

             prefs.edit().putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
         }
     }

    public String getTitle() {
        return prefs.getString("general_title", context.getString(R.string.pref_default_general_title));
    }

    public String getArtist() {
        return prefs.getString("general_artist", context.getString(R.string.pref_default_general_artist));
    }

    public String getServerName() { return prefs.getString("connection_address", ""); }

    public String getMountpoint() {
        return prefs.getString("connection_mountpoint", "");
    }

    public String getUsername() {
        return prefs.getString("connection_username", "");
    }

    public String getPassword() {
        return prefs.getString("connection_password", "");
    }

    public String getSampleRate() { return prefs.getString("audio_samplerate", context.getString(R.string.pref_default_audio_samplerate)); }

    public String getChannels() {
        return prefs.getString("audio_channels", context.getString(R.string.pref_default_audio_channels));
    }

    public String getQuality() {
        return prefs.getString("audio_quality", context.getString(R.string.pref_default_audio_quality));
    }

    public String getVuMeterInterval() {
        return prefs.getString("vumeter_interval", context.getString(R.string.pref_default_vumeter_interval));
    }

    public String getStreamStatsURL() {
        return String.format("http://%s:%s@%s/admin/stats.xml?mount=/%s", this.getUsername(), this.getPassword(), this.getServerName(), this.getMountpoint());
    }

    public boolean isConnectionSet() {
        if (!this.getServerName().isEmpty() && !this.getMountpoint().isEmpty() && !this.getUsername().isEmpty() && !this.getPassword().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
