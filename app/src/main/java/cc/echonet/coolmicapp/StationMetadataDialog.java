/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2021
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
import cc.echonet.coolmicapp.Configuration.Station;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StationMetadataDialog extends MetadataDialog<StationMetadataDialog.Adapter> {
    final static class Adapter extends MetadataDialog.Adapter {

        protected @NotNull List<@NotNull String> getKeys() {
            return new ArrayList<>(profile.getStation().getMetadata().keySet());
        }

        @Override
        protected String getValue(@NotNull String key, @Nullable String def) {
            return profile.getStation().getValue(key, def);
        }

        @Override
        protected String getKeyDisplayName(@NotNull String key) {
            return Station.getKeyDisplayName(key);
        }

        public Adapter(@NonNull @NotNull Context context, @NotNull Profile profile) {
            super(context, profile);
        }

        @Override
        protected void setMetadata(@NotNull String key, @Nullable String value) {
            profile.edit();
            profile.getStation().setValue(key.toLowerCase(Locale.ROOT), value);
            profile.apply();
            reloadData();
        }
    }

    protected boolean customKeysAllowed() {
        return false;
    }

    protected @NotNull List<@NotNull String> getStandardKeys() {
        return Station.STANDARD_KEYS;
    }

    public StationMetadataDialog(@NotNull Context context, @NotNull Profile profile) {
        super(context, profile, new Adapter(context, profile));
    }
}
