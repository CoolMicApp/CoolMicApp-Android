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

package cc.echonet.coolmicapp.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import cc.echonet.coolmicapp.BuildConfig;

public class GlobalConfiguration {
    private static final String PROFILE_NAME = "_global";
    private static final String KEY_PROFILE_CURRENT = "profile_current";
    private static final String KEY_DEVELOPER_MODE = "developer_mode";

    private final @NotNull Context context;
    private final @NotNull SharedPreferences prefs;

    public GlobalConfiguration(@NotNull Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PROFILE_NAME, Context.MODE_PRIVATE);

        setDefaults();
    }

    private void setDefaults() {
        SharedPreferences.Editor editor;

        if (prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false))
            return;

        editor = prefs.edit();

        editor.putString(KEY_PROFILE_CURRENT, Manager.DEFAULT_PROFILE);
        editor.putBoolean(KEY_DEVELOPER_MODE, BuildConfig.DEBUG);

        editor.putBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        editor.apply();
    }

    public String getCurrentProfileName() {
        return prefs.getString(KEY_PROFILE_CURRENT, Manager.DEFAULT_PROFILE);
    }

    public void setCurrentProfileName(String profileName) {
        Profile.assertValidProfileName(profileName);
        prefs.edit().putString(KEY_PROFILE_CURRENT, profileName).apply();
    }

    public boolean getDeveloperMode() {
        return prefs.getBoolean(KEY_DEVELOPER_MODE, BuildConfig.DEBUG);
    }

    public void setDeveloperMode(boolean mode) {
        prefs.edit().putBoolean(KEY_DEVELOPER_MODE, mode).apply();
    }
}
