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

import android.os.SystemClock;

import java.util.Locale;

import cc.echonet.coolmicapp.BuildConfig;

public class DialogState extends ProfileBase {
    private static final String KEY_VERSION = "version";
    private static final String KEY_TIMESTAMP = "timestamp";

    final DialogIdentifier dialogIdentifier;

    DialogState(ProfileBase profile, DialogIdentifier dialogId) {
        super(profile);
        this.dialogIdentifier = dialogId;
    }

    private String buildKey(String subkey) {
        return "dialogstate-" + getDialogIdentifier().toString() + "-" + subkey;
    }

    private String buildVersionString() {
        return BuildConfig.VERSION_NAME + " " +
                BuildConfig.GIT_REVISION + "-" +
                BuildConfig.GIT_DIRTY + " " +
                "[" + BuildConfig.BUILD_TS + "]";
    }

    public DialogIdentifier getDialogIdentifier() {
        return dialogIdentifier;
    }

    public boolean hasEverShown() {
        return !getString(buildKey(KEY_VERSION)).equals("");
    }

    public boolean hasShownInThisVersion() {
        return buildVersionString().equals(getString(buildKey(KEY_VERSION)));
    }

    public void shown() {
        edit();
        editor.putString(buildKey(KEY_VERSION), buildVersionString());
        editor.putLong(buildKey(KEY_TIMESTAMP), System.currentTimeMillis());
        apply();
    }

    public void reset() {
        edit();
        editor.putString(buildKey(KEY_VERSION), "");
        editor.putLong(buildKey(KEY_TIMESTAMP), Long.MIN_VALUE);
        apply();
    }
}
