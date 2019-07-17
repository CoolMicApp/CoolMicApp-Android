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

import cc.echonet.coolmicapp.Configuration.Manager;
import cc.echonet.coolmicapp.Configuration.Profile;

public class CoolMic {
    private Profile profile;

    public CoolMic(Context context, String settingskey) {
        Manager manager = new Manager(context);

        this.profile = manager.getProfile(settingskey);
    }

    public Profile getProfile() {
        return profile;
    }

    public boolean isCMTSConnection() {
        String CMTSHosts[] = {"coolmic.net", "echonet.cc", "64.142.100.248", "64.142.100.249", "46.165.219.118"};
        String serverName = profile.getServerHostname();

        for (int i = 0; i < CMTSHosts.length; i++) {
            if (serverName.endsWith(CMTSHosts[i])) {
                return true;
            }
        }

        return false;
    }
}
