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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Track;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrackMetadataDialog extends MetadataDialog<TrackMetadataDialog.Adapter> {
    final static class Adapter extends MetadataDialog.Adapter {

        @Override
        protected @NotNull List<@NotNull String> getKeys() {
            return profile.getTrack().getKeys();
        }

        @Override
        protected String getValue(@NotNull String key, @Nullable String def) {
            return profile.getTrack().getValue(key, def);
        }

        protected String getKeyDisplayName(@NotNull String key) {
            return Track.getKeyDisplayName(key);
        }

        public Adapter(@NonNull @NotNull Context context, @NotNull Profile profile) {
            super(context, profile);
        }

        @Override
        public void setMetadata(@NotNull String key, @Nullable String value) {
            profile.edit();
            profile.getTrack().setValue(key, value);
            profile.apply();
            reloadData();
        }
    }

    protected boolean customKeysAllowed() {
        return true;
    }

    protected @NotNull List<@NotNull String> getStandardKeys() {
        return Track.STANDARD_KEYS;
    }

    @Contract(pure = true)
    public TrackMetadataDialog(@NotNull Context context, @NotNull Profile profile) {
        super(context, profile, new Adapter(context, profile));
    }
}
