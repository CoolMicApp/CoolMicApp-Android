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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Track extends ProfileBase {
    Track(@NotNull ProfileBase profile) {
        super(profile);
    }

    public @NotNull List<String> getKeys() {
        final @NotNull List<String> ret = new ArrayList<>();
        for (final @NotNull String key : new String[]{"artist", "title"}) {
            if (getValue(key, null) != null)
                ret.add(key);
        }
        return ret;
    }

    @Contract("_, !null -> !null; _, null -> _")
    public @Nullable String getValue(@NotNull String key, @Nullable String def) {
        final @Nullable String ret;

        switch (key.toLowerCase(Locale.ROOT)) {
            case "artist": ret = getArtist(); break;
            case "title": ret = getTitle(); break;
            default: ret = null; break;
        }

        if (ret == null || ret.isEmpty())
            return def;

        return ret;
    }

    public void setValue(@NotNull String key, @Nullable String value) {
        if (!(key.equals("artist") || key.equals("title")))
            return;

        editor.putString("general_" + key, value);
    }

    public String getArtist() {
        return getString("general_artist");
    }

    public String getTitle() {
        return getString("general_title");
    }

}
